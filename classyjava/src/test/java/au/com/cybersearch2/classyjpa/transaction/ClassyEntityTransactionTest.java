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
package au.com.cybersearch2.classyjpa.transaction;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import dagger.Module;
import dagger.Provides;

/**
 * ClassyEntityTransactionTest
 * @author Andrew Bowley
 * 18/07/2014
 */
public class ClassyEntityTransactionTest
{
    @Module(injects = { 
            PersistenceContext.class })
    class ClassyEntityTransactionTestModule implements ApplicationModule
    {
        @Provides @Singleton PersistenceFactory providePersistenceFactory()
        {
            PersistenceFactory persistenceFactory = mock(PersistenceFactory.class);
            DatabaseSupport databaseSupport = mock(DatabaseSupport.class);
            when(persistenceFactory.getDatabaseSupport()).thenReturn(databaseSupport);
            return persistenceFactory;
        }
    }
    class CallableBase
    {
        PersistenceException persistenceException;
        IllegalArgumentException illegalArgumentException;
        IllegalStateException illegalStateException;
        UnsupportedOperationException unsupportedOperationException;
        // Sample RuntimeException
        boolean doThrowNpe;
        NullPointerException nullPointerException;
        SQLException sqlException;
        Boolean result = Boolean.valueOf(true);
        boolean isCalled;
        
        @SuppressWarnings("null")
        protected void throwExceptionIfSet() throws Exception
        {
            isCalled = true;
            if (persistenceException != null)
                throw persistenceException;
            if (illegalArgumentException != null)
                throw illegalArgumentException;
            if (illegalStateException != null)
                throw illegalStateException;
            if (unsupportedOperationException != null)
                throw unsupportedOperationException;
            if (doThrowNpe)
                try
                {
                    Object object = null;
                    object.toString();
                }
                catch(NullPointerException e)
                {
                    nullPointerException = e;
                    throw(e);
                }
            if (sqlException != null)
                throw sqlException;
        }
    }
    
    class TestPrecommitCallable extends CallableBase implements TransactionCallable
    {

        @Override
        public Boolean call(DatabaseConnection databaseConnection) throws Exception 
        {
            throwExceptionIfSet();
            return result;
        }
        
    }

    class TestPostcommitCallable extends CallableBase implements Callable<Boolean>
    {

        @Override
        public Boolean call() throws Exception 
        {
            throwExceptionIfSet();
            return result;
        }
        
    }

    protected ConnectionSource connectionSource;
 
    @Before
    public void setUp()
    {
        new DI(new ClassyEntityTransactionTestModule());
        connectionSource = mock(ConnectionSource.class);
    }
    
    @Test
    public void test_begin()
    {
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        assertThat(transaction.transactionState).isEqualTo(transaction.mockTransactionState);
        assertThat(transaction.isActive()).isTrue();
    }

