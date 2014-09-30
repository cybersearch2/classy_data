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

import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import android.database.sqlite.SQLiteDatabase;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

import com.j256.ormlite.support.ConnectionSource;

/**
 * ClassyOpenHelperCallbacks
 * Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods
 * @author Andrew Bowley
 * 24/06/2014
 */
public class ClassyOpenHelperCallbacks implements OpenHelperCallbacks
{
    protected DatabaseAdmin databaseAdmin;
    protected PersistenceAdmin persistenceAdmin;
    
    @Inject PersistenceFactory persistenceFactory;
    
    /**
     * Create ClassyOpenHelperCallbacks object
     * @param puName Persistence Unit name
     */
    public ClassyOpenHelperCallbacks(String puName)
    {
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
     * @param database
     *            Database being created.
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(SQLiteDatabase database,
            ConnectionSource connectionSource) 
    {
        // Should call databaseAdmin.onCreate(), but it does not work in this context 
        // because multi-threading not supported
        Properties properties = persistenceAdmin.getProperties();
        // Get SQL script file names from persistence.xml properties
        // A filename may be null if operation not supported
        String schemaFilename = properties.getProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME);
        String dropSchemaFilename = properties.getProperty(DatabaseAdmin.SCHEMA_FILENAME);
        String dataFilename = properties.getProperty(DatabaseAdmin.DATA_FILENAME);
        DatabaseWork processFilesCallable = new NativeScriptDatabaseWork(connectionSource, schemaFilename, dropSchemaFilename, dataFilename); 
        try
        {
            processFilesCallable.doInBackground(connectionSource.getReadWriteConnection());
        }
        catch (SQLException e)
        {
            throw new PersistenceException(e);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
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
     * @param database
     *            Database being upgraded.
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database,
            ConnectionSource connectionSource, int oldVersion,
            int newVersion) 
    {
        //databaseAdmin.onUpgrade(oldVersion, newVersion);
        // No special arrangements for upgrade by default. Override this class for custom upgrade
        onCreate(database, connectionSource);
    }
}
