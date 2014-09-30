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
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.transaction.TransactionInfo;
import au.com.cybersearch2.classyjpa.transaction.UserTransactionSupport;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;

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
    /** Task to run background thread and notify caller of outcome */
    public class PersistenceTaskImpl extends TaskBase
    {
        public PersistenceTaskImpl(PersistenceWork persistenceWork)
        {
            super(persistenceWork);
        }
        
        @Override
        public Boolean doInBackground() 
        {
            return executeInBackground(persistenceWork, transactionInfo);
        }
    }

    private static final String TAG = "PersistenceContainer";
    static Log log = JavaLogger.getLogger(TAG);
    /** Flag to indicate user transaction. If false, then only transaction method supported is setRollbackOnly() */
    protected volatile boolean isUserTransactionMode;
    /** JPA EntityManager "lite" factory ie. only API v1 supported. */
    protected EntityManagerLiteFactory entityManagerFactory;
    /** Object which provides access to full persistence implementation */
    @Inject PersistenceFactory persistenceFactory;

    /**
     * Create PersistenceContainer object 
     * @param puName Persistence Unit name
     */
    public PersistenceContainer(String puName)
    {
        DI.inject(this);
        /** Reference Persistence Unit specified by name to extract EntityManagerFactory object */
        Persistence persistence = persistenceFactory.getPersistenceUnit(puName);
        entityManagerFactory = persistence.getPersistenceAdmin().getEntityManagerFactory();
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
    public Executable executeTask(PersistenceWork persistenceWork)
    {
        TaskBase taskBase = new PersistenceTaskImpl(persistenceWork);
        taskBase.getTransactionInfo().setUserTransaction(isUserTransactionMode);
        taskBase.execute();
        return taskBase;
    }

    /**
     * Execute persistence work in background thread
     * @param persistenceWork Object specifying unit of work
     * @param transactionInfo Enclosing transaction and associated information
     * @return Boolean result - TRUE = success, FALSE = failure/rollback 
     *          or null if exception thrown on transaction begin() called.
     */
    protected Boolean executeInBackground(PersistenceWork persistenceWork, TransactionInfo transactionInfo)
    {
        // Use UserTransactionSupport interface to safely set user transaction mode
        UserTransactionSupport userTransactionSupport = null;
        EntityManagerLite entityManager = entityManagerFactory.createEntityManager();
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
            persistenceWork.doInBackground(entityManager);
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
                if (success && transaction.getRollbackOnly())
                    setRollbackOnly = true;
                entityManager.close();
            } // Persistence exception may be thrown on commit or rollback. Just log it.
            catch (PersistenceException e)
            {   // Ensure onRollback() is called on PersistenceWork object
                if (rollbackException == null)
                    transactionInfo.setRollbackException(e);
                log.error(TAG, "Persistence error on commit", e);
            }
        }
        return setRollbackOnly;
    }
    
}
