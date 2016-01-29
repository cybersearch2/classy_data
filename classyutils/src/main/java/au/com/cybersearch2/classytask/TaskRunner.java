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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * TaskRunner
 * @author Andrew Bowley
 * 8 Jan 2016
 */
public class TaskRunner extends Executable 
{
    /** Manages the background task thread pool */
    protected TaskManager taskManager; 
    /** Post inter-thread messages */
    TaskMessenger taskMessenger;
    /** Work status PENDING, RUNNING, FINISHED, FAILED */
    protected volatile WorkStatus status;
    /** Worker task executes background thread using java.util.concurrent.FutureTask and related classes */
    protected BackgroundTask backgroundTask;
    /** The FutureTask implementation - overrides abstract method done() */
    private FutureTask<Boolean> future;

    /**
     * Construct TaskRunner object
     * @param taskManager Manages the thread pool
     * @param taskMessenger Post inter-thread messages
     */
    public TaskRunner(TaskManager taskManager, TaskMessenger taskMessenger)
    {
        this.taskManager = taskManager;
        this.taskMessenger = taskMessenger;
        // Work status PENDING, RUNNING, FINISHED, FAILED
        status = WorkStatus.PENDING;
    }

    /**
     * Returns task status
     * @return WorkStatus
     */
    @Override
    public WorkStatus getStatus()
    {
        return status;
    }

    /**
     * Executes the worker task. Any required parameters will need to be provided as fields 
     * belonging to the sub class. 
     * The task returns itself (this) so that the caller can keep a reference to it.
     * @param backgroundTask Task to run
     * @return Executable to track status
     * @throws IllegalStateException If status is either
     *         {@link WorkStatus#RUNNING} or {@link WorkStatus#FINISHED}.
     */
    public final Executable execute(BackgroundTask backgroundTask) 
    {
        switch (status) 
        {
        case RUNNING:
            throw new IllegalStateException("Cannot execute task:"
                    + " the task is already running.");
        case FINISHED:
        case FAILED:
            throw new IllegalStateException("Cannot execute task:"
                    + " the task has already been executed "
                    + "(a task can be executed only once)");
        case PENDING:
            break;
        default:
            break;
        }
        status = WorkStatus.RUNNING;
        this.backgroundTask = backgroundTask;

        backgroundTask.onPreExecute();
        // The FutureTask implementation - overrides abstract method done()
        future = createFutureTask(); // future executes in execute() method
        taskManager.getExecutor().execute(future);
        return this;
    }

     /**
      * Attempts to cancel execution of this task.  This attempt will
      * fail if the task has already completed, already been cancelled,
      * or could not be cancelled for some other reason. If successful,
      * and this task has not started when <tt>cancel</tt> is called,
      * this task should never run.  If the task has already started,
      * then the <tt>mayInterruptIfRunning</tt> parameter determines
      * whether the thread executing this task should be interrupted in
      * an attempt to stop the task.
      *
      * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this
      *        task should be interrupted; otherwise, in-progress tasks are allowed
      *        to complete.
      *
      * @return <tt>false</tt> if the task could not be cancelled,
      *         typically because it has already completed normally;
      *         <tt>true</tt> otherwise
      *
      * @see #isCancelled()
      * @see BackgroundTask#onCancelled(Boolean) 
      */
     public final boolean cancel(boolean mayInterruptIfRunning) 
     {
         return future.cancel(mayInterruptIfRunning);
     }

    /**
     * Returns <tt>true</tt> if this task was cancelled before it completed normally.
     *
     * @return boolean
     *
     * @see #cancel(boolean)
     */
    public final boolean isCancelled() 
    {
        return (future == null) || future.isCancelled();
    }


    /**
     * Post result to calling thread
     * @param result Object of generic type Result
     * @return Object of generic type Result or null if task cancelled before result available
     */
    protected Boolean postResult(Boolean result) 
    {
        if ((future == null) || future.isCancelled())
            taskMessenger.sendCancel(backgroundTask, result);
        else
            taskMessenger.sendResult(this, result);
        return result;
    }

    /**
     * Process signalled result after task has run
     * @param result Object of generic type Result or null if task cancelled before result available
     */
    protected void finish(Boolean result) 
    {
        if ((future == null) || future.isCancelled())
            backgroundTask.onCancelled(result);
        else
            backgroundTask.onPostExecute(result);
        // Final status will be FINISHED or FAILED
        if (status != WorkStatus.FAILED)
            status = WorkStatus.FINISHED;
    }

   /**
     * Returns Concurrent worker
     * @param worker
     * @return FutureTask&lt;Result&gt;
     */
    private FutureTask<Boolean> createFutureTask()
    {
        return new FutureTask<Boolean>(backgroundTask) 
        {
            @Override
            protected void done() 
            {
                Boolean result = null;
                try 
                {
                    // Waits for the computation to complete, and then
                    // retrieves its result.
                    result = get();
                } 
                catch (InterruptedException e) 
                {
                    // Cause a null result to be posted to indicate unexpected failure to complete task.
                } 
                catch (ExecutionException e) 
                {
                    backgroundTask.setExecutionException(e);
                    postResult(null);
                    throw new RuntimeException("An error occured while executing doInBackground()",
                            e.getCause());
                } 
                catch (CancellationException e) 
                {
                    // Cause a null result to be posted to indicate unexpected failure to complete task.
                } 
                catch (Throwable t) 
                {
                    throw new RuntimeException("An error occured while executing doInBackground()", t);
                }
                if (backgroundTask.wasTaskInvoked())
                    postResult(result);
            }
        };
    }
}
