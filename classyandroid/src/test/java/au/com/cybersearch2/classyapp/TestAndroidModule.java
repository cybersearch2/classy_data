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

import android.content.Context;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
//import au.com.cybersearch2.classydb.DatabaseAdminImpl;
//import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyinject.ApplicationModule;
//import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
//import au.com.cybersearch2.classytask.WorkerRunnable;
import dagger.Module;
import dagger.Provides;

/**
 * TestAndroidModule
 * @author Andrew Bowley
 * 20/06/2014
 */
@Module(/*injects = { 
        WorkerRunnable.class,
        PersistenceFactory.class,
        NativeScriptDatabaseWork.class,
        PersistenceContext.class,
        DatabaseAdminImpl.class
        }*/)
public class TestAndroidModule implements ApplicationModule
{
    private Context context;

    public TestAndroidModule(Context context)
    {
        this.context = context;
    }
    
    @Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new TestSystemEnvironment();
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new TestResourceEnvironment();
    }

    @Provides @Singleton PersistenceFactory providePersistenceFactory()
    {
        return new PersistenceFactory(new AndroidDatabaseSupport());
    }

    /**
     * Returns Android Application Context
     * @return Context
     */
    @Provides @Singleton Context provideContext()
    {
        return context;
    }
}
