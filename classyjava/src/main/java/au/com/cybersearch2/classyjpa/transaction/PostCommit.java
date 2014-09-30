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

import java.util.concurrent.Callable;

import javax.persistence.PersistenceException;

/**
 * PostCommit
 * Executes Callable object and records success and any Exception or expected RuntimeException thrown. 
 * Expected: PersistenceException, IllegalArgumentException, IllegalStateException, UnsupportedOperationException
 * Only non-important code should to be executed such as for auditing, logging and other cross-cutting concerns 
 * following transaction commit/rollback. 
 * @author Andrew Bowley
 * 18/07/2014
 */
public class PostCommit
{
    /** Caught Exception or RuntimeException declared in EntityManager interface  */
    protected Throwable postCommitException;
    /** Error message for unexpected RuntimeException, ie. not declared in EntityManager interface. */
    protected String error;
    /** The Callable object */
    protected Callable<Boolean> onPostCommit;
   
    /**
     * Create PostCommit object
     * @param onPostCommit Callable
     * @see java.util.concurrent.Callable
     */
    public PostCommit(Callable<Boolean> onPostCommit)
    {
        this.onPostCommit = onPostCommit;
    }
    
    /**
     * Execute call
     */
    public void doPostCommit()
    {
        boolean success = false; // Use flag to identify RuntimeExceptions
        try
        {
            onPostCommit.call();
            success = true;
        }
        catch (PersistenceException e)
        {
            postCommitException = e;
        }
        catch (IllegalArgumentException e)
        {
            postCommitException = e;
        }
        catch (IllegalStateException e)
        {
            postCommitException = e;
        }
        catch (UnsupportedOperationException e)
        {
            postCommitException = e;
         }
        catch (RuntimeException e)
        {
            throw(e);
        }
        catch (Exception e)
        {
            postCommitException = e;
        }
        finally
        {
            if (!success)
                error = "postCommit operation failed due to unexpected exception";
        }
    }

    /**
     * Returns caught Exception, if thrown
     * @return Throwable or null if no exception thrown
     */
    public Throwable getPostCommitException() 
    {
        return postCommitException;
    }

    /**
     * Returns error message for unexpected exception
     * @return String or null if unexpected exception did not occur
     */
    public String getError() 
    {
        return error;
    }
}
