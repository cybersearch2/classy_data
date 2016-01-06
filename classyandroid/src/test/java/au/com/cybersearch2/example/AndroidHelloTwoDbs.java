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
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import dagger.Component;

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
        void inject(PersistenceContext persistenceContext);
        void inject(AndroidHelloTwoDbs helloTwoDbsMain);
        void inject(PersistenceFactory persistenceFactory);
        void inject(DatabaseAdminImpl databaseAdminImpl);
        void inject(NativeScriptDatabaseWork nativeScriptDatabaseWork);
        void inject(ApplicationContext applicationContext);
    }
 
    @Override
    protected void createObjectGraph()
    {
        Context context = TestRoboApplication.getTestInstance();
        ApplicationComponent component = 
                DaggerAndroidHelloTwoDbs_ApplicationComponent.builder()
                .androidHelloTwoDbsModule(new AndroidHelloTwoDbsModule(context))
                .build();
        // Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object
        DI.getInstance(component).validate();
    }

}
