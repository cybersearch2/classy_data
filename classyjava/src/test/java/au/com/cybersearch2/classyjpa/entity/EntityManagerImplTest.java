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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupportBase;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.query.EntityQuery;
import au.com.cybersearch2.classyjpa.query.NamedDaoQuery;
import au.com.cybersearch2.classyjpa.query.NamedSqlQuery;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import dagger.Component;
import dagger.Module;
import dagger.Provides;




/**
 * ClassyEntityManagerTest
 * @author Andrew Bowley
 * 02/05/2014
 */
public class EntityManagerImplTest
{
    @Module(/*injects = { 
            PersistenceContext.class,
            NativeScriptDatabaseWork.class }*/)
    class ClassyEntityManagerTestModule
    {
        @Provides ResourceEnvironment provideResourceEnvironment()
        {
            return new JavaTestResourceEnvironment();
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
    @Component(modules = ClassyEntityManagerTestModule.class)  
    static interface ApplicationComponent
    {
        void inject(PersistenceContext persistenceContext);
        void inject(NativeScriptDatabaseWork nativeScriptDatabaseWork);
    }


    private ConnectionSource connectionSource;
    private Map<String,OrmDaoHelperFactory<?,?>> helperFactoryMap;
    private Map<String,NamedDaoQuery> namedQueryMap;
    private Map<String,NamedSqlQuery> namedSqlQueryMap;
    private PersistenceConfig persistenceConfig;
    @SuppressWarnings("rawtypes")
    private OrmDaoHelper ormDaoHelper;
    private EntityTransaction transaction;
    private EntityManagerImpl entityManagerImpl;
    private ObjectMonitor objectMonitor;
    private TransactionCallable onPrecommit;
    private DatabaseConnection connection;
    private PersistenceDao<?, ?> dao;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception 
    {
        @SuppressWarnings("unused")
        ApplicationComponent component = 
                DaggerEntityManagerImplTest_ApplicationComponent.builder()
                .classyEntityManagerTestModule(new ClassyEntityManagerTestModule())
                .build();
        connectionSource = mock(ConnectionSource.class);
        helperFactoryMap = mock(Map.class);
        @SuppressWarnings("rawtypes")
        OrmDaoHelperFactory ormDaoHelperFactory = mock(OrmDaoHelperFactory.class);
        when(helperFactoryMap.get(RecordCategory.class.getName())).thenReturn(ormDaoHelperFactory);
        dao = mock(PersistenceDao.class);
        when(ormDaoHelperFactory.getDao(connectionSource)).thenReturn(dao);
        ormDaoHelper = mock(OrmDaoHelper.class);
        when(ormDaoHelperFactory.getOrmDaoHelper(connectionSource)).thenReturn(ormDaoHelper);
        namedQueryMap = mock(Map.class);
        namedSqlQueryMap = mock(Map.class);
        transaction = mock(EntityTransactionImpl.class);
        objectMonitor = mock(ObjectMonitor.class);
        connection = mock(DatabaseConnection.class);
        when(connectionSource.getReadWriteConnection(DatabaseSupportBase.DATABASE_INFO_NAME)).thenReturn(connection);
        when(connection.isAutoCommitSupported()).thenReturn(true);
        when(connection.isAutoCommit()).thenReturn(true);
        Savepoint savePoint = mock(Savepoint.class);
        when(connection.setSavePoint(isA(String.class))).thenReturn(savePoint);
        when(savePoint.getSavepointName()).thenReturn("mySavePoint");
        persistenceConfig = mock(PersistenceConfig.class);
        when(persistenceConfig.getHelperFactoryMap()).thenReturn(helperFactoryMap);
        when(persistenceConfig.getNamedQueryMap()).thenReturn(namedQueryMap);
        when(persistenceConfig.getNativeQueryMap()).thenReturn(namedSqlQueryMap);
        entityManagerImpl = new EntityManagerImpl(connectionSource, persistenceConfig);
        onPrecommit = entityManagerImpl.onTransactionPreCommitCallback;
        entityManagerImpl.entityTransaction = transaction;
        entityManagerImpl.objectMonitor = objectMonitor;
    }

    @Test
    public void test_constructor() throws Exception
    {
        assertThat(entityManagerImpl).isNotNull();
        assertThat(entityManagerImpl.isOpen).isTrue();
        assertThat(onPrecommit).isNotNull();
    }
    
    @Test 
    public void test_persist() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        when(transaction.isActive()).thenReturn(true);
        when(ormDaoHelper.create(entity)).thenReturn(1);
        when(objectMonitor.monitorNewEntity(entity, id, id)).thenReturn(true);
        entityManagerImpl.persist(entity);
        verify(transaction, times(0)).begin();
    }

