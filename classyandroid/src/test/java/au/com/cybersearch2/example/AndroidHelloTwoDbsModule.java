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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.inject.Singleton;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.AndroidDatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.ThreadHelper;

/**
 * AndroidHelloTwoDbsModule
 * @author Andrew Bowley
 * 24 Apr 2015
 */
@Module(/*injects = { 
		AndroidHelloTwoDbs.class,
        WorkerRunnable.class,
        PersistenceFactory.class,
        NativeScriptDatabaseWork.class,
        PersistenceContext.class,
        DatabaseAdminImpl.class
        }*/)
public class AndroidHelloTwoDbsModule implements ApplicationModule 
{
	//ConnectionType CONNECTION_TYPE = ConnectionType.memory;
	ConnectionType CONNECTION_TYPE = ConnectionType.file;
	private Context context;

	public AndroidHelloTwoDbsModule(Context context)
	{
	    this.context = context;
	}
	
	@Provides @Singleton ThreadHelper provideSystemEnvironment()
    {
        return new TestSystemEnvironment();
    }
    
    @Provides @Singleton ResourceEnvironment provideResourceEnvironment()
    {
        return new ResourceEnvironment(){

            @Override
            public InputStream openResource(String resourceName)
                    throws IOException
            {
                return context.getAssets().open("hello2dbs/" + resourceName);
            }

            @Override
            public Locale getLocale()
            {
                return new Locale("en", "AU");
            }};
    }

    @Provides @Singleton PersistenceFactory providePersistenceModule()
    {
        return new PersistenceFactory(new AndroidDatabaseSupport());
    }

    @Provides @Singleton ConnectionType provideConnectionType()
    {
    	return CONNECTION_TYPE;
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