    @Test
    public void test_begin_already_active()
    {
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        try
        {
            transaction.begin();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch (IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("begin() called while active");
        }
    }

    @Test
    public void test_begin_sql_exception()
    {
        SQLException exception = new SQLException("Database error");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.sqlException = exception;
        try
        {
            transaction.begin();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("begin transaction error Database error");
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test
    public void test_commit() throws SQLException
    {
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_commit_not_active()
    {
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch (IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("commit() called while not active");
        }
    }
    
    @Test
    public void test_commit_sql_exception() throws SQLException
    {
        SQLException exception = new SQLException("Database error");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        doThrow(exception).when(transaction.mockTransactionState).doCommit();
        try
        {
            transaction.commit();
           failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Exception on commit/rollback: Database error");
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test
    public void test_commit_rollback_only() throws SQLException
    {
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        assertThat(transaction.getRollbackOnly()).isFalse();
        transaction.setRollbackOnly();
        assertThat(transaction.getRollbackOnly()).isTrue();
        transaction.commit();
        verify(transaction.mockTransactionState).doRollback();
        assertThat(transaction.isActive()).isFalse();
        transaction.begin();
        assertThat(transaction.getRollbackOnly()).isFalse();
    }

    @Test
    public void test_commit_rollback_only_sql_exception() throws SQLException
    {
        SQLException exception = new SQLException("Database error");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        transaction.setRollbackOnly();
       doThrow(exception).when(transaction.mockTransactionState).doRollback();
        try
        {
            transaction.commit();
           failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch (PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Exception on commit/rollback: Database error");
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test
    public void test_rollback() throws SQLException
    {
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        transaction.rollback();
        verify(transaction.mockTransactionState).doRollback();
        assertThat(transaction.isActive()).isFalse();
     }

    @Test
    public void test_rollback_sql_exception() throws SQLException
    {
        SQLException exception = new SQLException("Database error");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource);
        transaction.begin();
        doThrow(exception).when(transaction.mockTransactionState).doRollback();
        // Do not throw exception from rollback so it can be called from a finally clause
        transaction.rollback();
    }

    @Test
    public void test_precommit() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState).doCommit();
        assertThat(transaction.isActive()).isFalse();
        assertThat(callable.isCalled).isTrue();
    }

    @Test
    public void test_precommit_fail() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.result = Boolean.FALSE;
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit failure caused rollback");
             assertThat(e.getCause()).isEqualTo(callable.sqlException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_persistence_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.persistenceException = new PersistenceException("Not an entity");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
             assertThat(e.getCause()).isEqualTo(callable.persistenceException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_illegalArgument_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.illegalArgumentException = new IllegalArgumentException("Invalid parameter");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
             assertThat(e.getCause()).isEqualTo(callable.illegalArgumentException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_illegalStateException_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.illegalStateException = new IllegalStateException("Invalid state");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
             assertThat(e.getCause()).isEqualTo(callable.illegalStateException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_unsupportedOperation_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.unsupportedOperationException = new UnsupportedOperationException("Not supported");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
             assertThat(e.getCause()).isEqualTo(callable.unsupportedOperationException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_nullpointer_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.doThrowNpe = true;
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            assertThat(e).isEqualTo(callable.nullPointerException);
        }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_sql_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.sqlException = new SQLException("Database kaput!");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit operation failed");
             assertThat(e.getCause()).isEqualTo(callable.sqlException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_precommit_rollback_sql_exception() throws SQLException
    {
        TestPrecommitCallable callable = new TestPrecommitCallable();
        callable.result = Boolean.FALSE;
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, callable);
        transaction.begin();
        SQLException exception = new SQLException("Database error");
        doThrow(exception).when(transaction.mockTransactionState).doRollback();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit failure caused rollback");
             assertThat(e.getCause()).isEqualTo(callable.sqlException);
         }
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        assertThat(callable.isCalled);
        verify(transaction.mockTransactionState).doCommit();
        assertThat(transaction.isActive()).isFalse();
        assertThat(callable.isCalled).isTrue();
    }

    @Test
    public void test_postcommit_fail() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.result = Boolean.FALSE;
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_persistence_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.persistenceException = new PersistenceException("Not an entity");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_illegalArgument_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.illegalArgumentException = new IllegalArgumentException("Invalid parameter");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_illegalStateException_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.illegalStateException = new IllegalStateException("Invalid state");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_unsupportedOperation_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.unsupportedOperationException = new UnsupportedOperationException("Not supported");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_nullpointer_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.doThrowNpe = true;
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(NullPointerException.class);
        }
        catch (NullPointerException e)
        {
            assertThat(e).isEqualTo(callable.nullPointerException);
        }
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_sql_exception() throws SQLException
    {
        TestPostcommitCallable callable = new TestPostcommitCallable();
        callable.sqlException = new SQLException("Database kaput!");
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, null , callable);
        transaction.begin();
        transaction.commit();
        verify(transaction.mockTransactionState, times(0)).doRollback();
        verify(transaction.mockTransactionState, times(1)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

    @Test
    public void test_postcommit_rollback_sql_exception() throws SQLException
    {
        TestPostcommitCallable postcallable = new TestPostcommitCallable();
        TestPrecommitCallable precallable = new TestPrecommitCallable();
        precallable.result = Boolean.FALSE;
        TestClassyEntityTransaction transaction = new TestClassyEntityTransaction(connectionSource, precallable , postcallable);
        transaction.begin();
        // This just gets logged
        SQLException exception = new SQLException("Database error");
        doThrow(exception).when(transaction.mockTransactionState).doRollback();
        try
        {
            transaction.commit();
            failBecauseExceptionWasNotThrown(PersistenceException.class);
         }
         catch (PersistenceException e)
         {
             assertThat(e.getMessage()).isEqualTo("Pre commit failure caused rollback");
             assertThat(e.getCause()).isNull();
         }
        assertThat(postcallable.isCalled).isFalse();
        verify(transaction.mockTransactionState, times(1)).doRollback();
        verify(transaction.mockTransactionState, times(0)).doCommit();
        assertThat(transaction.isActive()).isFalse();
    }

}
