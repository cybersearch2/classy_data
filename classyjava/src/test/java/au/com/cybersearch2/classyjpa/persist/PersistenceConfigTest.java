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
package au.com.cybersearch2.classyjpa.persist;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.NamedDaoQuery;
import au.com.cybersearch2.classyjpa.query.NamedSqlQuery;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;

/**
 * PersistenceConfigTest
 * @author Andrew Bowley
 * 12/05/2014
 */
public class PersistenceConfigTest
{
    private static final String QUERY_NAME = "my_query";

    
    @Before
    public void setUp() throws Exception 
    {
    }
    
    @Test
    public void test_addNamedQuery()
    {
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        DaoQueryFactory daoQueryFactory = mock(DaoQueryFactory.class);
        @SuppressWarnings("unchecked")
        Map<String,NamedDaoQuery> namedQueryMap = mock(Map.class);
        persistenceConfig.namedQueryMap = namedQueryMap;
        when(namedQueryMap.containsKey(QUERY_NAME)).thenReturn(false);
        persistenceConfig.addNamedQuery(RecordCategory.class, QUERY_NAME, daoQueryFactory);
        verify(namedQueryMap).put(eq(QUERY_NAME), isA(NamedDaoQuery.class));
    }
    
    @Test
    public void test_addNamedQuery_already_exists()
    {   // Check error does not throw exeption or change existing setting
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        DaoQueryFactory daoQueryFactory = mock(DaoQueryFactory.class);
        @SuppressWarnings("unchecked")
        Map<String,NamedDaoQuery> namedQueryMap = mock(Map.class);
        persistenceConfig.namedQueryMap = namedQueryMap;
        when(namedQueryMap.containsKey(QUERY_NAME)).thenReturn(true);
        persistenceConfig.addNamedQuery(RecordCategory.class, QUERY_NAME, daoQueryFactory);
        verify(namedQueryMap, times(0)).put(eq(QUERY_NAME), isA(NamedDaoQuery.class));
    }

    @Test
    public void test_addNamedSqlQuery()
    {
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        SqlQueryFactory sqlQueryFactory = mock(SqlQueryFactory.class);
        QueryInfo queryInfo = mock(QueryInfo.class);
        @SuppressWarnings("unchecked")
        Map<String,NamedSqlQuery> namedSqlQueryMap = mock(Map.class);
        persistenceConfig.nativeQueryMap = namedSqlQueryMap;
        when(namedSqlQueryMap.containsKey(QUERY_NAME)).thenReturn(false);
        persistenceConfig.addNamedQuery(QUERY_NAME, queryInfo, sqlQueryFactory);
        verify(namedSqlQueryMap).put(eq(QUERY_NAME), isA(NamedSqlQuery.class));
    }
    
    @Test
    public void test_addNamedSqlQuery_already_exists()
    {   // Check error does not throw exeption or change existing setting
        PersistenceConfig persistenceConfig = new PersistenceConfig(new SqliteDatabaseType());
        SqlQueryFactory sqlQueryFactory = mock(SqlQueryFactory.class);
        QueryInfo queryInfo = mock(QueryInfo.class);
        @SuppressWarnings("unchecked")
        Map<String,NamedSqlQuery> namedSqlQueryMap = mock(Map.class);
        persistenceConfig.nativeQueryMap = namedSqlQueryMap;
        when(namedSqlQueryMap.containsKey(QUERY_NAME)).thenReturn(true);
        persistenceConfig.addNamedQuery(QUERY_NAME, queryInfo, sqlQueryFactory);
        verify(namedSqlQueryMap, times(0)).put(eq(QUERY_NAME), isA(NamedSqlQuery.class));
    }
}
