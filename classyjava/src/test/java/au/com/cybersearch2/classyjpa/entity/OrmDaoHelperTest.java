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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

import com.j256.ormlite.support.ConnectionSource;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

/**
 * OrmDaoHelperTest
 * @author Andrew Bowley
 * 03/05/2014
 */
public class OrmDaoHelperTest
{
/*    

    // Overide internal methods which create concrete objects to replace them with mocks 
    class OrmDaoHelper<T,ID> extends OrmDaoHelper<T,ID>
    {
        @SuppressWarnings("unchecked")
        PersistenceDao<RecordCategory, ID> dao = mock(PersistenceDao.class);
        MockTableCreator tableCreator = mock(MockTableCreator.class);
        ConnectionSource connectionSource;

        public OrmDaoHelper(Class<T> entityClass)
        {
            super(entityClass);
            connectionSource = null;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public PersistenceDao<T, ID> getDao(ConnectionSource connectionSource) 
        {
            this.connectionSource = connectionSource;
            return (PersistenceDao<T, ID>)dao;
        }
        @Override
        protected void createEntityTable(Class<T> clazz, ConnectionSource connectionSource) throws SQLException
        {
            this.connectionSource = connectionSource;
            tableCreator.createTable(clazz, connectionSource);
        }
    }
    
    @Test 
    public void test_create_for_no_entity_table_case() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(RecordCategory.class);
        when(dao.isTableExists()).thenReturn(false);
        RecordCategory entity = new RecordCategory();
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        helper.create(entity, connectionSource);
        verify(helper.tableCreator).createTable(RecordCategory.class, connectionSource);
        verify(dao).create(entity);
        assertThat(helper.connectionSource).isEqualTo(connectionSource);
    }

    @Test 
    public void test_create_for_entity_table_exists_case() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(RecordCategory.class);
        when(dao.isTableExists()).thenReturn(true);
        RecordCategory entity = new RecordCategory();
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        helper.create(entity, connectionSource);
        verifyZeroInteractions(helper.tableCreator);
        verify(dao).create(entity);
        assertThat(helper.connectionSource).isEqualTo(connectionSource);
    }

    @Test 
    public void test_create_sql_exception() throws Exception
    {
        RecordCategory entity = new RecordCategory();
        ConnectionSource connectionSource = mock(ConnectionSource.class);
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(RecordCategory.class);
        when(dao.isTableExists()).thenReturn(false);
        SQLException exception = new SQLException();
        Mockito.doThrow(exception).when(helper.tableCreator).createTable(RecordCategory.class, connectionSource);
        try
        {
            helper.create(entity, connectionSource);
            failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch(RuntimeException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error creating table for class " + RecordCategory.class.getName());
            assertThat(e.getCause()).isEqualTo(exception);
        }
    }
*/
    PersistenceDao<RecordCategory, Integer> dao;

    @SuppressWarnings("unchecked")
    @Before
    public void setup()
    {
        dao = mock(PersistenceDao.class);
    }
    
    @Test
    public void test_query_for_id() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity = new RecordCategory();
        Integer id = new Integer(1);
        when(dao.queryForId(id)).thenReturn(entity);
        assertThat(helper.queryForId(id)).isEqualTo(entity);
    }
    
    @Test
    public void test_query_for_same_id() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        RecordCategory entity2 = new RecordCategory();
        when(dao.queryForSameId(entity1)).thenReturn(entity2);
        assertThat(helper.queryForSameId(entity1)).isEqualTo(entity2);
    }

    @Test
    public void test_extract_id() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        Integer id = new Integer(1);
        when(dao.extractId(entity1)).thenReturn(id);
        assertThat(helper.extractId(entity1)).isEqualTo(id);
    }

    @Test
    public void test_entity_exists() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        Integer id = new Integer(1);
        when(dao.extractId(entity1)).thenReturn(id);
        when(dao.isTableExists()).thenReturn(true);
        when(dao.idExists(id)).thenReturn(true);
        assertThat(helper.entityExists(entity1)).isTrue();
    }

    @Test
    public void test_update() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        when(dao.update(entity1)).thenReturn(1);
        assertThat(helper.update(entity1)).isEqualTo(1);
    }

    @Test
    public void test_delete() throws Exception
    {
        OrmDaoHelper<RecordCategory, Integer> helper = new OrmDaoHelper<RecordCategory, Integer>(dao);
        RecordCategory entity1 = new RecordCategory();
        when(dao.delete(entity1)).thenReturn(1);
        assertThat(helper.delete(entity1)).isEqualTo(1);
    }
}
