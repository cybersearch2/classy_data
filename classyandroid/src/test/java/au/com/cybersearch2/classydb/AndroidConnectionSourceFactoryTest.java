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
package au.com.cybersearch2.classydb;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import java.util.Properties;

import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import android.content.Context;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;

/**
 * AndroidConnectionSourceFactoryTest
 * @author Andrew Bowley
 * 10/07/2014
 */
@RunWith(RobolectricTestRunner.class)
public class AndroidConnectionSourceFactoryTest
{
    @Module(/*injects = PersistenceContext.class*/)
    static class AndroidConnectionSourceFactoryTestModule implements ApplicationModule
    {
        private Context context;
        private DatabaseAdmin databaseAdmin;
        private PersistenceFactory persistenceFactory;
  
        public AndroidConnectionSourceFactoryTestModule(Context context)
        {
            this.context = context;
            persistenceFactory = mock(PersistenceFactory.class);
            Persistence persistence = mock(Persistence.class);
            databaseAdmin = mock(DatabaseAdmin.class);
            when(persistenceFactory.getPersistenceUnit(PU_NAME)).thenReturn(persistence);
            when(persistence.getDatabaseAdmin()).thenReturn(databaseAdmin);
        }
        
        @Provides  @Singleton PersistenceFactory providePersistenceFactory()
        {
            return persistenceFactory;
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
 
    @Singleton
    @Component(modules = AndroidConnectionSourceFactoryTestModule.class)  
    static interface TestComponent extends ApplicationModule
    {
        void inject(PersistenceContext persistenceContext);
        void inject(ApplicationContext applicationContext);
    }
 
    protected AndroidConnectionSourceFactory androidConnectionSourceFactory;
    protected AndroidDatabaseSupport androidDatabaseSupport;
    static AndroidConnectionSourceFactoryTestModule androidConnectionSourceFactoryTestModule;
    static final String PU_NAME = "classyfy";
    static final String DATABASE_NAME = "classyfy.db";
    Properties properties;
    static Context context;
    
    @Before
    public void setUp()
    {
        if (context == null)
        {
            //context = TestRoboApplication.getTestInstance();
        	context = mock(Context.class);
            androidConnectionSourceFactoryTestModule = new AndroidConnectionSourceFactoryTestModule(context);
            TestComponent component =
                DaggerAndroidConnectionSourceFactoryTest_TestComponent.builder()
                .androidConnectionSourceFactoryTestModule(androidConnectionSourceFactoryTestModule)
                .build();
            DI.getInstance(component);
        }
        properties = new Properties();
        properties.setProperty(PersistenceUnitInfoImpl.PU_NAME_PROPERTY, PU_NAME);
        properties.setProperty(DatabaseAdmin.DATABASE_VERSION, "2");
        androidDatabaseSupport = mock(AndroidDatabaseSupport.class);
        androidConnectionSourceFactory = new AndroidConnectionSourceFactory(androidDatabaseSupport);
    }
    
    @Test
    public void test_constructor()
    {
        assertThat(androidConnectionSourceFactory.applicationContext).isNotNull();
    }

    @Test
    public void test_createAndroidSQLiteConnection()
    {
        OpenHelperConnectionSource result = 
             androidConnectionSourceFactory.createAndroidSQLiteConnection(DATABASE_NAME, properties);
        assertThat(result).isNotNull();
        assertThat(result.getSQLiteOpenHelper()).isInstanceOf(OpenEventHandler.class);
    }
    
    @Test
    public void test_getAndroidSQLiteConnection_custom()
    {
    	Properties testProperties = new Properties();
    	testProperties.putAll(properties);
    	testProperties.setProperty(PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY, TestOpenHelperCallbacks.class.getName());
        OpenHelperConnectionSource result = 
             androidConnectionSourceFactory.createAndroidSQLiteConnection(DATABASE_NAME, testProperties);
        assertThat(result).isNotNull();
        assertThat(result.getSQLiteOpenHelper()).isInstanceOf(OpenEventHandler.class);
        OpenEventHandler openEventHandler = (OpenEventHandler) result.getSQLiteOpenHelper();
        assertThat(openEventHandler.openHelperCallbacks).isInstanceOf(TestOpenHelperCallbacks.class);
    }
        
    @Test
    public void test_getAndroidSQLiteConnection_custom_instantiation_exception()
    {
        properties.setProperty("open-helper-callbacks-classname", "x" + TestOpenHelperCallbacks.class.getName());
        try
        {
            androidConnectionSourceFactory.createAndroidSQLiteConnection(DATABASE_NAME, properties);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).contains("x" + TestOpenHelperCallbacks.class.getName());
        }
    }
}
