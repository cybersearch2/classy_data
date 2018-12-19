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

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;

/**
 * DatabaseAdminImplTest
 * @author Andrew Bowley
 * 01/08/2014
 */
public class DatabaseAdminImplTest
{
    public static final String CREATE_SQL_FILENAME = "create.sql";
    public static final String DROP_SQL_FILENAME = "drop.sql";
    public static final String DATA_FILENAME = "data.sql";
    public static final String UPGRADE_DATA_FILENAME = "classyfy-upgrade-v1-v2.sql";
    PersistenceAdmin persistenceAdmin;
    ConnectionSource connectionSource;
    Properties properties;
    ResourceEnvironment resourceEnvironment;

    @Before
    public void setUp()
    {
        properties = new Properties();
        persistenceAdmin = mock(PersistenceAdmin.class);
        connectionSource = mock(ConnectionSource.class);
        when(persistenceAdmin.getConnectionSource()).thenReturn(connectionSource);
        when(persistenceAdmin.getProperties()).thenReturn(properties);
        resourceEnvironment = new JavaTestResourceEnvironment("src/test/resources/sqlite");
    }

    @Test
    public void test_onCreate()
    {
        properties.setProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME, DROP_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.SCHEMA_FILENAME, CREATE_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.DATA_FILENAME, DATA_FILENAME);
        TestDatabaseAdminImpl databaseAdminImpl = new TestDatabaseAdminImpl(TestClassyApplication.PU_NAME, persistenceAdmin, resourceEnvironment);
        databaseAdminImpl.onCreate(connectionSource);
        NativeScriptDatabaseWork processFilesCallable = (NativeScriptDatabaseWork) databaseAdminImpl.processFilesCallable;
        assertThat(processFilesCallable.filenames.length).isEqualTo(3);
        assertThat(processFilesCallable.filenames[0]).isEqualTo(DROP_SQL_FILENAME);
        assertThat(processFilesCallable.filenames[1]).isEqualTo(CREATE_SQL_FILENAME);
        assertThat(processFilesCallable.filenames[2]).isEqualTo(DATA_FILENAME);
    }

    @Test
    public void test_onUpgrade()
    {
        properties.setProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME, DROP_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.SCHEMA_FILENAME, CREATE_SQL_FILENAME);
        properties.setProperty(DatabaseAdmin.DATA_FILENAME, DATA_FILENAME);
        TestDatabaseAdminImpl databaseAdminImpl = new TestDatabaseAdminImpl(TestClassyApplication.PU_NAME, persistenceAdmin, resourceEnvironment);
        databaseAdminImpl.onUpgrade(connectionSource, 1,2);
        NativeScriptDatabaseWork processFilesCallable = (NativeScriptDatabaseWork) databaseAdminImpl.processFilesCallable;
        assertThat(processFilesCallable.filenames.length).isEqualTo(1);
        assertThat(processFilesCallable.filenames[0]).isEqualTo(UPGRADE_DATA_FILENAME);
    }
}
