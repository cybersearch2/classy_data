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
package au.com.cybersearch2.example;

import javax.inject.Singleton;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.H2DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.ThreadHelper;
import dagger.Module;
import dagger.Provides;

/**
 * H2ManyToManyModule
 * H2 database allows multiple connections, so PersistenceContainer runs 
 * the requested task in a background thread. This means WorkerRunnable 
 * must be supported.
 * background thread
 * Dependency injection data object for H2 database example. @see H2ManyToManyMain.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module
public class H2ManyToManyModule
{
    private ResourceEnvironment resourceEnvironment;
    private H2DatabaseSupport h2DatabaseSupport;
    
    public H2ManyToManyModule(ResourceEnvironment resourceEnvironment)
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

    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return resourceEnvironment;
    }

    @Provides @Singleton DatabaseSupport provideDatabaseSupport()
    {
        // Note memory ConnectionType does not work. Reason unknown
        h2DatabaseSupport = new H2DatabaseSupport(ConnectionType.file); 
        return h2DatabaseSupport;     
    }
    
    @Provides @Singleton PersistenceFactory providePersistenceFactory(DatabaseSupport databaseSupport, ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment);
    }

    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory()
    {
        return h2DatabaseSupport;
    }

    @Provides @Singleton PersistenceContext providePersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }
}
