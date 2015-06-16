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

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

/**
 * LoaderTaskImpl
 * AsyncTaskLoader implementation which executes persistence work
 * @author Andrew Bowley
 * 04/09/2014
 */
public class PersistenceLoader
{
	class LoaderImpl extends AsyncTaskLoader<Boolean> implements OnLoadCompleteListener<Boolean>
	{
	    /** Persistence task */
	    protected PersistenceTaskImpl persistenceTask;
	    /** Object to track work status and notify completion */
	    protected WorkTracker workTracker;
	    /** Persistence unit object to perform persistence work */
	    protected String persistenceUnit;

	    public LoaderImpl(String persistenceUnit, PersistenceWork persistenceWork)
	    {
	        super(context);
	        this.persistenceUnit = persistenceUnit;
	        PersistenceContainer persistenceContainer = new PersistenceContainer(persistenceUnit, false);
	        if (isUserTransactionMode)
	        	persistenceContainer.setUserTransactionMode(true);
	        persistenceTask = persistenceContainer.getPersistenceTask(persistenceWork);
	        workTracker = new WorkTracker();
	        // Register self as onLoadCompleteListener
	        registerListener(1, this);
	    }
	    
	    /**
	     * Execute persistence work in  background thread
	     * Called on a worker thread to perform the actual load. 
	     * @return Boolean object - Boolean.TRUE indicates successful result
	     */
	    @Override
	    public Boolean loadInBackground() 
	    {
	        Boolean success =  persistenceTask.doInBackground();
	        workTracker.setStatus(success ? WorkStatus.RUNNING : WorkStatus.FAILED);
	        return success;
	    }

	    /**
	     * Starts an asynchronous load of the Loader's data. When the result
	     * is ready the callbacks will be called on the process's main thread.
	     * If a previous load has been completed and is still valid
	     * the result may be passed to the callbacks immediately.
	     * The loader will monitor the source of
	     * the data set and may deliver future callbacks if the source changes.
	     * <p>Must be called from the process's main thread.
	     */
	    @Override
	    protected void onStartLoading() 
	    {
	        forceLoad();
	    }

	    /**
	     * Handle load complete on calling thread
	     * @param loader the loader that completed the load
	     * @param success Boolean object - Boolean.TRUE indicates successful result
	     */
	    @Override
	    public void onLoadComplete(Loader<Boolean> loader, Boolean success) 
	    {
	    	persistenceTask.onPostExecute(success);
	    	if (workTracker.getStatus() != WorkStatus.FAILED)
	    		workTracker.setStatus(success ? WorkStatus.FINISHED : WorkStatus.FAILED);
	        // Notify waiting threads at very last point of exit
	        synchronized(workTracker)
	        {
	            workTracker.notifyAll();
	        }
	    }
	    
	    /**
	     * Execute persistence work. To be run on calling thread.
	     */
	    public Executable execute() 
	    {
	        startLoading();
	        return workTracker;
	    }

    }
	
    //private static final String TAG = "PersistenceLoader";
    //private Log log = JavaLogger.getLogger(TAG);

    /** Flag to indicate user transaction. If false, then only transaction method supported is setRollbackOnly() */
    protected volatile boolean isUserTransactionMode;
    /** Android Application Context */
    protected Context context;

    /**
     * Create PersistenceLoader object
     */
    public PersistenceLoader()
    {
    	this(new ApplicationContext().getContext());
    }

    /**
     * Create PersistenceLoader object
     * @param context Android Application Context
     */
    public PersistenceLoader(Context context)
    {
    	this.context = context;
    }


	/**
     * Set user transaction mode. The transaction is accessed by calling EntityManager getTransaction() method.
     * @param value boolean
     */
    public void setUserTransactionMode(boolean value)
    {
        isUserTransactionMode = value;
    }
 
    
    /**
     * Execute persistence work. To be run on calling thread.
     */
    public Executable execute(String persistenceUnit, PersistenceWork persistenceWork) 
    {
    	LoaderImpl LoaderImpl = new LoaderImpl(persistenceUnit, persistenceWork);
        return LoaderImpl.execute();
    }

}
