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

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.Loader.OnLoadCompleteListener;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
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
	    /** Work to be performed */
	    protected PersistenceWork persistenceWork;
	    /** Object to track work status and notify completion */
	    protected WorkTracker workTracker;
	    /** RuntimeException, if thrown, which caused task to terminate unexpectedly */
	    protected Throwable uncaughtException;
	    protected String persistenceUnit;

	    public LoaderImpl(String persistenceUnit, PersistenceWork persistenceWork)
	    {
	        super(context);
	        this.persistenceUnit = persistenceUnit;
	        this.persistenceWork = persistenceWork;
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
	        // Hook into UncaughtExceptionHandler chain 
	        final UncaughtExceptionHandler chain = Thread.getDefaultUncaughtExceptionHandler();
	        Thread.setDefaultUncaughtExceptionHandler(
	                new Thread.UncaughtExceptionHandler() {

	            @Override
	            public void uncaughtException(Thread t, Throwable uncaughtException) 
	            {   // Save exception for post-execution error handling
	            	LoaderImpl.this.uncaughtException = uncaughtException;
	                chain.uncaughtException(t, uncaughtException);
	            }
	        });
	        PersistenceContainer persistenceContainer = new PersistenceContainer(persistenceUnit, false);
	        if (isUserTransactionMode)
	        	persistenceContainer.setUserTransactionMode(true);
	        workTracker.setStatus(persistenceContainer.executeTask(persistenceWork).getStatus());
	        return workTracker.getStatus() == WorkStatus.FINISHED;
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
	        if (uncaughtException != null)
	            log.error(TAG, "Persistence container rolled back transaction", uncaughtException);
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
	
    private static final String TAG = "PersistenceLoader";
    private Log log = JavaLogger.getLogger(TAG);

    /** Flag to indicate user transaction. If false, then only transaction method supported is setRollbackOnly() */
    protected volatile boolean isUserTransactionMode;
    protected Context context;

    /**
     * Create LoaderTaskImpl object
     * @param context Android Application Context
     * @param persistenceContainer Persistence container in which to execute persistence work
     * @param persistenceWork Persistence work object
     */
    public PersistenceLoader()
    {
    	this(new ApplicationContext().getContext());
    }

    /**
     * Create LoaderTaskImpl object
     * @param context Android Application Context
     * @param persistenceContainer Persistence container in which to execute persistence work
     * @param persistenceWork Persistence work object
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
