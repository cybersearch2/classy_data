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

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.DatabaseConnection;

/**
 * PreCommit
 * Prior to transaction commmit, executes Callable object and records success and any Exception or expected RuntimeException thrown. 
 * Expected: PersistenceException, IllegalArgumentException, IllegalStateException, UnsupportedOperationException
 * Flags rollback required if exception is thrown or call returns false, indicating a fatal error has occured. 
 * @author Andrew Bowley
 * 18/07/2014
 */
public class PreCommit
{
    /** Caught Exception or RuntimeException declared in EntityManager interface  */
    protected Throwable preCommitException;
    /** Flags rollback only */
    protected boolean doRollback;
    /** The Callable object */
    protected TransactionCallable onPreCommit;
    
    /**
     * Create PreCommit object
     * @param onPreCommit Callable
     * @see java.util.concurrent.Callable
     */
    public PreCommit(TransactionCallable onPreCommit)
    {
        this.onPreCommit = onPreCommit;
    }
    
    /**
     * Execute call
     * @param databaseConnection Open database connection on which transaction is active
     */
    public void doPreCommit(DatabaseConnection databaseConnection)
    {
        boolean success = false; // Use flag to identify RuntimeExceptions
        try
        {
            doRollback = !onPreCommit.call(databaseConnection);
            success = true;
        }
        catch (PersistenceException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (IllegalArgumentException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (IllegalStateException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (UnsupportedOperationException e)
        {
            preCommitException = e;
            doRollback = true;
        }
        catch (RuntimeException e)
        {
            throw(e);
        }
        catch (Exception e)
        {
            preCommitException = e;
            doRollback = true;
        }
        finally
        {
            if (!success)
                doRollback = true;
        }
    }

    /**
     * Returns caught Exception, if thrown
     * @return Throwable or null if no exception thrown
     */
    public Throwable getPreCommitException() 
    {
        return preCommitException;
    }

    /**
     * Returns rollback flag
     * @return boolean - true if rollback only
     */
    public boolean isDoRollback() 
    {
        return doRollback;
    }
}
