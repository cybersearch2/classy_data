/**
    Copyright (C) 2015  www.cybersearch2.com.au

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

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import dagger.Component;
import dagger.Subcomponent;

/**
 * AndroidManyToManyFactory
 * @author Andrew Bowley
 * 8 Jan 2016
 */
public class AndroidManyToManyFactory
{
    @Singleton
    @Component(modules = AndroidManyToManyModule.class)  
    static interface ApplicationComponent extends ApplicationModule
    {
        PersistenceContext persistenceContext();
        PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
    }

    @Singleton
    @Subcomponent(modules = PersistenceWorkModule.class)
    static interface PersistenceWorkSubcontext
    {
        Executable executable();
    }

    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    
    public AndroidManyToManyFactory(final Context context) throws IOException, XmlPullParserException
    {
        ResourceEnvironment resourceEnvironment = 
            new ResourceEnvironment(){
    
                @Override
                public InputStream openResource(String resourceName)
                        throws IOException
                {
                    return context.getAssets().open(ManyToManyMain.PU_NAME + "/" + resourceName);
                }
    
                @Override
                public Locale getLocale()
                {
                    return new Locale("en", "AU");
                }};
        component = 
                DaggerAndroidManyToManyFactory_ApplicationComponent.builder()
                .androidManyToManyModule(new AndroidManyToManyModule(context, resourceEnvironment, ManyToManyMain.PU_NAME))
                .build();
    }
    
    public PersistenceContext getPersistenceContext()
    {
        return component.persistenceContext();
    }
    
    public Executable getExecutable(PersistenceWork persistenceWork)
    {
        persistenceWorkModule = new PersistenceWorkModule("manytomany", true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }
    
}
