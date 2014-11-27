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

import au.com.cybersearch2.classyjpa.entity.InProcessPersistenceContainer;

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
 * @see InProcessPersistenceContainer
 */
public class OpenHelperCallbacksImpl extends InProcessPersistenceContainer implements OpenHelperCallbacks
{
    
    /**
     * Create ClassyOpenHelperCallbacks object
     * @param puName Persistence Unit name
     */
    public OpenHelperCallbacksImpl(String puName)
    {
    	super(puName);
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
    	databaseAdmin.onUpgrade(connectionSource, oldVersion, newVersion);
    }

}
