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

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javax.inject.Singleton;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import dagger.Component;
import dagger.Subcomponent;

/**
 * AndroidHelloTwoDbs
 * @author Andrew Bowley
 * 24 Apr 2015
 */
public class AndroidHelloTwoDbs extends HelloTwoDbsMain 
{
    @Singleton
    @Component(modules = AndroidHelloTwoDbsModule.class)  
    static interface ApplicationComponent extends ApplicationModule
    {
        PersistenceContext persistenceContext();
        ConnectionType connectionType();
        PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
    }

    @Singleton
    @Subcomponent(modules = PersistenceWorkModule.class)
    static interface PersistenceWorkSubcontext
    {
        Executable executable();
    }

    public AndroidHelloTwoDbs() throws IOException, XmlPullParserException
    {
        super();
        final Context context = TestRoboApplication.getTestInstance();
        resourceEnvironment =
        
            new ResourceEnvironment(){

                @Override
                public InputStream openResource(String resourceName)
                        throws IOException
                {
                    return context.getAssets().open("hello2dbs/v2/" + resourceName);
                }
        
                @Override
                public Locale getLocale()
                {
                    return new Locale("en", "AU");
                }};
    }
 
    protected ResourceEnvironment resourceEnvironment;
    protected ApplicationComponent component;
    
    /**
     * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
     * Override to run with different database and/or platform. 
     */
    @Override
    protected PersistenceContext createObjectGraph()
    {
        Context context = TestRoboApplication.getTestInstance();
        component = 
                DaggerAndroidHelloTwoDbs_ApplicationComponent.builder()
                .androidHelloTwoDbsModule(new AndroidHelloTwoDbsModule(context, resourceEnvironment))
                .build();
        return component.persistenceContext();
    }

    @Override
    protected Executable getExecutable(String puName, PersistenceWork persistenceWork)
    {
        persistenceWorkModule = new PersistenceWorkModule(puName, true, persistenceWork);
        if (component == null)
            System.err.println("Component is null!");
         PersistenceWorkSubcontext persistenceWorkSubcontext = component.plus(persistenceWorkModule);
         if (persistenceWorkSubcontext == null)
             System.err.println("persistenceWorkSubcontext is null!");
         return persistenceWorkSubcontext.executable();
    }
    
    @Override
    ConnectionType getConnectionType()
    {
        return component.connectionType();
    }
}
