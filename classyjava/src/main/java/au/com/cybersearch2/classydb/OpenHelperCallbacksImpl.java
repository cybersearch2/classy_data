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

import javax.inject.Inject;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.transaction.UserTransactionSupport;

import com.j256.ormlite.support.ConnectionSource;

/**
 * ClassyOpenHelperCallbacks
 * Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods
 * @author Andrew Bowley
 * 24/06/2014
 */
public class OpenHelperCallbacksImpl implements OpenHelperCallbacks
{
    protected DatabaseAdmin databaseAdmin;
    protected PersistenceAdmin persistenceAdmin;
    protected String puName;
    
    @Inject PersistenceFactory persistenceFactory;
    
    /**
     * Create ClassyOpenHelperCallbacks object
     * @param puName Persistence Unit name
     */
    public OpenHelperCallbacksImpl(String puName)
    {
    	this.puName = puName;
        DI.inject(this);
        Persistence persistence = persistenceFactory.getPersistenceUnit(puName);
        databaseAdmin = persistence.getDatabaseAdmin();
        persistenceAdmin = persistence.getPersistenceAdmin();
    }

    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        databaseAdmin.onCreate(connectionSource);
        databaseAdmin.waitForTask();
    }

    /**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(
            ConnectionSource connectionSource, int oldVersion,
            int newVersion) 
    {
    	databaseAdmin.onUpgrade(connectionSource, oldVersion, newVersion);
        databaseAdmin.waitForTask();
    }

    /**
     * Execute persistence work in background thread
     * @param connectionSource Open ConnectionSource object
     * @param persistenceWork Object specifying unit of work
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
        	throw new PersistenceException(puName + " Persistence Task failed in onCreate()", rollbackException);
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
                //log.error(TAG, "Persistence error on commit", e);
            }
        }
        return setRollbackOnly;
    }
}
