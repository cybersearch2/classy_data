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

import java.util.concurrent.ExecutionException;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.transaction.TransactionInfo;
import au.com.cybersearch2.classyjpa.transaction.UserTransactionSupport;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * JavaPersistenceContext
 * Creates a persistence context and executes a task in that context. 
 * Allows for execution in process (synchronous) as well as in background thread (asynchronous). 
 * @author Andrew Bowley
 * 25 Mar 2015
 */
public class JavaPersistenceContext
{
    public interface EntityManagerProvider
    {
        EntityManagerLite entityManagerInstance();
    }
    
    private static final String TAG = "PersistenceTaskImpl";
    private Log log = JavaLogger.getLogger(TAG);
    
    /** Enclosing transaction and associated information */ 
    protected TransactionInfo transactionInfo;
    /** Work to be performed */
    protected PersistenceWork persistenceWork;
    /** JPA EntityManager "lite" provider hides special case where a reserved ConnectionSource must be employed */
    protected EntityManagerProvider entityManagerProvider;
    /** Unexpected RunTimeException caught in process execution */
    protected ExecutionException executionException;
    /** Execution status - final state will be FINISHED or FAILED */
    protected WorkStatus status;

    /**
     * Construct JavaPersistenceContext object
     * @param persistenceWork Work to be performed in Java PersistenceUnitAdmin context
     * @param entityManagerProvider EntityManager factory
     */
    public JavaPersistenceContext(PersistenceWork persistenceWork, EntityManagerProvider entityManagerProvider)
    {
        this.persistenceWork = persistenceWork;
        this.entityManagerProvider = entityManagerProvider;
        this.transactionInfo = new TransactionInfo();
        status = WorkStatus.PENDING;
    }

    /**
     * Returns transaction information
     * @return TransactionInfo object
     */
    public TransactionInfo getTransactionInfo()
    {
    	return transactionInfo;
    }
 
    /**
     * Returns execution status
     * @return WorkStatus object
     */
    public WorkStatus getWorkStatus()
    {
    	return status;
    }
 
    /**
     * Execute task in process 
     * @return Executable to track progress
     */
    public Executable executeInProcess()
    {
        final Executable exe = new Executable()
        {
            @Override
            public WorkStatus getStatus() 
            {
                return status;
            }
        };
    	final Boolean[] success = { Boolean.FALSE };
    	Runnable completeTaskRunnable = new Runnable(){

			@Override
			public void run() 
			{
				onPostExecute(success[0]); 
				if (success[0] == null)
					status = WorkStatus.FAILED;
				else
				    status = success[0] ? WorkStatus.FINISHED : WorkStatus.FAILED;
				// Notify owner of persistence work
				// when running in separate thread
				if (executionException != null)
				    synchronized(persistenceWork)
				    {
				        persistenceWork.notifyAll();
				    }
				else
	                synchronized(exe)
	                {
	                    exe.notifyAll();
	                }
			}
		};
		try
		{
    	    success[0] = doTask();
    	    completeTaskRunnable.run(); 
		}
		catch (RuntimeException e)
		{
			executionException = new ExecutionException(e);
			Thread completionThread = new Thread(completeTaskRunnable);
			status = WorkStatus.FAILED;
			completionThread.start();
		}
		finally
		{
		}
		if (executionException != null)
			throw (RuntimeException)executionException.getCause();
       return exe;
    }
    
