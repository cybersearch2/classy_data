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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.inject.Singleton;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.TestSystemEnvironment;

import com.j256.ormlite.support.DatabaseConnection;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

/**
 * NativeScriptDatabaseWorkTest
 * @author Andrew Bowley
 * 01/08/2014
 */
public class NativeScriptDatabaseWorkTest
{
    @Module(/*injects = { 
            PersistenceContext.class,
            NativeScriptDatabaseWork.class, 
            PersistenceFactory.class,
            WorkerRunnable.class }*/)
    class NativeScriptDatabaseWorkTestModule implements ApplicationModule
    {
        @Provides ResourceEnvironment provideResourceEnvironment()
        {
            return resourceEnvironment;
        }
        
        @Provides ThreadHelper provideSystemEnvironment()
        {
            return new TestSystemEnvironment();
        }
        
        @Provides @Singleton PersistenceFactory providePersistenceFactory()
        {
            PersistenceFactory persistenceFactory = mock(PersistenceFactory.class);
            DatabaseSupport databaseSupport = mock(DatabaseSupport.class);
            when(persistenceFactory.getDatabaseSupport()).thenReturn(databaseSupport);
            return persistenceFactory;
        }
    }

    @Singleton
    @Component(modules = NativeScriptDatabaseWorkTestModule.class)  
    static interface ApplicationComponent extends ApplicationModule
    {
        void inject(PersistenceContext persistenceContext);
        void inject(PersistenceFactory persistenceFactory);
        void inject(DatabaseAdminImpl databaseAdminImpl);
        void inject(NativeScriptDatabaseWork nativeScriptDatabaseWork);
    }


    static final String CREATE_SQL = "create table models ( _id integer primary key autoincrement, name text, _description text);\n";
    public static final String CREATE_SQL_FILENAME = "create.sql";
    public static final String DROP_SQL_FILENAME = "drop.sql";

    PersistenceAdmin persistenceAdmin;
    ResourceEnvironment resourceEnvironment;
    DatabaseConnection databaseConnection;

    @Before
    public void setUp()
    {
        databaseConnection = mock(DatabaseConnection.class);
        resourceEnvironment = mock(ResourceEnvironment.class);
        persistenceAdmin = mock(PersistenceAdmin.class);
        ApplicationComponent component = 
                DaggerNativeScriptDatabaseWorkTest_ApplicationComponent.builder()
                .nativeScriptDatabaseWorkTestModule(new NativeScriptDatabaseWorkTestModule())
                .build();
        DI.getInstance(component).validate();
    }
    
    @Test
    public void test_constructor()
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(CREATE_SQL_FILENAME);
        assertThat(databaseWork.resourceEnvironment).isNotNull();
    }
    
    @Test
    public void test_call() throws Exception
    {
        TestByteArrayInputStream bais = new TestByteArrayInputStream(CREATE_SQL.getBytes());
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(CREATE_SQL_FILENAME);
        when(resourceEnvironment.openResource(CREATE_SQL_FILENAME)).thenReturn(bais);
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(true);
        assertThat(bais.isClosed()).isTrue();
        verify(databaseConnection).executeStatement(CREATE_SQL.trim(), DatabaseConnection.DEFAULT_RESULT_FLAGS);
    }

    @Test
    public void test_call_null_filename() throws Exception
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork((String)null);
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(false);
   }
 

    @Test
    public void test_call_empty_filename() throws Exception
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork("");
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(false);
   }

    @Test
    public void test_doInBackground_ioexception_on_open() throws Exception
    {
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(CREATE_SQL_FILENAME);
        doThrow(new IOException("File not found")).when(resourceEnvironment).openResource(CREATE_SQL_FILENAME);
        try
        {
            databaseWork.call(databaseConnection);
            failBecauseExceptionWasNotThrown(IOException.class);
        }
        catch (IOException e)
        {
            assertThat(e.getMessage()).isEqualTo("File not found");
        }
    }

    @Test
    public void test_doInBackground_ioexception_on_close() throws Exception
    {
        TestByteArrayInputStream bais = new TestByteArrayInputStream(CREATE_SQL.getBytes());
        bais.throwExceptionOnClose = true;
        NativeScriptDatabaseWork databaseWork = new NativeScriptDatabaseWork(CREATE_SQL_FILENAME);
        when(resourceEnvironment.openResource(CREATE_SQL_FILENAME)).thenReturn(bais);
        Boolean result = databaseWork.call(databaseConnection);
        assertThat(result).isEqualTo(true);
        verify(databaseConnection).executeStatement(CREATE_SQL.trim(), DatabaseConnection.DEFAULT_RESULT_FLAGS);
    }
    
}
