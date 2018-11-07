/**
    Copyright (C) 2016  www.cybersearch2.com.au

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
package au.com.cybersearch2.classytask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BackgroundTask
 * @author Andrew Bowley
 * 8 Jan 2016
 */
public abstract class BackgroundTask implements Callable<Boolean>
{
    /** Exception thrown when the worker task fails due to a uncaught RuntimeException */
    protected ExecutionException executionException;
    /** Flag set when task invoked. Used in logic to handle cancel request */
    private final AtomicBoolean taskInvoked = new AtomicBoolean();
    /** Thread helper to lower priority according to system platform */
    private ThreadHelper threadHelper;

    /**
     * Construct BackgroundTask object
     * @param threadHelper ThreadHelper allows thread priority adjustment
     */
    public BackgroundTask(ThreadHelper threadHelper)
    {
        this.threadHelper = threadHelper;
    }
    
    /**
     * Override this method to perform a computation on a background thread. Any
     * required parameters will need to be provided as fields belonging to the sub class.
     * @return Boolean object to flag success (true), failure (false) or cancel (null)
     *
     * @see #onPreExecute()
     * @see #onPostExecute(Boolean)
     */
    public abstract boolean doInBackground();

    /**
     * Runs on the UI thread before {@link #doInBackground()}.
     *
     * @see #onPostExecute(Boolean)
     * @see #doInBackground()
     */
    public void onPreExecute() 
    {
    }

    /**
     * Runs on the UI thread after {@link #doInBackground()}. The
     * specified result is the value returned by {@link #doInBackground()}
     * or null if the task was cancelled or an exception occured.
     *
     * @param result The result of the operation computed by {@link #doInBackground()}.
     *
     * @see #onPreExecute()
     * @see #doInBackground()
     */
    public void onPostExecute(Boolean result) 
    {
    }


    /**
     * Runs on separate thread after {@link TaskRunner#cancel(boolean)} is invoked. 
     *
     *
     * @param result The result of the operation computed by {@link #doInBackground()}.
     *
     * @see TaskRunner#cancel(boolean)
     * @see TaskRunner#isCancelled()
     */
    public void onCancelled(Boolean result) 
    {
    }

    /**
     * Start task on background thread
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Boolean call() throws Exception
    {
        taskInvoked.set(true); // Set flag so FutureTask done() knows whether to it has to post results
        threadHelper.setBackgroundPriority();
        return Boolean.valueOf(doInBackground());
    }

    /**
     * Set Exception thrown when the worker task fails due to a uncaught RuntimeException  
     * @param executionException ExecutionException
     */
    public void setExecutionException(ExecutionException executionException)
    {
        this.executionException = executionException;
    }

    /**
     * Returns Exception thrown when the worker task fails due to a uncaught RuntimeException
     * @return ExecutionException
     */
    public ExecutionException getExecutionException()
    {
        return executionException;
    }

    /**
     * Returns flag set true when task starts execution
     * @return boolean
     */
    protected boolean wasTaskInvoked() 
    {
        final boolean wasTaskInvoked = taskInvoked.get();
        return wasTaskInvoked;
    }
 
}
