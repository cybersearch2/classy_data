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
package au.com.cybersearch2.classytask;

import java.util.logging.Level;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * WorkTracker
 * WorkStatus accessor
 * @author Andrew Bowley
 * 29/06/2014
 */
public class WorkTracker extends Executable
{
    private static final String TAG = "WorkTracker";
    private static Log log = JavaLogger.getLogger(TAG);

    /** Wait one minute maximum for tasks to complete */
    public final static int MAX_TASK_WAIT_SECS = 60;
    
    /** Status: PENDING, RUNNING, FINISHED or FAILED */
    protected volatile WorkStatus workStatus;
    /** Task name. Defaults to current Thread name */
    protected String taskName;
    
    /**
     * Create WorkTracker object
     */
    public WorkTracker()
    {
        workStatus = WorkStatus.PENDING;
        taskName = Thread.currentThread().getName();
    }

    /**
     * Create WorkTracker object
     * @param taskName
     */
    public WorkTracker(String taskName)
    {
        workStatus = WorkStatus.PENDING;
        this.taskName = taskName;
    }

    /**
     * Returns current work status
     * @see au.com.cybersearch2.classytask.Executable#getStatus()
     */
    @Override
    public WorkStatus getStatus() 
    {
        return workStatus;
    }

    /**
     * Set work status
     * @param status WorkStatus
     */
    public void setStatus(WorkStatus status)
    {
        workStatus = status;
    }

    /**
     * Wait for currently executing persistence unit task to complete
     * @param workTracker WorkTracker object used to monitor task
     * @return WorkStatus Status value will be FINISHED or FAILED
     */
    public WorkStatus waitForTask(WorkTracker workTracker)
    {
        return waitForTask(workTracker, MAX_TASK_WAIT_SECS); 
    }
    
    /**
     * Wait up to specified number of seconds for currently executing persistence unit task to complete
     * @param workTracker WorkTracker object used to monitor task
     * @param timeoutSecs int
     * @return WorkStatus Status value will be FINISHED or FAILED
     */
    public WorkStatus waitForTask(WorkTracker workTracker, int timeoutSecs) 
    {
        if (timeoutSecs < 0)
            timeoutSecs = MAX_TASK_WAIT_SECS;
        WorkStatus workStatus = WorkStatus.PENDING;
        if (workTracker.getStatus() == WorkStatus.RUNNING)
        {
            if ((timeoutSecs == 0) && !log.isLoggable(TAG, Level.FINE))
               // Only allow indeterminate wait for debugging
                timeoutSecs = MAX_TASK_WAIT_SECS;
            if (timeoutSecs == 0)
                synchronized(workTracker)
                {
                    try
                    {
                    	workTracker.wait();
                    }
                    catch (InterruptedException e)
                    {
                        log.warn(TAG, taskName + " interrupted", e);
                    }
                }
            else
                waitTicks(workTracker, timeoutSecs);
            workStatus = workTracker.getStatus();
        }
        return workStatus;
    }

    /**
     * Wait specified number of seconds for task to complte
     *@param task Executable tracking Database work
     *@param timeoutSecs int
     */
    private void waitTicks(Executable task, int timeoutSecs) 
    {
        int tick = 0;
        while (tick < timeoutSecs)
        {
            synchronized(task)
            {
                try
                {
                    task.wait(1000);
                }
                catch (InterruptedException e)
                {
                    log.warn(TAG, "createDatabaseTask interrupted", e);
                    return;
                }
            }
            if (task.getStatus() != WorkStatus.RUNNING)
                break;
            ++tick;
        }
        if (tick == MAX_TASK_WAIT_SECS)
        {
            log.warn(TAG, "Task \"" + taskName + "\" taking more than " + MAX_TASK_WAIT_SECS + " seconds to complete");
        }
    }

}
