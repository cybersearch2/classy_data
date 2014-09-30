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

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Singleton;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkerRunnable;
import au.com.cybersearch2.classyutil.Transcript;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import dagger.Module;
import dagger.Provides;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * DatabaseWorkerTaskTest
 * @author Andrew Bowley
 * 24/06/2014
 */
public class DatabaseWorkerTaskTest
{
    @Module(injects = { WorkerRunnable.class })
    public static class DatabaseWorkerTaskTestModule implements ApplicationModule
    {
        @Provides @Singleton ThreadHelper provideSystemEnvironment()
        {
            return new TestSystemEnvironment();
        }
    }

    interface DatabaseWorkerCallable
    {
        Boolean call(DatabaseConnection databaseConnection) throws Exception;
    }
            
    class CallableDatabaseWork extends TestDatabaseWork
    {
        DatabaseWorkerCallable callable;
        
        public CallableDatabaseWork(DatabaseWorkerCallable callable)
        {
            super(connectionSource);
            setMockTransaction(DatabaseWorkerTaskTest.this.transaction);
            this.callable = callable;
        }
        
        @Override
        public Boolean doInBackground(DatabaseConnection databaseConnection)
                throws Exception 
        {
            transcript.add("background task");
            return callable.call(databaseConnection);
        }

        @Override
        public void onPostExecute(boolean success) 
        {
            transcript.add("onPostExecute " + success);
       }

        @Override
        public void onRollback(Throwable rollbackException) 
        {
            transcript.add("onRollback " + rollbackException.toString());
            transcript.add(rollbackException.getCause().toString());
       }
        
    }
    
    DatabaseWorkerTask task;
    ConnectionSource connectionSource;
    DatabaseConnection databaseConnection;
    EntityTransaction transaction;
    private Transcript transcript;
    private static DI dependencyInjection;
   
    @Before
    public void setUp() throws SQLException
    {
        if (dependencyInjection == null)
            dependencyInjection = new DI(new DatabaseWorkerTaskTestModule());
        connectionSource = mock(ConnectionSource.class);
        databaseConnection = mock(DatabaseConnection.class);
        when(connectionSource.getReadWriteConnection()).thenReturn(databaseConnection);
        task = new DatabaseWorkerTask();
        transaction = mock(EntityTransaction.class);
        transcript = new Transcript();
    }
    
    @Test
    public void test_DatabaseWorkerTask_executeTask() throws Exception
    {
        DatabaseWork databaseWork = new CallableDatabaseWork(new DatabaseWorkerCallable(){

            @Override
            public Boolean call(DatabaseConnection databaseConnection)
                    throws Exception {
                return Boolean.valueOf(true);
            }});
        Executable exe = task.executeTask(databaseWork);
        synchronized(exe)
        {
            exe.wait();
        }
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        assertThat(task.isSuccess()).isTrue();
        verify(transaction).begin();
        verify(transaction).commit();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }
    
    @Test
    public void test_DatabaseWorkerTask_executeTask_fail() throws Exception
    {
        DatabaseWork databaseWork = new CallableDatabaseWork(new DatabaseWorkerCallable(){

            @Override
            public Boolean call(DatabaseConnection databaseConnection)
                    throws Exception {
                return Boolean.valueOf(false);
            }});
        Executable exe = task.executeTask(databaseWork);
        synchronized(exe)
        {
            exe.wait();
        }
        transcript.assertEventsSoFar("background task", "onPostExecute false");
        assertThat(task.isSuccess()).isFalse();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
        verify(transaction).begin();
        verify(transaction).rollback();

    }

    @Test
    public void test_DatabaseWorkerTask_exception_on_connection_close() throws Exception
    {
        // Error currently just logged, but maybe rollback would be better
        doThrow(new IOException("Connection error")).when(databaseConnection).close();
        DatabaseWork databaseWork = new CallableDatabaseWork(new DatabaseWorkerCallable(){

            @Override
            public Boolean call(DatabaseConnection databaseConnection)
                    throws Exception {
                return Boolean.valueOf(true);
            }});
        Executable exe = task.executeTask(databaseWork);
        synchronized(exe)
        {
            exe.wait();
        }
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        assertThat(task.isSuccess()).isTrue();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
        verify(transaction).begin();
        verify(transaction).commit();
    }

    @Test
    public void test_DatabaseWorkerTask_executeTask_npe() throws Exception
    {
        final NullPointerException npe = new NullPointerException("The parameter is null");
        DatabaseWork databaseWork = new CallableDatabaseWork(new DatabaseWorkerCallable(){

            @Override
            public Boolean call(DatabaseConnection databaseConnection)
                    throws Exception {
                throw npe;
            }});
        Executable exe = task.executeTask(databaseWork);
        synchronized(exe)
        {
            exe.wait();
        }
        transcript.assertEventsSoFar("background task", "onRollback javax.persistence.PersistenceException: Pre commit operation failed", npe.toString());
        assertThat(task.isSuccess()).isFalse();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
        verify(transaction).begin();
        verify(transaction).rollback();
    }

}
