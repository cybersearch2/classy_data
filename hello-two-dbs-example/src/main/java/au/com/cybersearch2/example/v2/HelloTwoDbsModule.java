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
package au.com.cybersearch2.example.v2;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.WorkerRunnable;

/**
 * ManyToManyModule
 * Dependency injection data object. @see ManyToManyMain.
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@Module(injects = { 
        WorkerRunnable.class,
        PersistenceFactory.class,
        NativeScriptDatabaseWork.class,
        HelloTwoDbsMain.class, 
        PersistenceContainer.class,
        EntityTransactionImpl.class,
        DatabaseUpgrader.class
        })
public class HelloTwoDbsModule implements ApplicationModule
{
	ConnectionType CONNECTION_TYPE = ConnectionType.file;
	
    @Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new TestSystemEnvironment();
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new JavaTestResourceEnvironment("src/test/resources");
    }

    @Provides @Singleton PersistenceFactory providePersistenceModule()
    {
        return new PersistenceFactory(new SQLiteDatabaseSupport(CONNECTION_TYPE));
    }

    @Provides @Singleton ConnectionType provideConnectionType()
    {
    	return CONNECTION_TYPE;
    }
}
