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

import android.content.Context;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.AndroidConnectionSourceFactory;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
import au.com.cybersearch2.classydb.AndroidSqliteParams;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;
import au.com.cybersearch2.classydb.OpenEventHandler;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.ThreadHelper;
import dagger.Module;
import dagger.Provides;

/**
 * ManyToManyModule
 * Dependency injection data object. @see ManyToManyMain.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module
public class AndroidManyToManyModule implements ApplicationModule
{
    private Context context;
    private String puName;
    private ResourceEnvironment resourceEnvironment;

    public AndroidManyToManyModule(
            Context context, 
            ResourceEnvironment resourceEnvironment, 
            String puName)
    {
        this.context = context;
        this.resourceEnvironment = resourceEnvironment;
        this.puName = puName;
    }
    
    /**
     * Returns Android Application Context
     * @return Context
     */
    @Provides @Singleton Context provideContext()
    {
        return context;
    } 
    
    @Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new TestSystemEnvironment();
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return resourceEnvironment;
    }

    @Provides @Singleton TaskManager provideTaskManager()
    {
        return new TaskManager();
    }

    @Provides @Singleton AndroidDatabaseSupport provideDatabaseSupport()
    {
        return new AndroidDatabaseSupport();
    }
    
    @Provides @Singleton PersistenceFactory providePersistenceFactory(
            AndroidDatabaseSupport databaseSupport, 
            ResourceEnvironment resourceEnvironment)
    {
        return new PersistenceFactory(databaseSupport, resourceEnvironment);
    }

    @Provides @Singleton OpenEventHandler provideOpenEventHandler(Context context, PersistenceFactory persistenceFactory)
    {
        // NOTE: This class extends Android SQLiteHelper 
        return new OpenEventHandler(new AndroidSqliteParams(context, puName, persistenceFactory));
    }
  
    @Provides @Singleton ConnectionSourceFactory provideConnectionSourceFactory(OpenEventHandler openEventHandler)
    {
        return new AndroidConnectionSourceFactory(openEventHandler);
    }
    
    @Provides @Singleton PersistenceContext providePersistenceContext(
            PersistenceFactory persistenceFactory, 
            ConnectionSourceFactory connectionSourceFactory)
    {
        return new PersistenceContext(persistenceFactory, connectionSourceFactory);
    }

}
