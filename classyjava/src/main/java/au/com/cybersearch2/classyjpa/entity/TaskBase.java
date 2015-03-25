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
package au.com.cybersearch2.classyjpa.entity;

import au.com.cybersearch2.classyjpa.transaction.TransactionInfo;
//import au.com.cybersearch2.classylog.JavaLogger;
//import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.WorkerTask;

/**
 * TaskBase
 * Implements most of the details to execute a peristence WorkerTask. To complete, override base class method executeInBackground().
 * @author Andrew Bowley
 * 19/08/2014
 */
public class TaskBase extends WorkerTask<Boolean>
{
    //private static final String TAG = "TaskBase";
    //private Log log = JavaLogger.getLogger(TAG);
    
    /** Task to be performed */
    protected PersistenceTaskImpl persistenceTask;

    /**
     * Constructor
     * @param persistenceWork Object containing work to be performed
     */
    public TaskBase(PersistenceTaskImpl persistenceTask)
    {
        this.persistenceTask = persistenceTask;
    }
 
    /**
     * Process signalled result after task has run
     * @param result Boolean TRUE or FALSE or null if task cancelled before result available
     */
    @Override
    protected void finish(Boolean result) 
    {
        super.finish(result);
        // Notify waiting threads at very last point of exit
        notifyTaskCompleted();
    }

    /**
     * Process signalled result after task has run
     * @param success Boolean TRUE or FALSE or null if task cancelled before result available
     */
    @Override
    public void onPostExecute(Boolean success) 
    {
    	persistenceTask.setExecutionException(getExecutionException());
    	persistenceTask.onPostExecute(success);
    	status = persistenceTask.getWorkStatus();
    }

    /**
     * Process signalled result after task has been cancelled. 
     * NOTE: Interruption of the running thread is not permitted, so same as non-cancel case.
     * @param success Boolean TRUE or FALSE or null if task cancelled before result available
     */
    @Override
    public void onCancelled(Boolean success) 
    {
    	persistenceTask.onPostExecute(success);
        notifyTaskCompleted();
    }

    /**
     * Returns transaction information
     * @return TransactionInfo
     */
    public TransactionInfo getTransactionInfo()
    {
        return persistenceTask.getTransactionInfo();
    }
 
    /**
     * Notify waiting threads after post-execute completed
     */
    protected void notifyTaskCompleted() 
    {
        synchronized(this)
        {
            this.notifyAll();
        }
    }

	@Override
	public Boolean doInBackground() 
	{
		return persistenceTask.doInBackground();
	}
}
