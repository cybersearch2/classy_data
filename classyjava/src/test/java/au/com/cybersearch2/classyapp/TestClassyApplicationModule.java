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
package au.com.cybersearch2.classyapp;

import java.sql.SQLException;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import com.google.common.base.Throwables;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.H2DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.DatabaseSupportBase;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.ThreadHelper;
import dagger.Module;
import dagger.Provides;

/**
 * TestClassyApplicationModule
 * @author Andrew Bowley
 * 13/06/2014
 */
@Module
public class TestClassyApplicationModule
{
    private ResourceEnvironment resourceEnvironment;
    //private SQLiteDatabaseSupport sqliteDatabaseSupport;
    private H2DatabaseSupport h2DatabaseSupport;
    
    public TestClassyApplicationModule(ResourceEnvironment resourceEnvironment)
    {
        this.resourceEnvironment = resourceEnvironment;
    }
    
    @Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new TestSystemEnvironment();
    }
    
    @Provides @Singleton TaskManager provideTaskManager()
    {
        return new TaskManager();
    }

    @Provides @Singleton DatabaseSupport provideDatabaseSupport()
    {
        //sqliteDatabaseSupport = new SQLiteDatabaseSupport(ConnectionType.memory);
        //return sqliteDatabaseSupport;    
        h2DatabaseSupport = new H2DatabaseSupport(ConnectionType.memory); 
        return h2DatabaseSupport;     
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
         return resourceEnvironment;
    }

    @Provides @Singleton PersistenceFactory providePersistenceFactory(DatabaseSupport databaseSupport, ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment) {
        	@Override
            public void initializeAllDatabases(ConnectionSourceFactory connectionSourceFactory) {
                //Initialize PU implementations
                for (Map.Entry<String, DatabaseAdminImpl> entry: databaseAdminImplMap.entrySet())
                {
                    PersistenceAdminImpl persistenceAdmin = persistenceImplMap.get(entry.getKey());
                	DatabaseAdminImpl databaseAdmin = entry.getValue();
                    ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
                    try {
                    	databaseSupport.setVersion(0, connectionSource);
                    	System.out.println("Set user_info version to 0");
					} catch (PersistenceException e) {
						System.out.println(Throwables.getStackTraceAsString(e));
					}
                    super.initializeAllDatabases(connectionSourceFactory);
                }
            }
        };
    }

    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory()
    {
        //return sqliteDatabaseSupport;
    	return h2DatabaseSupport;
    }
    
    @Provides @Singleton PersistenceContext providePersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory, true);
    }
    
}


