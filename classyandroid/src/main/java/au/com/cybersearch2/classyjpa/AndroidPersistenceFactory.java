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
package au.com.cybersearch2.classyjpa;

import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.j256.ormlite.support.ConnectionSource;

import android.database.sqlite.SQLiteOpenHelper;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;


/**
 * AndroidPersistenceFactory
 * Factory for the creation of annonymous AndroidPersistenceEnvironment implementation objects 
 * @author Andrew Bowley
 * 06/07/2014
 */
public class AndroidPersistenceFactory
{
    /** Object which provides access to full persistence implementation */
    @Inject PersistenceFactory persistenceFactory;
    
    /**
     * Create AndroidPersistenceFactory object
     */
    public AndroidPersistenceFactory()
    {
        DI.inject(this); // Inject persistenceFactory
    }

    /**
     * Returns annonymous AndroidPersistenceEnvironment implementation object
     * @param puName Persistence Unit name
     * @return AndroidPersistenceEnvironment
     */
    public AndroidPersistenceEnvironment getAndroidPersistenceEnvironment(final String puName)
    {
        // Assumes DatabaseSupport is implemented as an AndroidDatabaseSupport object
        final AndroidDatabaseSupport androidDatabaseSupport = (AndroidDatabaseSupport)persistenceFactory.getDatabaseSupport();
        final Persistence persistence = persistenceFactory.getPersistenceUnit(puName);
        return new AndroidPersistenceEnvironment(){
            @Override
            public SQLiteOpenHelper getSQLiteOpenHelper() {
                // With AndroidDatabaseSupport and database name, obtain Persistence Unit ConnectionSource 
                // Then call getSQLiteOpenHelper() on the ConnectionSource to obtain the open helper
                Properties properties = persistence.getPersistenceAdmin().getProperties();
                String databaseName = properties.getProperty("database-name");
                if ((databaseName == null) || (databaseName.length() == 0))
                    throw new PersistenceException("\"" + persistence.getPersistenceUnitName() + "\" does not have property \"database-name\"");
                ConnectionSource connectionSource = androidDatabaseSupport.getConnectionSource(databaseName, properties);
                return androidDatabaseSupport.getSQLiteOpenHelper(connectionSource);
            }};
    }
}
