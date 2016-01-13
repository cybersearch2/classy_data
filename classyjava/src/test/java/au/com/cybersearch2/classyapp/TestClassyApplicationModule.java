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

import javax.inject.Singleton;

import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;
import au.com.cybersearch2.classyinject.ApplicationModule;
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
public class TestClassyApplicationModule implements ApplicationModule
{
    private ResourceEnvironment resourceEnvironment;
    private SQLiteDatabaseSupport sqliteDatabaseSupport;
    
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
        sqliteDatabaseSupport = new SQLiteDatabaseSupport(ConnectionType.memory);
        return sqliteDatabaseSupport;    
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
         return resourceEnvironment;
    }

    @Provides @Singleton PersistenceFactory providePersistenceFactory(DatabaseSupport databaseSupport, ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment);
    }

    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory()
    {
        return sqliteDatabaseSupport;
    }
   @Provides @Singleton PersistenceContext providePersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }
    
}


