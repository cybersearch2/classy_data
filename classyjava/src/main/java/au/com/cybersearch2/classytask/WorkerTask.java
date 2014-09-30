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
// Original copyright - code mostly unchanged except for promotion of static classes to separate classes
/*
 * Copyright (C) 2008 The Android Open Source Project, Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.com.cybersearch2.classytask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;

/**
 * WorkerTask
 * @author Romain Guy, Andrew Bowley
 * 25/04/2014
  * <p>WorkerTask enables proper and easy use of a non-UI thread. This class allows to
 * perform background operations and publish results on the calling thread without
 * having to manipulate threads and/or handlers.</p>
 *
 * <p>A worker task is defined by a computation that runs on a background thread and
 * whose result is published on the calling thread. A worker task is defined by one generic
 * type, called <code>Result</code>,
 * and 3 steps, called <code>onPreExecute</code>, <code>doInBackground</code>
 *  and <code>onPostExecute</code>.</p>
 *
 * <h2>Usage</h2>
 * <p>WorkerTask must be subclassed to be used. The subclass will override at least
 * one method ({@link #doInBackground()}), and most often will override a
 * second one ({@link #onPostExecute(Object)}.)</p>
 *
 * <p>Here is an example of subclassing:</p>
 * <pre>
 * private class DownloadFilesTask extends WorkerTask&lt;URL, Long&gt; {
 * 
 *     List<URL> urls;
 *     
 *     public void setUrls(List<URL> urls)
 *     {
 *         this.urls = urls;
 *     } 
 *     
 *     public File doInBackground() {
 *         long totalSize = 0;
 *         for (Url url: urls) {
 *             totalSize += Downloader.downloadFile(urls);
 *         }
 *     }
 *
 *     public void onPostExecute(Long result) {
 *         showDialog("Downloaded " + result + " bytes");
 *     }
 * }
 * </pre>
 *
 * <p>Once created, a task is executed very simply:</p>
 * <pre>
 * DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
 * downloadFilesTask.setUrls(urls);
 * downloadFilesTask.execute();
 * </pre>
 *
 * <h2>User task's generic type</h2>
 * <p>The single type used by a user task is:</p>
 * <ol>
 *     <li><code>Result</code>, the type of the result of the background
 *     computation.</li>
 * </ol>
 * <p>To mark the Result type as unused, simply use the type {@link Void}:</p>
 * <pre>
 * private class MyTask extends WorkerTask<Void) { ... }
 * </pre>
 *
 * <h2>The 3 steps</h2>
 * <p>When a user task is executed, the task goes through 3 steps:</p>
 * <ol>
 *     <li>{@link #onPreExecute()}, invoked on the caller thread immediately after the task
 *     is executed. This step is normally used to setup the task, for instance by
 *     showing a progress bar in the user interface.</li>
 *     <li>{@link #doInBackground()}, invoked on the background thread
 *     immediately after {@link #onPreExecute ()} finishes executing. This step is used
 *     to perform background computation that can take a long time. The parameters
 *     of the worker task are passed to this step. The result of the computation must
 *     be returned by this step and will be passed back to the last step. 
 *     <li>{@link #onPostExecute (Object)}, invoked on the caller thread after the background
 *     computation finishes. The result of the background computation is passed to
 *     this step as a parameter.</li>
 * </ol>
 *
 * <h2>Threading rules</h2>
 * <p>There are a few threading rules that must be followed for this class to
 * work properly:</p>
 * <ul>
 *     <li>{@link #execute()} must be invoked on the caller thread.</li>
 *     <li>Do not call {@link #onPreExecute ()}, {@link #onPostExecute (Object)},
 *     {@link #doInBackground()}
 *     manually.</li>
 *     <li>The task can be executed only once (an exception will be thrown if
 *     a second execution is attempted.)</li>
 * </ul>
*/
public abstract class WorkerTask<Result> implements ThreadMessenger<Result>, Executable
{
    /** Manages the thread pool */
    protected static TaskManager taskManager; 
    /** Work status PENDING, RUNNING, FINISHED, FAILED */
    protected volatile WorkStatus status;
    /** Worker task executes background thread using java.util.concurrent.FutureTask and related classes */
    private final WorkerRunnable<Result> worker;
    /** The FutureTask implementation - overrides abstract method done() */
    private final FutureTask<Result> future;

