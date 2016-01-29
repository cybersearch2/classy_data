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

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.JavaPersistenceContext.EntityManagerProvider;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classytask.Executable;


import com.j256.ormlite.support.ConnectionSource;

/**
 * ClassyOpenHelperCallbacks
 * Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods.
 * This is a persistence container in which tasks can be performed using a supplied
 * EntityManager object by calling doWork(). Unlike other PersistenceContainer 
 * implementations, the execution takes place in the current thread using supplied
 * ConnectionSource to support ORMLite's special connection implementation. Any
 * RuntimeException will be forwarded to the caller.
 * 
 * @author Andrew Bowley
 * 24/06/2014
 */
public class OpenHelperCallbacksImpl implements OpenHelperCallbacks
{
    protected DatabaseAdmin databaseAdmin;
    protected PersistenceAdmin persistenceAdmin;
    
    /**
     * Create ClassyOpenHelperCallbacks object
     * @param puName PersistenceUnitAdmin Unit name
     */
    public OpenHelperCallbacksImpl(String puName)
    {
    }

    public void setDatabaseAdmin(DatabaseAdmin databaseAdmin)
    {
        this.databaseAdmin = databaseAdmin;
    }

    public void setPersistenceAdmin(PersistenceAdmin persistenceAdmin)
    {
        this.persistenceAdmin = persistenceAdmin;
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
        if (databaseAdmin != null)
            databaseAdmin.onCreate(connectionSource);
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
            ConnectionSource connectionSource, 
            int oldVersion,
            int newVersion) 
    {
        if (databaseAdmin != null)
    	    databaseAdmin.onUpgrade(connectionSource, oldVersion, newVersion);
    }

    /**
     * Execute persistence work in same thread as caller
     * @param connectionSource Open ConnectionSource object
     * @param persistenceTask Object specifying unit of work
     * @return Executable object to track task status
     */
    protected Executable doWork(final ConnectionSource connectionSource, final PersistenceTask persistenceTask)
    {
        if (persistenceAdmin == null)
            throw new IllegalStateException(getClass().getName() + " not initialized");
        // PersistenceUnitAdmin work required for JavaPersistenceContext, but only doTask() is relevant
        // as work is performed on caller's thread
        PersistenceWork persistenceWork = new PersistenceWork(){

            @Override
            public void doTask(EntityManagerLite entityManager)
            {
                persistenceTask.doTask(entityManager);
            }

            @Override
            public void onPostExecute(boolean success)
            {
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
            }
        };
        JavaPersistenceContext jpaContext = new JavaPersistenceContext(persistenceWork, new EntityManagerProvider(){

            @Override
            public EntityManagerLite entityManagerInstance()
            {
                return persistenceAdmin.createEntityManager(connectionSource);
            }
        }); 
        return jpaContext.executeInProcess();
    }
}
