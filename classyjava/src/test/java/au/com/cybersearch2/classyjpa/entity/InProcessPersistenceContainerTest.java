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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;

import com.j256.ormlite.support.ConnectionSource;

import dagger.Module;
import dagger.Provides;


/**
 * InProcessPersistenceContainerTest
 * @author Andrew Bowley
 * 27 Nov 2014
 */
public class InProcessPersistenceContainerTest 
{
    @Module(injects = { InProcessPersistenceContainer.class })
    public static class PersistenceContainerTestModule implements ApplicationModule
    {

        @Provides @Singleton PersistenceFactory providePersistenceModule()
        {
            transaction = mock(EntityTransactionImpl.class);
            entityManager = mock(EntityManagerImpl.class);
            when(entityManager.getTransaction()).thenReturn(transaction);
            connectionSource = mock(ConnectionSource.class);
            PersistenceFactory persistenceFactory = mock(PersistenceFactory.class);
            Persistence persistence = mock(Persistence.class);
            when(persistenceFactory.getPersistenceUnit(isA(String.class))).thenReturn(persistence);
            PersistenceAdmin persistenceAdmin = mock(PersistenceAdmin.class);
            when(persistence.getPersistenceAdmin()).thenReturn(persistenceAdmin);
            when(persistenceAdmin.createEntityManager(connectionSource)).thenReturn(entityManager);
            return persistenceFactory;
        }
        
    }

    private static EntityManagerImpl entityManager;
    private InProcessPersistenceContainer testContainer;
    private static EntityTransactionImpl transaction;
    private static ConnectionSource connectionSource;

    @Before
    public void setUp() throws Exception 
    {
         new DI(new PersistenceContainerTestModule());
         testContainer = new InProcessPersistenceContainer(TestClassyApplication.PU_NAME);
    }
 
    @Test 
    public void test_doWork() throws InterruptedException
    {
    	final boolean[] doTaskCalled = new boolean[1];
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				doTaskCalled[0] = true;
			}};
		testContainer.doWork(connectionSource, persistenceTask);
		assertThat(doTaskCalled[0]).isTrue();
		verify(transaction).begin();
		verify(entityManager).close();
    }   

    @Test 
    public void test_exception_thrown() throws InterruptedException
    {   
        EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final RecordCategory entity = new RecordCategory();
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		        entityManager.persist(entity);
			}};
	    doThrow(persistException).when(entityManager).persist(entity);
	    try
	    {
	    	testContainer.doWork(connectionSource, persistenceTask);
	    	failBecauseExceptionWasNotThrown(PersistenceException.class);
	    }
	    catch (PersistenceException e)
	    {
	    	assertThat(e.getMessage()).isEqualTo("classyfy Persistence Task failed");
	    	assertThat(e.getCause().getMessage()).isEqualTo("Entity of class RecordCategory, primary key 1 already exists");
	    }
        verify(transaction).begin();
        verify(transaction).setRollbackOnly();
        verify(entityManager).close();
    }
    
    @Test 
    public void test_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
			}};
	    doThrow(exception).when(entityManager).close();
		testContainer.doWork(connectionSource, persistenceTask);
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_close_following_previous_exception() throws InterruptedException
    {   // Expected behavior: The first exception is reported
    	EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        PersistenceException exception = new PersistenceException("Exception on pre-commit: SQLException");
        final RecordCategory entity = new RecordCategory();
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		        entityManager.persist(entity);
			}};
	    doThrow(persistException).when(entityManager).persist(entity);
        doThrow(exception).when(entityManager).close();
	    try
	    {
	    	testContainer.doWork(connectionSource, persistenceTask);
	    	failBecauseExceptionWasNotThrown(PersistenceException.class);
	    }
	    catch (PersistenceException e)
	    {
	    	assertThat(e.getMessage()).isEqualTo("classyfy Persistence Task failed");
	    	assertThat(e.getCause().getMessage()).isEqualTo("Entity of class RecordCategory, primary key 1 already exists");
	    }
    }

    @Test 
    public void test_exception_thrown_on_entity_manager_begin() throws InterruptedException
    {
        PersistenceException exception = new PersistenceException("Exception on connect: SQLException");
        doThrow(exception).when(transaction).begin();
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
			}};
	    try
	    {
	    	testContainer.doWork(connectionSource, persistenceTask);
	    	failBecauseExceptionWasNotThrown(PersistenceException.class);
	    }
	    catch (PersistenceException e)
	    {
	    	assertThat(e.getMessage()).isEqualTo("Exception on connect: SQLException");
	    }
    }

    @Test 
    public void test_null_pointer_exception_thrown_on_entity_manager_close() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        doThrow(exception).when(entityManager).close();
        when(transaction.isActive()).thenReturn(true);
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
			}};
	    try
	    {
	    	testContainer.doWork(connectionSource, persistenceTask);
	    	failBecauseExceptionWasNotThrown(NullPointerException.class);
	    }
	    catch (NullPointerException e)
	    {
	    	assertThat(e.getMessage()).isEqualTo("The parameter is null");
	    }
    }
    
    @Test 
    public void test_null_pointer_exception_thrown_on_persist() throws InterruptedException
    {
        NullPointerException exception = new NullPointerException("The parameter is null");
        final RecordCategory entity = new RecordCategory();
        when(transaction.isActive()).thenReturn(true);
        doThrow(exception).when(entityManager).persist(entity);
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		        entityManager.persist(entity);
			}};
	    try
	    {
	    	testContainer.doWork(connectionSource, persistenceTask);
	    	failBecauseExceptionWasNotThrown(NullPointerException.class);
	    }
	    catch (NullPointerException e)
	    {
	    	assertThat(e.getMessage()).isEqualTo("The parameter is null");
	    }
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(entityManager, never()).close();
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
    	PersistenceTask persistenceTask = new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		        entityManager.setFlushMode(FlushModeType.AUTO);
			}};
        do_persist_exception(exception, persistenceTask);
    }
    
    private void do_persist_exception(Exception exception) throws InterruptedException
    {
        do_persist_exception(exception, null);
    }
    

    private void do_persist_exception(Exception exception, PersistenceTask persistenceTask)
    {
        final RecordCategory entity = new RecordCategory();
        if (persistenceTask == null)
        {
            doThrow(exception).when(entityManager).persist(entity);
        	persistenceTask = new PersistenceTask(){

    			@Override
    			public void doTask(EntityManagerLite entityManager) 
    			{
    		        entityManager.persist(entity);
    			}};
        }
	    try
	    {
	    	testContainer.doWork(connectionSource, persistenceTask);
	    	failBecauseExceptionWasNotThrown(exception.getClass());
	    }
	    catch (RuntimeException e)
	    {
	    	assertThat(e.getMessage()).isEqualTo("classyfy Persistence Task failed");
	    	assertThat(e.getCause().getMessage()).isEqualTo(exception.getMessage());
	    }
        verify(transaction).begin();
        verify(transaction).setRollbackOnly();
        verify(entityManager).close();
    }
    

}
