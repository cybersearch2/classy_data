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

import java.sql.SQLException;
import java.util.Properties;

import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import dagger.Module;
import dagger.Provides;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.Persistence;
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
    @Module(injects = OpenHelperCallbacksImpl.class)
    static class AndroidConnectionSourceFactoryTestModule implements ApplicationModule
    {
        DatabaseAdmin databaseAdmin;
        PersistenceFactory persistenceFactory;
  
        public AndroidConnectionSourceFactoryTestModule()
        {
            persistenceFactory = mock(PersistenceFactory.class);
            Persistence persistence = mock(Persistence.class);
            databaseAdmin = mock(DatabaseAdmin.class);
            when(persistenceFactory.getPersistenceUnit(PU_NAME)).thenReturn(persistence);
            when(persistence.getDatabaseAdmin()).thenReturn(databaseAdmin);
        }
        
        @Provides PersistenceFactory providePersistenceFactory()
        {
            return persistenceFactory;
        }
    }
    
    protected AndroidConnectionSourceFactory androidConnectionSourceFactory;
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
            context = TestRoboApplication.getTestInstance();
            androidConnectionSourceFactoryTestModule = new AndroidConnectionSourceFactoryTestModule();
            new DI(androidConnectionSourceFactoryTestModule, new ContextModule(context));
        }
        properties = new Properties();
        properties.setProperty(PersistenceUnitInfoImpl.PU_NAME_PROPERTY, PU_NAME);
        androidConnectionSourceFactory = new AndroidConnectionSourceFactory();
    }
    
    @Test
    public void test_constructor()
    {
        assertThat(androidConnectionSourceFactory.androidSQLiteMap).isNotNull();
        assertThat(androidConnectionSourceFactory.applicationContext).isNotNull();
    }

    @Test
    public void test_getAndroidSQLiteConnection()
    {
        Properties testProperties = new Properties();
        testProperties.putAll(properties);
        testProperties.setProperty(PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY, "au.com.cybersearch2.classyapp.TestOpenHelperCallbacks");
        OpenHelperConnectionSource result = 
             androidConnectionSourceFactory.getAndroidSQLiteConnection(DATABASE_NAME, testProperties);
        assertThat(result).isNotNull();
        assertThat(androidConnectionSourceFactory.androidSQLiteMap.get(DATABASE_NAME)).isEqualTo(result);
        assertThat(result.openHelperCallbacks).isNotNull();
        SQLiteOpenHelper sqLiteOpenHelper = result.getSQLiteOpenHelper();
        assertThat(sqLiteOpenHelper).isNotNull();
        // Database parameter not used by databaseAdmin, so can be set to null. 
        // Mocking not allowed as class is final.
        SQLiteDatabase db = null;
        sqLiteOpenHelper.onCreate(db);
        assertThat(au.com.cybersearch2.classyapp.TestOpenHelperCallbacks.getOpenConnectionSource()).isEqualTo(result);
        sqLiteOpenHelper.onUpgrade(db, 1, 2);
        assertThat(au.com.cybersearch2.classyapp.TestOpenHelperCallbacks.getUpdateConnectionSource()).isEqualTo(result);
        OpenHelperConnectionSource result2 = 
                androidConnectionSourceFactory.getAndroidSQLiteConnection(DATABASE_NAME, properties);
        assertThat(result2).isEqualTo(result);
    }
    
    @Test
    public void test_getAndroidSQLiteConnection_custom()
    {
        properties.setProperty("open-helper-callbacks-classname", TestOpenHelperCallbacks.class.getName());
        OpenHelperConnectionSource result = 
             androidConnectionSourceFactory.getAndroidSQLiteConnection(DATABASE_NAME, properties);
        assertThat(result).isNotNull();
        assertThat(androidConnectionSourceFactory.androidSQLiteMap.get(DATABASE_NAME)).isEqualTo(result);
        assertThat(result.openHelperCallbacks).isNotNull();
        SQLiteOpenHelper sqLiteOpenHelper = result.getSQLiteOpenHelper();
        assertThat(sqLiteOpenHelper).isNotNull();
        // Database parameter not used by databaseAdmin, so can be set to null. 
        // Mocking not allowed as class is final.
        SQLiteDatabase db = null;
        sqLiteOpenHelper.onCreate(db);
        assertThat(TestOpenHelperCallbacks.connectionSource).isEqualTo(result);
        TestOpenHelperCallbacks.connectionSource = null;
        sqLiteOpenHelper.onUpgrade(db, 1, 2);
        assertThat(TestOpenHelperCallbacks.connectionSource).isEqualTo(result);
        assertThat(TestOpenHelperCallbacks.oldVersion).isEqualTo(1);
        assertThat(TestOpenHelperCallbacks.newVersion).isEqualTo(2);
    }
    
    @Test
    public void test_getAndroidSQLiteConnection_custom_instantiation_exception()
    {
        properties.setProperty("open-helper-callbacks-classname", "x" + TestOpenHelperCallbacks.class.getName());
        try
        {
            androidConnectionSourceFactory.getAndroidSQLiteConnection(DATABASE_NAME, properties);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).contains("x" + TestOpenHelperCallbacks.class.getName());
        }
    }
    
   @Test
    public void test_close() throws SQLException
    {
        OpenHelperConnectionSource conn1 = mock(OpenHelperConnectionSource.class);
        OpenHelperConnectionSource conn2 = mock(OpenHelperConnectionSource.class);
        androidConnectionSourceFactory.androidSQLiteMap.put(DATABASE_NAME + "1", conn1);
        androidConnectionSourceFactory.androidSQLiteMap.put(DATABASE_NAME + "2", conn2);
        androidConnectionSourceFactory.close();
        verify(conn2).close();
        verify(conn1).close();
        assertThat(androidConnectionSourceFactory.androidSQLiteMap.size()).isEqualTo(0);
    }
    
}
