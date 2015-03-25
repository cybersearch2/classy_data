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

import javax.inject.Inject;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.transaction.TransactionInfo;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * PersistenceContainer
 * Provides task-scoped persistence context with automatic rollback if a fatal exception occurs.
 * The unit of work is passed as a PersistenceWork object which handles one of 3 outcomes: success, failure and rollback.
 * Failure is intended for when pre-conditions are not satisfied. If failure occurs after changes have be made, then rollback should be invoked.
 * @author Andrew Bowley
 * 27/06/2014
 */
public class PersistenceContainer
{

    private static final String TAG = "PersistenceContainer";
    static Log log = JavaLogger.getLogger(TAG);
    /** Flag to indicate user transaction. If false, then only transaction method supported is setRollbackOnly() */
    protected volatile boolean isUserTransactionMode;
    /** JPA EntityManager "lite" factory ie. only API v1 supported. */
    protected EntityManagerLiteFactory entityManagerFactory;
    boolean singleConnection;
    protected ConnectionSource connectionSource;
    
    String puName;
    /** Object which provides access to full persistence implementation */
    @Inject PersistenceFactory persistenceFactory;

    /**
     * Create PersistenceContainer object 
     * @param puName Persistence Unit name
     */
    public PersistenceContainer(String puName)
    {
        this.puName = puName;
        DI.inject(this);
        /** Reference Persistence Unit specified by name to extract EntityManagerFactory object */
        Persistence persistence = persistenceFactory.getPersistenceUnit(puName);
        PersistenceAdmin persistenceAdmin = persistence.getPersistenceAdmin();
        singleConnection = persistenceAdmin.isSingleConnection();
        entityManagerFactory = persistenceAdmin.getEntityManagerFactory();
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
     * Commence execution of work in perisistence context. Wait on returned object for notification of task complete.
     * @param persistenceWork Object specifying unit of work
     * @return Executable. 
     */
    public Executable executeTask(final PersistenceWork persistenceWork)
    {
    	final PersistenceTaskImpl persistenceTask = new PersistenceTaskImpl(persistenceWork, entityManagerFactory);
    	persistenceTask.getTransactionInfo().setUserTransaction(isUserTransactionMode);
        if (singleConnection)
        {
        	final Executable exe = new Executable()
    		{
    			@Override
    			public WorkStatus getStatus() 
    			{
    				return persistenceTask.status;
    			}
    		};
        	final Boolean[] success =  { Boolean.FALSE };
    		success[0] = persistenceTask.executeInProcess(exe);
        	return exe;
        }
        else
        {
        	TaskBase  task = new TaskBase(persistenceTask);
        	task.execute();
        	return task;
        }
    }

    public PersistenceTaskImpl getPersistenceTask(final PersistenceWork persistenceWork)
    {
    	final PersistenceTaskImpl persistenceTask = new PersistenceTaskImpl(persistenceWork, entityManagerFactory);
    	persistenceTask.getTransactionInfo().setUserTransaction(isUserTransactionMode);
    	return persistenceTask;
    }
    
    public PersistenceTaskImpl getPersistenceTask(final PersistenceWork persistenceWork, TransactionInfo transactionInfo)
    {
    	final PersistenceTaskImpl persistenceTask = new PersistenceTaskImpl(persistenceWork, entityManagerFactory, transactionInfo);
    	persistenceTask.getTransactionInfo().setUserTransaction(isUserTransactionMode);
    	return persistenceTask;
    }

	public Boolean executeInBackground(PersistenceWork persistenceWork,
			TransactionInfo transactionInfo) 
	{
		getPersistenceTask(persistenceWork, transactionInfo).executeInBackground(); 
		return null;
	}
}
