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

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.transaction.UserTransactionSupport;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.support.ConnectionSource;

/**
 * InProcessPersistenceContainer
 * @author Andrew Bowley
 * 25 Nov 2014
 */
public class InProcessPersistenceContainer 
{
    private static final String TAG = "InProcessPersistenceContainer";
    static Log log = JavaLogger.getLogger(TAG);
    
    protected PersistenceAdmin persistenceAdmin;
    protected DatabaseAdmin databaseAdmin;
    protected String puName;
    
    @Inject PersistenceFactory persistenceFactory;

	public InProcessPersistenceContainer(String puName)
	{
    	this.puName = puName;
        DI.inject(this);
        Persistence persistence = persistenceFactory.getPersistenceUnit(puName);
        persistenceAdmin = persistence.getPersistenceAdmin();
        databaseAdmin = persistence.getDatabaseAdmin();
	}
	
    /**
     * Execute persistence work in same thread as caller
     * @param connectionSource Open ConnectionSource object
     * @param persistenceTask Object specifying unit of work
     */
    protected void doWork(ConnectionSource connectionSource, PersistenceTask persistenceTask)
    {
 		EntityManagerLite entityManager = persistenceAdmin.createEntityManager(connectionSource);
 		((UserTransactionSupport)entityManager).setUserTransaction(true);
 		EntityTransaction transaction = entityManager.getTransaction();
 		((UserTransactionSupport)entityManager).setUserTransaction(false);
        // The container manages the transaction, so begin before work starts
        transaction.begin();
        // Commence work, ready to catch RuntimeExceptions declared to be throwable by EntityManager API
        Throwable rollbackException = null;
        boolean success = false; // Use flag for indicating unexpected RuntimeException
        boolean setRollbackOnly = false;
        try
        {
            persistenceTask.doTask(entityManager);
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
            setRollbackOnly = resolveOutcome(entityManager, transaction, success, rollbackException);
        }
        if (!success && !setRollbackOnly)
        	throw new PersistenceException(puName + " Persistence Task failed", rollbackException);
    }
 
    /**
     * Resolve outcome of persistence work given all relevant parameters. 
     * Includes Commit/rollback by calling EntityManager close(), unless unexpected exception has occurred.
     *@param entityManager Open EntityManager object  
     *@param transaction EntityTransaction
     *@param success boolean
     *@param rollbackException Optional RuntimeException thrown by EntityManager object
     *@return boolean - If rollback, then true
     */
    protected boolean resolveOutcome(
            EntityManagerLite entityManager, 
            EntityTransaction transaction, 
            boolean success, 
            Throwable rollbackException)
    {
        // Flag for do rollback
        boolean setRollbackOnly = false;
        if (!success && (rollbackException == null))
        { // Unexpected exception thrown. Just rollback.
            if (transaction.isActive())
                transaction.rollback();
        }
        else
        {
            if (rollbackException != null)
            {   // RuntimeException caught, so do rollback  
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
                log.error(TAG, "Persistence error on commit", e);
            }
        }
        return setRollbackOnly;
    }

}
