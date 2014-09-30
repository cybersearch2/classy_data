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
package au.com.cybersearch2.classyjpa.transaction;

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * AndroidTransactionState
 * @author Andrew Bowley
 * 31/07/2014
 */
public class AndroidTransactionState extends TransactionState
{
    protected AndroidConnectionSource androidConnectionSource;
    protected boolean nested;
    
    /**
     * @param connectionSource
     * @throws SQLException
     */
    public AndroidTransactionState(ConnectionSource connectionSource)
            throws SQLException
    {
        super(connectionSource);
    }

    @Override
    protected void setup() throws SQLException
    {
        if (!(connectionSource instanceof AndroidConnectionSource))
            throw new PersistenceException("Class AndroidTransactionState only supports type AndroidConnectionSource");
        androidConnectionSource = (AndroidConnectionSource)connectionSource;
        nested = !connection.isAutoCommit();
        if (!nested)
            super.setup();
    }
    
    /**
     * Commit
     *@throws SQLException
     */
    public void doCommit() throws SQLException 
    {
        if (!nested)
            super.doCommit();
    }

    /**
     * Rollback
     *@throws SQLException
     */
    public void doRollback() throws SQLException 
    {
        if (!nested)
            super.doRollback();
        connection.rollback(null);
    }
}
