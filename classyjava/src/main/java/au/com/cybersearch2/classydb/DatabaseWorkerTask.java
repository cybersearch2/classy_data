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

import java.util.concurrent.ExecutionException;

import javax.persistence.EntityTransaction;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkerTask;


/**
 * DatabaseWorkerTask
 * Task which executes SQL statements contained in files accessed through the ResourceEnvironment
 * injected class member. The Boolean task result indicates success or failure. The errorMessage
 * variable contains the error summary. Error details are logged. 
 * onPostExecute() notifies waiting threads of task completion.
 * This class is abstract as method doInBackground() needs to be defined. This method is expected to call
 * executeSql().
 * @author Andrew Bowley
 * 15/06/2014
 */
public class DatabaseWorkerTask
{
    public class TaskImpl extends WorkerTask<Void>
    {
        protected DatabaseWork databaseWork;
        
        public TaskImpl(DatabaseWork databaseWork)
        {
            super();
            this.databaseWork = databaseWork;
        }
        
        @Override
        public Void doInBackground() 
        {
            EntityTransaction transaction = databaseWork.getTransaction();
            transaction.begin();
            transaction.commit();
            return null;
        }
        
        @Override
        protected void finish(Void x) 
        {
            super.finish(x);
            notifyTaskCompleted();
        }

        @Override
        public void onPostExecute(Void x) 
        {
            Throwable rollbackException = null;
            ExecutionException executionException = getExecutionException();
            if (executionException != null)
            {
                EntityTransaction transaction = databaseWork.getTransaction();
                if ((transaction != null) && transaction.isActive())
                    transaction.rollback();
                rollbackException = executionException.getCause();
            }
            Boolean success = databaseWork.getResult();
            if (rollbackException != null)
            {
                success = Boolean.valueOf(false);
                databaseWork.onRollback(rollbackException);
            }
            else
                databaseWork.onPostExecute(success);
            if (success)
                status = WorkStatus.FINISHED;
            else
                status = WorkStatus.FAILED;
            signalResult(success);
        }

        @Override
        public void onCancelled(Void x) 
        {
            onPostExecute(x);
            notifyTaskCompleted();
        }

        protected void notifyTaskCompleted() 
        {
            // Notify waiting threads at very last point of exit
            synchronized(this)
            {
                this.notifyAll();
            }
        }
}
    
    private static final String TAG = "DatabaseWorkerTask";
    static Log log = JavaLogger.getLogger(TAG);
    /** Task result - success or not */
    protected boolean success;

    /**
     * On post-execute, save result and notify waiting thread(s)
     * @param result Success or not
     * @see au.com.cybersearch2.classytask.WorkerTask#onPostExecute(java.lang.Object)
     */
    public void signalResult(Boolean result) 
    {
        success = result;
    }

    /**
     * Returns result
     * @return boolean
     */
    public boolean isSuccess() 
    {
        return success;
    }

    public Executable executeTask(DatabaseWork databaseWork)
    {
        TaskImpl taskImpl = new TaskImpl(databaseWork);
        taskImpl.execute();
        return taskImpl;
    }
    
}
