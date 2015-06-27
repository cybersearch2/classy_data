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
package au.com.cybersearch2.classyjpa.entity;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.fest.assertions.api.Assertions.assertThat;

import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.j256.ormlite.support.ConnectionSource;

import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.persist.TestEntityManagerFactory;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.ThreadHelper;
import au.com.cybersearch2.classytask.TestSystemEnvironment;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkerRunnable;
import au.com.cybersearch2.classyutil.Transcript;

/**
 * PersistenceContainerTest
 * @author Andrew Bowley
 * 27/06/2014
 */
public class PersistenceContainerTest
{
    @Module(injects = { PersistenceContext.class, WorkerRunnable.class })
    public static class PersistenceContainerTestModule implements ApplicationModule
    {

        @Provides @Singleton PersistenceFactory providePersistenceModule() 
        {
            PersistenceFactory persistenceFactory = mock(PersistenceFactory.class);
            Persistence persistence = mock(Persistence.class);
            when(persistenceFactory.getPersistenceUnit(isA(String.class))).thenReturn(persistence);
            PersistenceAdmin persistenceAdmin = mock(PersistenceAdmin.class);
            ConnectionSource connectionSource = mock(ConnectionSource.class);
            when(persistenceAdmin.isSingleConnection()).thenReturn(false);
            when(persistenceAdmin.getConnectionSource()).thenReturn(connectionSource);
            when(persistence.getPersistenceAdmin()).thenReturn(persistenceAdmin);
            when(persistenceAdmin.getEntityManagerFactory()).thenReturn(new TestEntityManagerFactory());
            return persistenceFactory;
        }
        
        @Provides @Singleton ThreadHelper provideSystemEnvironment()
        {
            return new TestSystemEnvironment();
        }
    }

    class EntityManagerWork extends TestPersistenceWork
    {
        RecordCategory entity;
        
        public EntityManagerWork(RecordCategory entity, Transcript transcript)
        {
            super(transcript);
            this.entity = entity;
        }
        
        @Override
        public void doTask(EntityManagerLite entityManager) 
        {
            super.doTask(entityManager);
            entityManager.persist(entity);
        }
    }
    
    class FlushModeWork extends TestPersistenceWork
    {
        RecordCategory entity;
        
        public FlushModeWork(Transcript transcript)
        {
            super(transcript);
        }
        
        @Override
        public void doTask(EntityManagerLite entityManager) 
        {
            super.doTask(entityManager);
            entityManager.setFlushMode(FlushModeType.AUTO);
        }
    }

    private EntityManagerImpl entityManager;
    private PersistenceContainer testContainer;
    private Transcript transcript;
    private EntityTransactionImpl transaction;

    @Before
    public void setUp() throws Exception 
    {
        new DI(new PersistenceContainerTestModule());
        transcript = new Transcript();
        testContainer = new PersistenceContainer(TestClassyApplication.PU_NAME);
        transaction = TestEntityManagerFactory.setEntityManagerInstance();
        entityManager = (EntityManagerImpl) TestEntityManagerFactory.getEntityManager();
    }
    
    @Test 
    public void test_background_called() throws InterruptedException
    {
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        verify(transaction).begin();
        verify(entityManager).close();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }

    @Test 
    public void test_exception_thrown() throws InterruptedException
    {   
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        doThrow(persistException).when(entityManager).persist(entity);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        verify(transaction).begin();
        verify(transaction).setRollbackOnly();
        verify(entityManager).close();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        doThrow(exception).when(entityManager).close();
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_close_following_previous_exception() throws InterruptedException
    {   // Expected behavior: The first exception is reported
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        doThrow(persistException).when(entityManager).persist(entity);
        doThrow(exception).when(entityManager).close();
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + persistException.toString());
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_begin() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on connect: SQLException");
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(transaction).begin();
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("onRollback " + exception.toString());
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Test 
    public void test_null_pointer_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        doThrow(exception).when(entityManager).close();
        when(transaction.isActive()).thenReturn(true);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_null_pointer_exception_thrown_on_persist() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        when(transaction.isActive()).thenReturn(true, false);
        doThrow(exception).when(entityManager).persist(entity);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(entityManager, never()).close();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_persist_NullPointerException_no_active_transaction() throws InterruptedException
    {
        transaction = mock(EntityTransactionImpl.class);
        TestEntityManagerFactory.setEntityManagerInstance(transaction);
        entityManager = (EntityManagerImpl) TestEntityManagerFactory.getEntityManager();

        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        PersistenceWork persistenceWork = new EntityManagerWork(entity, transcript);
        when(transaction.isActive()).thenReturn(false);
        doThrow(exception).when(entityManager).persist(entity);
        testContainer.setUserTransactionMode(true);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "onRollback " + exception.toString());
        verify(transaction, times(0)).begin();
        verify(transaction, times(0)).rollback();
        verify(entityManager, never()).close();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
    
    @Test 
    public void test_background_user_transaction() throws InterruptedException
    {
        PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        testContainer.setUserTransactionMode(true);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        verify(entityManager, times(2)).setUserTransaction(true);
        transcript.assertEventsSoFar("background task", "onPostExecute true");
        verify(entityManager).close();
        verify(transaction, times(0)).getRollbackOnly();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }    

    @Test
    public void test_persist_EntityExistsException() throws InterruptedException
    {
        do_persist_exception(new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists"));
    }
    
    @Test
    public void test_persist_IllegalArgumentException() throws InterruptedException
    {
        do_persist_exception(new IllegalArgumentException("persist entity has null primary key"));
    }
    
    @Test
    public void test_persist_IllegalStateException() throws InterruptedException
    {
        do_persist_exception(new IllegalStateException("persist called after EntityManager has been closed"));
    }

    @Test
    public void test_persist_UnsupportedOperationException() throws InterruptedException
    {
        UnsupportedOperationException exception = new UnsupportedOperationException("FlushModeType.AUTO not supported");
        Mockito.doThrow(exception).when(entityManager).setFlushMode(FlushModeType.AUTO);
        do_persist_exception(exception, new FlushModeWork(transcript));
    }
    
    private void do_persist_exception(Throwable exception) throws InterruptedException
    {
        do_persist_exception(exception, null);
    }
    
    private void do_persist_exception(Throwable exception, PersistenceWork persistenceWork) throws InterruptedException
    {
        final RecordCategory entity = new RecordCategory();
        if (persistenceWork == null)
        {
            persistenceWork = new EntityManagerWork(entity, transcript);
            doThrow(exception).when(entityManager).persist(entity);
        }
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        verify(transaction).begin();
        verify(transaction).setRollbackOnly();
        transcript.assertEventsInclude("onRollback " + exception.toString());
        verify(entityManager).close();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }
}
