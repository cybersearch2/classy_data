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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.persistence.PersistenceException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classybean.BeanException;
import au.com.cybersearch2.classybean.BeanUtil;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * AndroidConnectionSourceFactory
 * Connection management is delegated to AndroidConnectionSourceFactory
 * which also manages SQLiteOpenHelper objects
 * @author Andrew Bowley
 * 10/07/2014
 */
public class AndroidConnectionSourceFactory
{
    private static final String TAG = "AndroidConnectionSourceFactory";
    static Log log = JavaLogger.getLogger(TAG);
    /** Maps Persistence Unit name to a single connection associated with it */
    protected Map<String, OpenHelperConnectionSource> androidSQLiteMap;
    /** Android Application Context */
    protected ApplicationContext applicationContext;

    /**
     * Construct an AndroidConnectionSourceFactory instance
     */
    public AndroidConnectionSourceFactory()
    {
        androidSQLiteMap = new HashMap<String, OpenHelperConnectionSource>();
        applicationContext = new ApplicationContext();
    }

    /**
     * Return an AndroidSQLiteConnection object
     * @param databaseName
     * @param properties Properties defined in persistence unit
     */
    public OpenHelperConnectionSource getAndroidSQLiteConnection(final String databaseName, Properties properties)
    {
        if ((databaseName == null) || (databaseName.length() == 0))
            throw new IllegalArgumentException("Parameter \"databaseName\" is null or empty");
        if (properties == null)
            throw new IllegalArgumentException("Parameter \"properties\" is null");
        OpenHelperConnectionSource openHelperConnectionSource = androidSQLiteMap.get(databaseName);
        if (openHelperConnectionSource != null)
            return openHelperConnectionSource;
        // AndroidSQLiteConnection contains an OpenHelperCallbacks implementation, which can either be 
        // custom or default
        OpenHelperCallbacks openHelperCallbacks = null;
        // Property "open-helper-callbacks-classname"
        String openHelperCallbacksClassname = properties.getProperty(PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY);
        if (openHelperCallbacksClassname != null)
        {   // Custom
            try
            {
                openHelperCallbacks = (OpenHelperCallbacks) BeanUtil.newClassInstance(openHelperCallbacksClassname);
            }
            catch(BeanException e)
            {
                throw new PersistenceException(e.getMessage(), e.getCause());
            }
        }
        else
        {   
            // Default implementation requires persistence unit name, which is automatically inserted.
            // Property "persistence-unit-name" 
            String puName = properties.getProperty(PersistenceUnitInfoImpl.PU_NAME_PROPERTY);
            if (puName == null)
                throw new PersistenceException("Persistence property \"" + PersistenceUnitInfoImpl.PU_NAME_PROPERTY + "\" not set");
            openHelperCallbacks = new OpenHelperCallbacksImpl(puName);
        }
        int databaseVersion = PersistenceAdminImpl.getDatabaseVersion(properties);
        // AndroidSQLiteConnection also contains an SQLiteOpenHelper. 
        // The onCreate and onUpgrade overrides are delegated to the OpenHelperCallbacks implementation 
        SQLiteOpenHelper sqLiteOpenHelper = 
                new SQLiteOpenHelper(applicationContext.getContext(),
                                     databaseName,
                                     null,
                                     databaseVersion){

            @Override
            public void onCreate(SQLiteDatabase db) {
                androidSQLiteMap.get(databaseName).onCreate(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion,
                    int newVersion) {
                androidSQLiteMap.get(databaseName).onUpgrade(db, oldVersion, newVersion);
            }
            @Override
            public void onOpen(SQLiteDatabase db) {
                androidSQLiteMap.get(databaseName).setDatabase(db);
            }};
            openHelperConnectionSource = new OpenHelperConnectionSource(sqLiteOpenHelper, openHelperCallbacks);
            androidSQLiteMap.put(databaseName, openHelperConnectionSource);
            return openHelperConnectionSource;
    }

    /**
     * Close all ConnectionSource objects and clear the connection map.
     */
    public void close()
    {
        for (Entry<String, OpenHelperConnectionSource> entry: androidSQLiteMap.entrySet())
            entry.getValue().close();
        androidSQLiteMap.clear();
    }
}
