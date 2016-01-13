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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.database.sqlite.SQLiteDatabase;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;

/**
 * AndroidConnectionSourceFactoryTest
 * @author Andrew Bowley
 * 10/07/2014
 */
@RunWith(RobolectricTestRunner.class)
public class AndroidConnectionSourceFactoryTest
{
    protected AndroidConnectionSourceFactory androidConnectionSourceFactory;
    protected OpenEventHandler openEventHandler;
    protected SQLiteDatabase sqliteDatabase;
    static final String PU_NAME = "classyfy";
    static final String DATABASE_NAME = "classyfy.db";
    Properties properties;
    
    @Before
    public void setUp()
    {
        openEventHandler = mock(OpenEventHandler.class);
        when(openEventHandler.getDatabaseName()).thenReturn(DATABASE_NAME);
        sqliteDatabase = mock(SQLiteDatabase.class);
        when(openEventHandler.getWritableDatabase()).thenReturn(sqliteDatabase);
        properties = new Properties();
        properties.setProperty(PersistenceUnitInfoImpl.PU_NAME_PROPERTY, PU_NAME);
        properties.setProperty(DatabaseAdmin.DATABASE_VERSION, "2");
        androidConnectionSourceFactory = new AndroidConnectionSourceFactory(openEventHandler);
    }
    
    @Test
    public void test_createAndroidSQLiteConnection()
    {
        OpenHelperConnectionSource result = 
             androidConnectionSourceFactory.getConnectionSource(DATABASE_NAME, properties);
        assertThat(result).isNotNull();
        assertThat(result.getSQLiteOpenHelper()).isEqualTo(openEventHandler);
    }
/*    
    @Test
    public void test_getAndroidSQLiteConnection_custom()
    {
    	Properties testProperties = new Properties();
    	testProperties.putAll(properties);
    	testProperties.setProperty(PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY, TestOpenHelperCallbacks.class.getName());
        OpenHelperConnectionSource result = 
             androidConnectionSourceFactory.getConnectionSource(DATABASE_NAME, testProperties);
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
            androidConnectionSourceFactory.getConnectionSource(DATABASE_NAME, properties);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).contains("x" + TestOpenHelperCallbacks.class.getName());
        }
    }
    */
}
