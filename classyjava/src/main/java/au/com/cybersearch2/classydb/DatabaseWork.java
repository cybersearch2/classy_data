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
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.classydb;

import javax.persistence.EntityTransaction;

import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * DatabaseWork
 * Sub classes perform native database transactions
 * @author Andrew Bowley
 * 28/06/2014
 */
public abstract class DatabaseWork
{
    private static final String TAG = "DatabaseWork";
    static Log log = JavaLogger.getLogger(TAG);
 
    /** Transaction object with pre-commit callback used to perform work (actually ClassyEntityTransaction class) */
    protected EntityTransaction transaction;
    /** Work result type is Boolean to indicate success or failure */
    protected Boolean result;

    /**
     * Construct a DatabaseWork object
     * @param connectionSource Open ConnectionSource object
     */
    public DatabaseWork(final ConnectionSource connectionSource)
    {
        // Default result to fail
        result = Boolean.FALSE;
        // Pass pre-commit callback to transaction constructor
        transaction = getTransaction(connectionSource, new TransactionCallable(){

            @Override
            public Boolean call(DatabaseConnection databaseConnection) throws Exception 
            {
                result = DatabaseWork.this.doInBackground(databaseConnection);
                return result;
            }

        });
    }
    
    public abstract Boolean doInBackground(DatabaseConnection databaseConnection) throws Exception;

    /**
     * Runs on separate thread after successful completion of {@link #doInBackground(DatabaseConnection databaseConnection)}.
     * @param success True if PersistenceWork completed successfully, otherwise false
     */
    public void onPostExecute(boolean success)
    {
    }

    /**
     * Handle rollback caused by exception while executing {@link #doInBackground(DatabaseConnection databaseConnection)}
     * @param rollbackException Throwable exception which caused rollback
     */
    public void onRollback(Throwable rollbackException)
    {
    }

    public EntityTransaction getTransaction() 
    {
        return transaction;
    }

    public Boolean getResult() 
    {
        return result;
    }

    protected EntityTransaction getTransaction(final ConnectionSource connectionSource, TransactionCallable callable)
    {
        return new EntityTransactionImpl(connectionSource, callable);
    }
}