    /**
     * Execute persistence work. 
     * @return Boolean result - TRUE = success, FALSE = failure/rollback 
     *          or null if exception thrown on transaction begin() called.
     */
    public Boolean doTask()
    {
        status = WorkStatus.RUNNING;
         // Use UserTransactionSupport interface to safely set user transaction mode
        UserTransactionSupport userTransactionSupport = null;
        EntityManagerLite entityManager = entityManagerProvider.entityManagerInstance();
        if (entityManager instanceof UserTransactionSupport)
        {
            userTransactionSupport = (UserTransactionSupport)entityManager;
            // Set user transaction true initially to obtain actual transaction object
            userTransactionSupport.setUserTransaction(true);
        }
        // Set transaction available to worker in transactionInfo object. 
        // This will only be a proxy if not in user transaction mode.
        EntityTransaction transaction = entityManager.getTransaction();
        transactionInfo.setEntityTransaction(transaction);
        // Now set actual enclosing transaction in transactionInfo object so it is available on commit
        if (transactionInfo.isUserTransaction())
        {
            if (userTransactionSupport == null)
                throw new PersistenceException("EntityManger does not support user transactions");
        }
        else
            try
            {   // Use container managed transaction. User can only request rollback.
                if (userTransactionSupport != null)
                    userTransactionSupport.setUserTransaction(false);
                 // The container manages the transaction, so begin before work starts
                transaction.begin();
            }
            catch (PersistenceException e)
            {
                transactionInfo.setRollbackException(e);
                // Return null as special value indicating work not commenced
                return null;
            }
        // Commence work, ready to catch RuntimeExceptions declared to be throwable by EntityManager API
        Throwable rollbackException = null;
        boolean success = false; // Use flag for indicating unexpected RuntimeException
        boolean setRollbackOnly = false;
        try
        {
            persistenceWork.doTask(entityManager);
            success = true;
        }
        catch (PersistenceException e)
        {
            rollbackException = e;
        }
        catch (IllegalArgumentException e)
        {
            rollbackException = e;
        }
        catch (IllegalStateException e)
        {
            rollbackException = e;
        }
        catch (UnsupportedOperationException e)
        {
            rollbackException = e;
        }
        // Other runtime exceptions are captured by the WorkerTask and reported onExecuteComplete()
        finally
        {
            setRollbackOnly = resolveOutcome(entityManager, userTransactionSupport, transactionInfo, success, rollbackException);
        }
        return success && !setRollbackOnly;
    }
 
    /**
     * Process signalled result after task has run
     * @param success Boolean TRUE or FALSE or null if task cancelled before result available
     */
    public void onPostExecute(Boolean success) 
    {
        // Check for uncaught exception causing background thread to abort
        if (executionException != null)
        {
            EntityTransaction transaction = transactionInfo.getTransaction();
            // Rollback transaction, if active
            if ((transaction != null) && transaction.isActive())
                transaction.rollback();
            // If rollback exception not already captured, set the uncaught exception as the rollback cause
            if (transactionInfo.getRollbackException() == null)
                transactionInfo.setRollbackException(executionException.getCause());

        }
        // Complete work based on final outcome: success/failure/rollback
        Throwable rollbackException = transactionInfo.getRollbackException();
        if ((rollbackException != null) || (success == null))
            success = Boolean.FALSE;
        if (rollbackException != null)
        {
            persistenceWork.onRollback(rollbackException);
            log.error(TAG, "PersistenceUnitAdmin container rolled back transaction", rollbackException);
        }
        else
            persistenceWork.onPostExecute(success);
        // Set final work status FINISHED/FAILED
        if (success)
            status = WorkStatus.FINISHED;
        else
            status = WorkStatus.FAILED;
    }
    /**
     * Resolve outcome of persistence work given all relevant parameters. 
     * Includes Commit/rollback by calling EntityManager close(), unless unexpected exception has occurred.
     *@param entityManager Open EntityManager object  
     *@param userTransactionSupport Optional implemention of user transaction in EntityManager object
     *@param transactionInfo Enclosing transaction and associated information
     *@param success boolean
     *@param rollbackException Optional RuntimeException thrown by EntityManager object
     *@return boolean - If rollback, then true
     */
    protected boolean resolveOutcome(
            EntityManagerLite entityManager, 
            UserTransactionSupport userTransactionSupport, 
            TransactionInfo transactionInfo, 
            boolean success, 
            Throwable rollbackException)
    {
        // Flag for do rollback
        boolean setRollbackOnly = false;
        // Set user transaction mode to access actual enclosing transaction
        if (userTransactionSupport != null)
            userTransactionSupport.setUserTransaction(true);
        EntityTransaction transaction = entityManager.getTransaction();
        if (!success && (rollbackException == null))
        { // Unexpected exception thrown. Just rollback.
            if (transaction.isActive())
                transaction.rollback();
        }
        else
        {
            if (rollbackException != null)
            {   // RuntimeException caught, so do rollback  
                transactionInfo.setRollbackException(rollbackException);
                transaction.setRollbackOnly();
            }
            try
            {
                if (success && transaction.isActive() && transaction.getRollbackOnly())
                    setRollbackOnly = true;
                entityManager.close();
            } // PersistenceUnitAdmin exception may be thrown on commit or rollback. Just log it.
            catch (PersistenceException e)
            {   // Ensure onRollback() is called on PersistenceWork object
                if (rollbackException == null)
                    transactionInfo.setRollbackException(e);
                setRollbackOnly = true;
                log.error(TAG, "PersistenceUnitAdmin error on commit", e);
            }
        }
        return setRollbackOnly;
    }

	public void setExecutionException(ExecutionException executionException) 
	{
		this.executionException = executionException;
	}
    

    
}