    static
    {
        taskManager = new TaskManager();
    }

    /**
     * Create WorkTask object. 
     */
    public WorkerTask()
    {
        // Worker task executes background thread using java.util.concurrent.FutureTask and related classes
        worker = new WorkerRunnable<Result>(new Callable<Result>(){
            @Override
            /**
             * Returns result after executing doInBackground() and posting result to calling thread
             * @see java.util.concurrent.Callable#call()
             */
            public Result call() throws Exception {
                return postResult(doInBackground());
            }});
        // The FutureTask implementation - overrides abstract method done()
        future = createFutureTask(worker); // future executes in execute() method
        // Work status PENDING, RUNNING, FINISHED, FAILED
        status = WorkStatus.PENDING;
    }

    /**
     * Override this method to perform a computation on a background thread. Any
     * required parameters will need to be provided as fields belonging to the sub class.
     * @return A result, defined by the subclass of this task.
     *
     * @see #onPreExecute()
     * @see #onPostExecute(Object)
     */
    public abstract Result doInBackground();
    
    /**
     * Runs on the UI thread before {@link #doInBackground()}.
     *
     * @see #onPostExecute(Object)
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
    public void onPostExecute(Result result) 
    {
    }

    /**
     * Executes the worker task. Any required parameters will need to be provided as fields 
     * belonging to the sub class. 
     * The task returns itself (this) so that the caller can keep a reference to it.
     * @return This instance of WorkerTask.
     * @throws IllegalStateException If status is either
     *         {@link WorkStatus#RUNNING} or {@link WorkStatus#FINISHED}.
     */
    public final WorkerTask<Result> execute() 
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

        onPreExecute();
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
      * @see #onCancelled(Object) 
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
         return future.isCancelled();
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
     * Runs on separate thread after {@link #cancel(boolean)} is invoked. 
     *
     *
     * @param result The result of the operation computed by {@link #doInBackground()}.
     *
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    public void onCancelled(Result result) 
    {
    }

    /**
     * Post result to calling thread on completion
     * @see au.com.cybersearch2.classytask.ThreadMessenger#sendResult(java.lang.Object)
     */
    public void sendResult(final Result result)
    {
        new Thread(new Runnable(){
            @Override
            public void run() {
                finish(result);
            }}).start();
    }

    /**
     * Post result to calling thread on cancel
     * @see au.com.cybersearch2.classytask.ThreadMessenger#sendCancel(java.lang.Object)
     */
    public void sendCancel(final Result result)
    {
        new Thread(new Runnable(){
            @Override
            public void run() {
                onCancelled(result);
            }}).start();
    }

    /**
     * Returns exception thrown when the worker task fails due to a uncaught RuntimeException
     * @return ExecutionException or null if work task ran to completion normally.
     */
    public ExecutionException getExecutionException()
    {
        return worker.getExecutionException();
    }

    /**
     * Process signalled result after task has run
     * @param result Object of generic type Result or null if task cancelled before result available
     */
    protected void finish(Result result) 
    {
        if (isCancelled())
            onCancelled(result);
        else
            onPostExecute(result);
        // Final status will be FINISHED or FAILED
        if (status != WorkStatus.FAILED)
            status = WorkStatus.FINISHED;
    }

    /**
     * Post result to calling thread
     * @param result Object of generic type Result
     * @return Object of generic type Result or null if task cancelled before result available
     */
    protected Result postResult(Result result) 
    {
        if (future.isCancelled())
            sendCancel(result);
        else
            sendResult(result);
        return result;
    }

    /**
     * Returns Concurrent worker
     * @param worker
     * @return FutureTask&lt;Result&gt;
     */
    protected FutureTask<Result> createFutureTask(
            final WorkerRunnable<Result> worker)
    {
        return new FutureTask<Result>(worker) 
        {
            @Override
            protected void done() 
            {
                Result result = null;
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
                    worker.setExecutionException(e);
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
                if (worker.wasTaskInvoked())
                    postResult(result);
            }
        };
    }

}