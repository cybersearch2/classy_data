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

import java.sql.SQLException;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * OrmDaoHelperFactoryTest
 * @author Andrew Bowley
 * 19/08/2014
 */
public class OrmDaoHelperFactoryTest
{
    // Overide internal methods which create concrete objects to replace them with mocks 
    class TestOrmDaoHelperFactory extends OrmDaoHelperFactory<RecordCategory,Integer>
    {
        @SuppressWarnings("unchecked")
        PersistenceDao<RecordCategory, Integer> dao = mock(PersistenceDao.class);
        ConnectionSource connectionSource;
        boolean tableCreated;
        SQLException toThrowOnTableCreate;
        SQLException toThrowOnDaoCreate;

        public TestOrmDaoHelperFactory()
        {
            super(RecordCategory.class);
            connectionSource = null;
        }
        
        @Override
        protected void createTable(ConnectionSource connectionSource) throws SQLException 
        {
            this.connectionSource = connectionSource;
            if (toThrowOnTableCreate != null)
                throw toThrowOnTableCreate;
            tableCreated = true;
        }
        
        @Override
        protected PersistenceDao<RecordCategory,Integer> createDao(ConnectionSource connectionSource) throws SQLException
        {
            this.connectionSource = connectionSource;
            if (toThrowOnDaoCreate != null)
                throw toThrowOnDaoCreate;
            return dao;
        }
    }
    
    @Test 
    public void test_create_for_no_entity_table_case() throws Exception
    {
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        when(helperFactory.dao.isTableExists()).thenReturn(false);
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        OrmDaoHelper<RecordCategory, Integer> ormDaoHelper = helperFactory.getOrmDaoHelper(connectionSource);
        assertThat(ormDaoHelper).isNotNull();
        assertThat(helperFactory.tableCreated).isTrue();
        assertThat(helperFactory.connectionSource).isEqualTo(connectionSource);
    }

    @Test 
    public void test_create_for_entity_table_exists_case() throws Exception
    {
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        when(helperFactory.dao.isTableExists()).thenReturn(true);
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        OrmDaoHelper<RecordCategory, Integer> ormDaoHelper = helperFactory.getOrmDaoHelper(connectionSource);
        assertThat(ormDaoHelper).isNotNull();
        assertThat(helperFactory.tableCreated).isFalse();
        assertThat(helperFactory.connectionSource).isEqualTo(connectionSource);
    }
    
    @Test 
    public void test_table_create_sql_exception() throws Exception
    {
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        when(helperFactory.dao.isTableExists()).thenReturn(false);
        SQLException exception = new SQLException();
        helperFactory.toThrowOnTableCreate = exception;
        try
        {
            helperFactory.getOrmDaoHelper(connectionSource);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch(RuntimeException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error creating table for class " + RecordCategory.class.getName());
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }

    @Test 
    public void test_dao_create_sql_exception() throws Exception
    {
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        TestOrmDaoHelperFactory helperFactory = new TestOrmDaoHelperFactory();
        SQLException exception = new SQLException();
        helperFactory.toThrowOnDaoCreate = exception;
        try
        {
            helperFactory.getOrmDaoHelper(connectionSource);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch(RuntimeException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error creating DAO for class " + RecordCategory.class.getName());
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }
}
