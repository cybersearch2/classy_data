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
package au.com.cybersearch2.classyjpa.query;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import java.util.Collections;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

/**
 * DaoQueryTest
 * Note: selectionArguments methods tested in EntityQueryTest
 * @author Andrew Bowley
 * 10/07/2014
 */
public class DaoQueryTest
{
    private static final int OFFSET = 17;
    private static final int LIMIT = 100;
    protected PersistenceDao<RecordCategory, Integer> persistenceDao;
    protected PreparedQuery<RecordCategory> preparedQuery;
    protected QueryBuilder<RecordCategory, Integer> statementBuilder;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp()
    {
        persistenceDao = mock(PersistenceDao.class);    
        preparedQuery = mock(PreparedQuery.class);
        statementBuilder = mock(QueryBuilder.class);
    }
    
    @Test
    public void test_prepare() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = new DaoQuery<RecordCategory>(persistenceDao){

            @Override
            protected QueryBuilder<RecordCategory, ?> buildQuery(
                    QueryBuilder<RecordCategory, ?> statementBuilder)
                    throws SQLException {
                return statementBuilder;
            }};
            
            int startPosition = OFFSET;
            int maxResults = LIMIT;
            when(persistenceDao.queryBuilder()).thenReturn(statementBuilder );
            when(statementBuilder.prepare()).thenReturn(preparedQuery);
            PreparedQuery<RecordCategory> result = daoQuery.prepare(startPosition, maxResults);  
            verify(statementBuilder).offset(Long.valueOf(startPosition));
            verify(statementBuilder).limit(Long.valueOf(maxResults));
            assertThat(result).isEqualTo(preparedQuery);
    }

    @Test
    public void test_prepare_sql_exception() throws SQLException
    {
        SQLException sqlException = new SQLException("Offset out of bounds");
        DaoQuery<RecordCategory> daoQuery = new DaoQuery<RecordCategory>(persistenceDao){

            @Override
            protected QueryBuilder<RecordCategory, ?> buildQuery(
                    QueryBuilder<RecordCategory, ?> statementBuilder)
                    throws SQLException {
                return statementBuilder;
            }};
        int startPosition = OFFSET;
        int maxResults = LIMIT;
        when(persistenceDao.queryBuilder()).thenReturn(statementBuilder );
        when(statementBuilder.offset(Long.valueOf(startPosition))).thenThrow(sqlException);
        try
        {
            daoQuery.prepare(startPosition, maxResults); 
            failBecauseExceptionWasNotThrown(PersistenceException.class);
        }
        catch(PersistenceException e)
        {
            assertThat(e.getMessage()).isEqualTo("Error preparing query");
            assertThat(e.getCause()).isEqualTo(sqlException);
        }
    }
  
    @Test
    public void test_getResultList() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = prepareQuery(OFFSET, LIMIT);
        List<RecordCategory> testList = Collections.singletonList(new RecordCategory());
        when(persistenceDao.query(preparedQuery)).thenReturn(testList );
        List<RecordCategory> result = daoQuery.getResultList(OFFSET, LIMIT);
        assertThat(result).isEqualTo(testList);
        verify(statementBuilder).offset(Long.valueOf(OFFSET));
        verify(statementBuilder).limit(Long.valueOf(LIMIT));
    }
 
    @Test
    public void test_getResultList_default() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = prepareQuery(0, 0);
        List<RecordCategory> testList = Collections.singletonList(new RecordCategory());
        when(persistenceDao.query(preparedQuery)).thenReturn(testList );
        List<RecordCategory> result = daoQuery.getResultList(0, 0);
        assertThat(result).isEqualTo(testList);
        verify(statementBuilder, times(0)).offset(Long.valueOf(OFFSET));
        verify(statementBuilder, times(0)).limit(Long.valueOf(LIMIT));
    }
 
    @Test
    public void test_getSingleResult() throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = prepareQuery(0, 1);
        RecordCategory recordCategory = new RecordCategory();
        when(persistenceDao.queryForFirst(preparedQuery)).thenReturn(recordCategory);
        RecordCategory result = daoQuery.getSingleResult();
        assertThat(result).isEqualTo(recordCategory);
        verify(statementBuilder, times(0)).offset(Long.valueOf(0));
        verify(statementBuilder).limit(Long.valueOf(1));
    }
    
    protected DaoQuery<RecordCategory> prepareQuery(int startPosition, int maxResults) throws SQLException
    {
        DaoQuery<RecordCategory> daoQuery = new DaoQuery<RecordCategory>(persistenceDao){

            @Override
            protected QueryBuilder<RecordCategory, ?> buildQuery(
                    QueryBuilder<RecordCategory, ?> statementBuilder)
                    throws SQLException {
                return statementBuilder;
            }};
            
            when(persistenceDao.queryBuilder()).thenReturn(statementBuilder );
            when(statementBuilder.prepare()).thenReturn(preparedQuery);
            return daoQuery;
    }

}
