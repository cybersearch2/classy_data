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

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext.EntityManagerProvider;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

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
    /** Flag set if executes asynchronously (default = false if only single connection ) */
    protected boolean async;
    /** PersistenceUnitAdmin Unit name */
    protected String puName;

    /**
     * Create PersistenceContainer object 
     * @param puName PersistenceUnitAdmin Unit name
     * @param async Flag set if executes asynchronously 
     */
    public PersistenceContainer(PersistenceContext persistenceContext, String puName, boolean async)
    {
        this.puName = puName;
        /** Reference PersistenceUnitAdmin Unit specified by name to extract EntityManagerFactory object */
        PersistenceAdmin persistenceAdmin = persistenceContext.getPersistenceAdmin(puName);
        if (persistenceAdmin == null) 
		{
			throw new PersistenceException("Persistence Unit \"" + puName + "\" is invalid");
		}
        if (async && persistenceAdmin.isSingleConnection())
        	async = false;
        this.async = async;
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
     * Returns object which creates a persistence context and executes a task in that contex
     * @param persistenceWork
     * @return JavaPersistenceContext
     */
    public JavaPersistenceContext getPersistenceTask(PersistenceWork persistenceWork)
    {
        JavaPersistenceContext jpaContext = 
            new JavaPersistenceContext(persistenceWork, new EntityManagerProvider(){

                @Override
                public EntityManagerLite entityManagerInstance()
                {
                    return entityManagerFactory.createEntityManager();
                }}); 
        jpaContext.getTransactionInfo().setUserTransaction(isUserTransactionMode);
    	return jpaContext;
    }
    
}
