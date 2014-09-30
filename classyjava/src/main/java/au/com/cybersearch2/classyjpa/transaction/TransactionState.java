/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> 
    Code originates from com.j256.ormlite.misc.TransactionManager by graywatson
    */
package au.com.cybersearch2.classyjpa.transaction;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import au.com.cybersearch2.classylog.*;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * TransactionState
 * Database connection management to enable transaction begin(), commit() and rollback()
 * @author Andrew Bowley
 * 09/05/2014
 */
public class TransactionState
{
    private static final String TAG = "TransactionState";
    protected static Log log = JavaLogger.getLogger(TAG);

    protected static final String SAVE_POINT_PREFIX = "ORMLITE";
    protected static AtomicInteger savePointCounter;

    protected ConnectionSource connectionSource;
    protected DatabaseConnection connection;
    protected Boolean autoCommitAtStart;
    protected Boolean hasSavePoint;
    protected Savepoint savePoint;

    static
    {   // Use counter to generate unique savepoint identifiers
        // Note SQLite does not support nested save points
        savePointCounter = new AtomicInteger();
    }
    
 /**
  * Construct a TransactionState instance   
  * @param connectionSource ConnectionSource to be used for database operations
  * @throws SQLException if ConnectionSource error occurs
  */
    public TransactionState(ConnectionSource connectionSource) throws SQLException
    {
        this.connectionSource = connectionSource;
        boolean success = false;
        try
        {
            connection = connectionSource.getReadWriteConnection();
            setup();
            success = true;
        }
        finally
        {   // Clean up if connection was not established
            if (!success)
                release();
        }
    }

    public DatabaseConnection getDatabaseConnection()
    {
        return connection;
    }
    
    protected void setup() throws SQLException
    {
        /*
         * Save this connection and return it for all calls to connectionSource.getReadOnlyConnection() and
         * connectionSource.getReadWriteConnection() 
         * unless the connectionSource.clearSpecialConnection(DatabaseConnection) method is
         * called. This is used by the transaction mechanism since since all operations within a transaction must
         * operate on the same connection. It is also used by the Android code during initialization.
         * 
         * <p>
         * <b> NOTE: </b> This should be a read-write connection since transactions and Android need it to be so.
         * </p>
         * 
         * <p>
         * <b> NOTE: </b> Saving a connection is usually accomplished using ThreadLocals so multiple threads should not be
         * using connections in this scenario.
         * </p>
         * 
         */
        connectionSource.saveSpecialConnection(connection);
        setAutoCommit();
        setSavePoint();
    }
    
    /**
     * Reset everything to initial state
     */
    public void release()
    {
        if (connection != null)
        {
            savePoint = null;
            hasSavePoint = null;
            if (autoCommitAtStart != null)
                resetAutoCommit();
            clearSpecialConnection();
            connection = null;
        }
    }
  
    /**
     * Commit
     * @throws SQLException
     */
    public void doCommit() throws SQLException 
    {
        // Perform check for release state. 
        if (!isValid())
        {
            if (log.isLoggable(TAG, Level.WARNING))
                log.warn(TAG, "doCommit() called while invalid");
            return;
        }
        try
        {
            String name = (savePoint == null ? null : savePoint.getSavepointName());
            if (name == null)
                name = "";
            connection.commit(savePoint);
            if (log.isLoggable(TAG, Level.FINE))
                log.debug(TAG, "committed savePoint transaction " + name);
        }
        finally
        {
            release();
        }
    }

    /**
     * Rollback
     * @throws SQLException
     */
    public void doRollback() throws SQLException 
    {
        // Perform check for release state. 
        if (!isValid())
        {
            if (log.isLoggable(TAG, Level.WARNING))
                log.warn(TAG, "doCommit() called while invalid");
            return;
        }
        try
        {
            String name = (savePoint == null ? null : savePoint.getSavepointName());
            if (name == null)
                name = "";
            connection.rollback(savePoint);
            if (log.isLoggable(TAG, Level.FINE))
                log.debug(TAG, "rolled back savePoint transaction " + name);
        }
        finally
        {
            release();
        }
    }

    /**
     * Check for release state
     * @return boolean true for is valid
     */
    private boolean isValid()
    {
        return (connection != null) && (hasSavePoint != null) && (autoCommitAtStart != null); 
    }

    /**
     * Turn off auto commit
     * @throws SQLException
     */
    private void setAutoCommit() throws SQLException
    {
        if (connection.isAutoCommitSupported()) 
        {
            autoCommitAtStart = Boolean.valueOf(connection.isAutoCommit());
            if (autoCommitAtStart) 
            {
                // Disable auto-commit mode if supported and enabled at start
                connection.setAutoCommit(false);
                if (log.isLoggable(TAG, Level.FINE))
                    log.debug(TAG, "Had to set auto-commit to false");
            }
        }

    }

    /**
     * Store save point
     * @throws SQLException
     */
    private void setSavePoint() throws SQLException
    {
        savePoint = connection.setSavePoint(SAVE_POINT_PREFIX + savePointCounter.incrementAndGet());
        String name = (savePoint == null ? null : savePoint.getSavepointName());
        if (name == null)
            name = "";
        if (log.isLoggable(TAG, Level.FINE))
            log.debug(TAG, "Started savePoint transaction " + name);
        hasSavePoint = Boolean.valueOf(true);
    }
 
    /**
     * Restore auto commit if required
     */
    private void resetAutoCommit()
    {
        // try to restore if we are in auto-commit mode
        if (autoCommitAtStart) 
            try
            {
                connection.setAutoCommit(true);
                if (log.isLoggable(TAG, Level.FINE))
                    log.debug(TAG, "restored auto-commit to true");
            }
            catch (SQLException e)
            {
                if (log.isLoggable(TAG, Level.WARNING))
                    log.warn(TAG, "setAutoCommit() failed");
            }
            finally
            {
                autoCommitAtStart = null;
            }
    }
    
    /**
     * Clear arrangement to use a single connection for the transaction
     */
    private void clearSpecialConnection()
    {
        connectionSource.clearSpecialConnection(connection);
        /* com.j256.ormlite.jdbc.JdbcConnectionSource releaseConnection() is "noop right now"
        try
        {
            connectionSource.releaseConnection(connection);
        }
        catch (SQLException e)
        {
            if (log.isLoggable(TAG, Level.WARNING))
                log.warn(TAG, "releaseConnection() failed");
        }
        */
    }
}
