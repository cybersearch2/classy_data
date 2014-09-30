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
import static org.mockito.Mockito.*;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.query.QueryInfo.RowMapper;

/**
 * SqlQueryTest
 * @author Andrew Bowley
 * 17/07/2014
 */
public class SqlQueryTest
{

    static final String PU_NAME = "acme-enterprise";
    static final String DATABASE_NAME = "acme-enterprise.db";
    static final String SQL_STATEMENT = 
    "SELECT Employees.LastName, COUNT(Orders.OrderID) AS NumberOfOrders FROM Orders " +
    "INNER JOIN Employees " +
    "ON Orders.EmployeeID=Employees.EmployeeID " +
    "WHERE LastName=? OR LastName=? " +
    "GROUP BY LastName " +
    "HAVING COUNT(Orders.OrderID) > 25 " +
    "ORDER BY NumberOfOrders";
    static final String SQL_TABLES = "Orders INNER JOIN Employees ON Orders.EmployeeID=Employees.EmployeeID";
    static final String[] SQL_COLUMNS = { "Employees.LastName", "COUNT(Orders.OrderID) AS NumberOfOrders" }; 
    static final String SQL_SELECTION = "LastName=? OR LastName=?";
    static final String SQL_GROUP_BY = "LastName";
    static final String SQL_HAVING = "COUNT(Orders.OrderID) > 25";
    static final String SQL_ORDER_BY = "NumberOfOrders";
    static final String SQL_LIMIT = "20";
    static Date CREATED;

    protected SqlQuery sqlQuery;
    protected PersistenceAdmin persistenceAdmin;
    protected QueryInfo queryInfo;

    @Before
    public void setUp()
    {
        persistenceAdmin = mock(PersistenceAdmin.class);
        queryInfo = getTestQueryInfo();
        sqlQuery = new SqlQuery(persistenceAdmin, queryInfo);
        Calendar cal = GregorianCalendar.getInstance(Locale.US);
        cal.set(2014, 5, 25, 5, 17, 23);
        CREATED = cal.getTime();
    }

    @Test
    public void test_getResultObjectList()
    {
        sqlQuery.selectionArgs.add(queryInfo.selectionArgs[0]);
        sqlQuery.selectionArgs.add(queryInfo.selectionArgs[1]);
        queryInfo.selectionArgs = null;
        Object object = new Object();
        when(persistenceAdmin.getResultList(queryInfo, 0, 0)).thenReturn((List<Object>)Collections.singletonList(object));
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) sqlQuery.getResultObjectList();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(object);
        assertThat(queryInfo.selectionArgs.length).isEqualTo(2);
        assertThat(queryInfo.selectionArgs[0]).isEqualTo("Brown");
        assertThat(queryInfo.selectionArgs[1]).isEqualTo("Smith");
    }
    
    @Test
    public void test_getResultObject()
    {
        sqlQuery.selectionArgs.add(queryInfo.selectionArgs[0]);
        sqlQuery.selectionArgs.add(queryInfo.selectionArgs[1]);
        queryInfo.selectionArgs = null;
        Object object = new Object();
        when(persistenceAdmin.getSingleResult(queryInfo)).thenReturn(object);
        Object result = sqlQuery.getResultObject();
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(object);
        assertThat(queryInfo.selectionArgs.length).isEqualTo(2);
        assertThat(queryInfo.selectionArgs[0]).isEqualTo("Brown");
        assertThat(queryInfo.selectionArgs[1]).isEqualTo("Smith");
    }

    @Test
    public void test_setParam_by_index()
    {
        assertThat(sqlQuery.setParam(1, "Jones")).isEqualTo(true);
        assertThat(sqlQuery.setParam(2, "Ng")).isEqualTo(true);
        assertThat(sqlQuery.setParam(0, "Xerces")).isEqualTo(false);
        assertThat(sqlQuery.setParam(3, "Xenon")).isEqualTo(false);
        assertThat(sqlQuery.setParam(2, CREATED)).isEqualTo(true);
        assertThat(sqlQuery.selectionArgs.get(1)).isEqualTo("2014-06-25 05:17:23.000000");
    }
    
    @Test
    public void test_setParam_by_name()
    {
        assertThat(sqlQuery.setParam("lastname1", "Jones")).isEqualTo(true);
        assertThat(sqlQuery.setParam("lastname2", "Ng")).isEqualTo(true);
        assertThat(sqlQuery.setParam("XXXX", "Xerces")).isEqualTo(false);
        assertThat(sqlQuery.setParam("lastname2", CREATED)).isEqualTo(true);
        assertThat(sqlQuery.selectionArgs.get(1)).isEqualTo("2014-06-25 05:17:23.000000");
    }
 
    @Test
    public void test_toString()
    {
        assertThat(sqlQuery.toString()).isEqualTo("Named query for '" + SQL_SELECTION + "'");
    }
    
    protected QueryInfo getTestQueryInfo()
    {
        RowMapper rowMapper = mock(RowMapper.class);
        QueryInfo queryInfo = new QueryInfo(rowMapper, SQL_TABLES, SQL_COLUMNS);
        queryInfo.setGroupBy(SQL_GROUP_BY);
        queryInfo.setHaving(SQL_HAVING);
        queryInfo.setLimit(SQL_LIMIT);
        queryInfo.setOrderBy(SQL_ORDER_BY);
        queryInfo.setParameterNames(new String[]{ "lastname1", "lastname2" });
        queryInfo.setSelection(SQL_SELECTION);
        queryInfo.setSelectionArgs(new String[]{ "Brown", "Smith" });
        return queryInfo;
    }
}