    @Test 
    public void test_persist_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.create(entity)).thenReturn(1);
        when(objectMonitor.monitorNewEntity(entity, id, id)).thenReturn(true);
        entityManagerImpl.persist(entity);
        verify(transaction).begin();
    }

    @Test 
    public void test_persist_primary_key_create_error() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = null;
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.create(entity)).thenReturn(1);
        when(objectMonitor.monitorNewEntity(entity, id, id)).thenReturn(false);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains("No Primary key or matches one belonging to managed entity");
        }
        verify(transaction).begin();
        verify(transaction).rollback();
    }

   @Test 
    public void test_contains_managed() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(entity);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(true);
        verifyZeroInteractions(transaction);
    }

    @Test 
    public void test_contains_exists() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(true);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(true);
        verifyZeroInteractions(transaction);
    }

    @Test 
    public void test_contains_not_exists() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(false);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(false);
        verifyZeroInteractions(transaction);
    }
    
    @Test 
    public void test_contains_extract_null_id() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(null);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.contains)).thenReturn(null);
        assertThat(entityManagerImpl.contains(entity)).isEqualTo(false);
        verifyZeroInteractions(transaction);
    }

    @Test 
    public void test_merge() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.merge)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(true);
        assertThat(entityManagerImpl.merge(entity)).isEqualTo(entity);
        verify(transaction, times(0)).begin();
    }
 
    @Test 
    public void test_merge_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.merge)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(false);
        assertThat(entityManagerImpl.merge(entity)).isEqualTo(entity);
        verify(transaction).begin();
    }
 
    @Test 
    public void test_refresh() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(true);
        when(ormDaoHelper.refresh(entity)).thenReturn(1);
        entityManagerImpl.refresh(entity);
        verify(transaction, times(0)).begin();
    }
 
    @Test 
    public void test_refresh_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.refresh(entity)).thenReturn(1);
        entityManagerImpl.refresh(entity);
        verify(transaction).begin();
    }
 
    @Test 
    public void test_remove() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(transaction.isActive()).thenReturn(true);
        when(ormDaoHelper.delete(entity)).thenReturn(1);
        entityManagerImpl.remove(entity);
        verify(transaction, times(0)).begin();
        verify(objectMonitor).markForRemoval(RecordCategory.class, id);
    }
 
    @Test 
    public void test_remove_no_transaction() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(transaction.isActive()).thenReturn(false);
        when(ormDaoHelper.delete(entity)).thenReturn(1);
        entityManagerImpl.remove(entity);
        verify(transaction).begin();
        verify(objectMonitor).markForRemoval(RecordCategory.class, id);
    }
 
    @Test 
    public void test_persist_already_managed() throws Exception
    { 
        RecordCategory managed = new RecordCategory();
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(managed);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(EntityExistsException.class);
        }
        catch(EntityExistsException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id.toString());
        }
    }
 
    @Test 
    public void test_fresh_not_managed() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(null);
        try
        {
            entityManagerImpl.refresh(entity);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id.toString());
        }
    }
 
    @Test 
    public void test_persist_already_created() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.entityExists(entity)).thenReturn(true);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(EntityExistsException.class);
        }
        catch(EntityExistsException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id.toString());
        }
    }
 
    @Test 
    public void test_persist_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.persist(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("persist");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_persist_null_entity() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.persist(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"entity\" is null");
        }
    }
 
    @Test 
    public void test_merge_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.merge(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("merge");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_refresh_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.refresh(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("refresh");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_remove_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.remove(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("remove");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_find_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        Integer primaryKey = new Integer(1);
        try
        {
            entityManagerImpl.find(RecordCategory.class, primaryKey);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("find");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }
 
    @Test 
    public void test_get_reference_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        Integer primaryKey = new Integer(1);
        try
        {
            entityManagerImpl.getReference(RecordCategory.class, primaryKey);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("getReference");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_flush_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.flush();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("flush");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_clear_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.clear();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("clear");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_contains_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.contains(prepareHelperMap());
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("contains");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_getDelegate_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.getDelegate();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("getDelegate");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_persist_unregistered_class() throws Exception
    { 
        when(helperFactoryMap.get(RecordCategory.class.getName())).thenReturn(null);
        try
        {
            entityManagerImpl.persist(new RecordCategory());
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
        }
    }
 
    @Test 
    public void test_persist_dao_returns_0() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.persist)).thenReturn(null);
        when(ormDaoHelper.create(entity)).thenReturn(0);
        try
        {
            entityManagerImpl.persist(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("persist");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }
 
    @Test 
    public void test_refresh_dao_returns_0() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        when(ormDaoHelper.create(entity)).thenReturn(0);
        try
        {
            entityManagerImpl.refresh(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("refresh");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }
 
    @Test 
    public void test_delete_dao_returns_0() throws Exception
    { 
        RecordCategory entity = prepareHelperMap();
        Integer id = new Integer(1);
        when(ormDaoHelper.extractId(entity)).thenReturn(id);
        when(objectMonitor.startManagingEntity(entity, id, PersistOp.refresh)).thenReturn(entity);
        when(ormDaoHelper.create(entity)).thenReturn(0);
        try
        {
            entityManagerImpl.remove(entity);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("remove");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }
 
    @Test 
    public void test_pre_commit_dao_returns_0() throws Exception
    { 
        RecordCategory entity = new RecordCategory();
        when(transaction.isActive()).thenReturn(true);
        prepareDoUpdates(entity, 0);
        try
        {
            onPrecommit.call(connection);
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).contains("update");
            assertThat(e.getMessage()).contains("result count 0");
        }
    }
 
    @Test
    public void test_find() throws Exception
    {
        RecordCategory entity = prepareHelperMap();
        Integer primaryKey = new Integer(1);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(entity);
        assertThat(entityManagerImpl.find(RecordCategory.class, primaryKey)).isEqualTo(entity);
        verifyZeroInteractions(transaction);
    }

    @Test
    public void test_find_not_found() throws Exception
    {
        Integer primaryKey = new Integer(1);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(null);
        assertThat(entityManagerImpl.find(RecordCategory.class, primaryKey)).isEqualTo(null);
    }
    
    @Test
    public void test_get_reference() throws Exception
    {
        RecordCategory entity = prepareHelperMap();
        Integer primaryKey = new Integer(1);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(entity);
        assertThat(entityManagerImpl.getReference(RecordCategory.class, primaryKey)).isEqualTo(entity);
        verifyZeroInteractions(transaction);
    }
    
    @Test
    public void test_get_reference_not_found() throws Exception
    {
        Integer primaryKey = new Integer(1);
        when(ormDaoHelper.queryForId(primaryKey)).thenReturn(null);
        try
        {
            entityManagerImpl.getReference(RecordCategory.class, primaryKey);
            failBecauseExceptionWasNotThrown(EntityNotFoundException.class);
        }
        catch(EntityNotFoundException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(primaryKey.toString());
        }
    }

    @Test
    public void test_flush_active() throws Exception
    {
        RecordCategory entity = new RecordCategory();
        when(transaction.isActive()).thenReturn(true);
        prepareDoUpdates(entity, 1);
        assertThat(onPrecommit.call(connection)).isEqualTo(new Boolean(true));
        entityManagerImpl.flush();
        verify(transaction).commit();
     }

    @Test
    public void test_flush_not_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(false);
        entityManagerImpl.flush();
        verify(transaction).begin();
     }

    @Test
    public void test_close_active() throws Exception
    {
        RecordCategory entity = new RecordCategory();
        when(transaction.isActive()).thenReturn(true);
        prepareDoUpdates(entity, 1);
        assertThat(onPrecommit.call(connection)).isEqualTo(new Boolean(true));
        entityManagerImpl.close();
        assertThat(entityManagerImpl.isOpen).isFalse();
        verify(transaction).commit();
        verify(objectMonitor).release();
     }

    @Test
    public void test_close_not_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(false);
        entityManagerImpl.close();
        verify(objectMonitor).release();
    }

    @Test
    public void test_close_io_exception() throws Exception
    {
        RecordCategory entity = new RecordCategory();
        when(transaction.isActive()).thenReturn(true);
        prepareDoUpdates(entity, 1);
        Mockito.doThrow(new IOException()).when(connectionSource).close();
        entityManagerImpl.close();
        verify(transaction).commit();
        verify(objectMonitor).release();
     }

    @Test
    public void test_clear_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(true);
        entityManagerImpl.clear();
        verify(transaction).rollback();
        verify(objectMonitor).release();
        verify(transaction).begin();
     }

    @Test
    public void test_clear_not_active() throws Exception
    {
        when(transaction.isActive()).thenReturn(false);
        entityManagerImpl.clear();
        verify(transaction, never()).rollback();
        verify(objectMonitor).release();
        verify(transaction).begin();
     }

    @Test
    public void test_get_delegate() throws Exception
    {
        EntityManagerDelegate delegate = (EntityManagerDelegate) entityManagerImpl.getDelegate();
        assertThat(delegate.connectionSource).isEqualTo(connectionSource);
        assertThat(delegate.helperFactoryMap).isEqualTo(helperFactoryMap);
        assertThat(delegate.getTransaction()).isEqualTo(transaction);
    }

    @Test
    public void test_user_rollback() throws Exception
    {
        EntityTransaction testTransaction = entityManagerImpl.getTransaction(); 
        assertThat(testTransaction).isNotNull();
        assertThat(testTransaction).isNotEqualTo(transaction);
        testTransaction.begin();
        testTransaction.commit();
        Mockito.verifyZeroInteractions(transaction);
        testTransaction.setRollbackOnly();
        verify(transaction).setRollbackOnly();
        RecordCategory entity = new RecordCategory();
        when(transaction.isActive()).thenReturn(true);
        prepareDoUpdates(entity, 1);
        assertThat(onPrecommit.call(connection)).isEqualTo(new Boolean(true));
        entityManagerImpl.close();
        verify(transaction).commit(); // Will cause rollback because rollback only flagged
        verify(objectMonitor).release();
    }
    
    @Test
    public void test_user_transaction_mode() throws Exception
    {
        assertThat(entityManagerImpl.getTransaction()).isNotNull();
        assertThat(entityManagerImpl.getTransaction()).isNotEqualTo(transaction);
        entityManagerImpl.setUserTransaction(true);
        assertThat(entityManagerImpl.getTransaction()).isEqualTo(transaction);
    }
    
    @Test
    public void test_create_named_query() 
    {
        String QUERY_NAME = "my_query";
        NamedDaoQuery namedDaoQuery = mock(NamedDaoQuery.class);
        when(namedQueryMap.get(QUERY_NAME)).thenReturn(namedDaoQuery);
        Mockito.<Class<?>>when(namedDaoQuery.getEntityClass()).thenReturn(RecordCategory.class);
        @SuppressWarnings("rawtypes")
        EntityQuery entityQuery = mock(EntityQuery.class);
        when(namedDaoQuery.createQuery(dao)).thenReturn(entityQuery);
        Query result = entityManagerImpl.createNamedQuery(QUERY_NAME);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(entityQuery);
    }

    @Test
    public void test_create_sql_named_query() 
    {
        String QUERY_NAME = "my_sql_query";
        NamedSqlQuery namedSqlQuery = mock(NamedSqlQuery.class);
        when(namedQueryMap.get(QUERY_NAME)).thenReturn(null);
        when(namedSqlQueryMap.get(QUERY_NAME)).thenReturn(namedSqlQuery);
        Query query = mock(Query.class);
        when(namedSqlQuery.createQuery()).thenReturn(query);
        Query result = entityManagerImpl.createNamedQuery(QUERY_NAME);
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(query);
    }

    @Test
    public void test_create_named_query_not_found() 
    {
        String QUERY_NAME = "my_query";
        when(namedQueryMap.get(QUERY_NAME)).thenReturn(null);
        try
        {
            entityManagerImpl.createNamedQuery(QUERY_NAME);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Named query '" + QUERY_NAME + "' not found");
        }
    }
    
    @Test
    public void test_set_flush_mode()
    {
        entityManagerImpl.setFlushMode(FlushModeType.COMMIT);
        assertThat(entityManagerImpl.getFlushMode()).isEqualTo(FlushModeType.COMMIT);
        try
        {
            entityManagerImpl.setFlushMode(FlushModeType.AUTO);
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch(UnsupportedOperationException e)
        {
            assertThat(e.getMessage()).isEqualTo("FlushModeType.AUTO not supported");
        }
    }
    
    @Test 
    public void test_set_flush_mode_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.setFlushMode(FlushModeType.COMMIT);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("setFlushMode");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_get_flush_mode_after_close() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.getFlushMode();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).contains("getFlushMode");
            assertThat(e.getMessage()).contains("called after EntityManager has been closed");
        }
    }

    @Test 
    public void test_join_transaction() throws Exception
    { 
        try
        {
            entityManagerImpl.joinTransaction();
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch(UnsupportedOperationException e)
        {
            assertThat(e.getMessage()).isEqualTo("joinTransaction() not available");
        }
    }

    @Test 
    public void test_lock() throws Exception
    { 
        entityManagerImpl.isOpen = false;
        try
        {
            entityManagerImpl.lock(new Object(), LockModeType.NONE);
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        }
        catch(UnsupportedOperationException e)
        {
            assertThat(e.getMessage()).isEqualTo("lock() not available");
        }
    }
  
    private void prepareDoUpdates(RecordCategory entity, int resultCode) throws Exception
    {
        ArrayList<Object> objects = new ArrayList<Object>();
        objects.add(entity);
        when(objectMonitor.getObjectsToUpdate()).thenReturn(objects);
        when(ormDaoHelper.update(entity)).thenReturn(resultCode);
    }
    
    private RecordCategory prepareHelperMap()
    {
        RecordCategory entity = new RecordCategory();
        return entity;
        
    }
    /*
    private RecordCategory populateManagedObjects()
    {
        RecordCategory entity = new RecordCategory();
        ArrayList<Object> objects = new ArrayList<Object>();
        objects.add(entity);
        when(objectMonitor.getManagedObjects()).thenReturn(objects);
        return entity;
        
    }
    */
}