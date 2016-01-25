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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import android.content.ContentResolver;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.QueryInfo.RowMapper;
import au.com.cybersearch2.classyjpa.query.ResultRow;

/**
 * AndroidDatabaseSupportTest
 * @author Andrew Bowley
 * 10/07/2014
 */
@RunWith(RobolectricTestRunner.class)
public class AndroidDatabaseSupportTest
{
    static class QueryParams
    {
        String table; 
        String[] columns;
        String selection; 
        String[] selectionArgs;
        String groupBy; 
        String having; 
        String orderBy;
        String limit;
        Cursor cursor;
        
        public QueryParams()
        {
            cursor = mock(Cursor.class);
        }

    }
    
    static class TestAndroidDatabaseSupport extends AndroidDatabaseSupport
    {
        QueryParams queryParams = new QueryParams();
        
        @Override
        protected SQLiteQueryExecutor getSQLiteQueryExecutor(ConnectionSource connectionSource)
        {
            
            return new SQLiteQueryExecutor(){

                @Override
                public Cursor query(String table, String[] columns,
                        String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy,
                        String limit) {
                    queryParams.table = table;
                    queryParams.columns = columns;
                    queryParams.selection = selection;
                    queryParams.selectionArgs = selectionArgs;
                    queryParams.groupBy = groupBy;
                    queryParams.having = having;
                    queryParams.orderBy = orderBy;
                    queryParams.limit = limit;
                    return queryParams.cursor;
                }};
        }

        protected SQLiteDatabase getSQLiteDatabase(SQLiteOpenHelper sqLiteOpenHelper)
        {
        	return sqLiteDatabase;
        }
   }

    static class TestCursor implements Cursor
    {
        @Override
        public Uri getNotificationUri ()
        {
            return null;
        }
        
        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public int getPosition() {
            return 5;
        }

        @Override
        public boolean move(int offset) {
            return false;
        }

        @Override
        public boolean moveToPosition(int position) {
            return false;
        }

        @Override
        public boolean moveToFirst() {
            return false;
        }

        @Override
        public boolean moveToLast() {
            return false;
        }

        @Override
        public boolean moveToNext() {
            return false;
        }

        @Override
        public boolean moveToPrevious() {
            return false;
        }

        @Override
        public boolean isFirst() {
            return false;
        }

        @Override
        public boolean isLast() {
            return false;
        }

        @Override
        public boolean isBeforeFirst() {
            return false;
        }

        @Override
        public boolean isAfterLast() {
            return false;
        }

        @Override
        public int getColumnIndex(String columnName) {
            return COLUMN_NAME.equals(columnName) ? 1 : -1;
        }

        @Override
        public int getColumnIndexOrThrow(String columnName)
                throws IllegalArgumentException {
            return 0;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return null;
        }

        @Override
        public String[] getColumnNames() {
            return COLUMN_NAMES;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public byte[] getBlob(int columnIndex) {
            return columnIndex == 3 ? TEST_BLOB : new byte[0];
        }

        @Override
        public String getString(int columnIndex) {
            return columnIndex == 7 ? DB_TEXT : "";
        }

        @Override
        public void copyStringToBuffer(int columnIndex,
                CharArrayBuffer buffer) {
            if (columnIndex == 9)
            {
                buffer.data = DB_TEXT.toCharArray();
                buffer.sizeCopied = DB_TEXT.length();
            }
        }

        @Override
        public short getShort(int columnIndex) {
            return columnIndex == 3 ? (short)1234 : (short)-1;
        }

        @Override
        public int getInt(int columnIndex) {
            return columnIndex == 3 ? Integer.MAX_VALUE : -1;
        }

        @Override
        public long getLong(int columnIndex) {
            return columnIndex == 3 ? Long.MAX_VALUE : -1;
        }

        @Override
        public float getFloat(int columnIndex) {
            return columnIndex == 3 ? Float.MAX_VALUE : -1f;
        }

        @Override
        public double getDouble(int columnIndex) {
            return columnIndex == 3 ? Double.MAX_VALUE : -1d;
        }

        @Override
        public int getType(int columnIndex) {
            return 0;
        }

        @Override
        public boolean isNull(int columnIndex) {
            return columnIndex == 3;
        }

        @Override
        public void deactivate() {
        }

        @Override
        public boolean requery() {
            return false;
        }

        @Override
        public void close() {
        }

        @Override
        public boolean isClosed() {
            return false;
        }

        @Override
        public void registerContentObserver(ContentObserver observer) {
        }

        @Override
        public void unregisterContentObserver(ContentObserver observer) {
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        @Override
        public void setNotificationUri(ContentResolver cr, Uri uri) {
        }

        @Override
        public boolean getWantsAllOnMoveCalls() {
            return false;
        }

        @Override
        public Bundle getExtras() {
            return null;
        }

        @Override
        public Bundle respond(Bundle extras) {
            return null;
        }
        
        @Override
        public void setExtras (Bundle extras) {
        }
    }   
    static final String COLUMN_NAME = "ID";
    static final String[] COLUMN_NAMES = new String[] { COLUMN_NAME, "Description" };
    static final byte[] TEST_BLOB = "This is a test 123!".getBytes();
    static final String DB_TEXT = "Acme Roadrunner Pty Ltd";

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
    static final FieldType[] fieldTypes = new FieldType[] {};
    
    protected AndroidDatabaseSupport sqLiteDatabaseSupport;
    protected ConnectionSource connectionSource;
    protected DatabaseConnection dbConnection;
    protected AndroidConnectionSourceFactory androidConnectionSourceFactory;
    protected OpenHelperConnectionSource openHelperConnectionSource;
    protected Context context;
    protected Properties properties;
    static protected SQLiteDatabase sqLiteDatabase;
    
    @Before
    public void setUp() throws SQLException
    {
        properties = new Properties();
        properties.setProperty(PersistenceUnitInfoImpl.PU_NAME_PROPERTY, PU_NAME);
        context = mock(Context.class);
        sqLiteDatabase = mock(SQLiteDatabase.class);
        sqLiteDatabaseSupport = new TestAndroidDatabaseSupport();
        openHelperConnectionSource = mock(OpenHelperConnectionSource.class);
        androidConnectionSourceFactory = mock(AndroidConnectionSourceFactory.class);
        when(androidConnectionSourceFactory.getConnectionSource(eq(DATABASE_NAME), isA(Properties.class))).thenReturn(openHelperConnectionSource);
        connectionSource = mock(ConnectionSource.class);
        dbConnection = mock(DatabaseConnection.class);
        when(connectionSource.getReadWriteConnection()).thenReturn(dbConnection);
    }

    @Test
    public void test_constructor()
    {
        assertThat(sqLiteDatabaseSupport.androidSQLiteMap).isNotNull();
    }
    /*
    @Test
    public void test_getAndroidSQLiteConnection()
    {
        Properties testProperties = new Properties();
        testProperties.putAll(properties);
        testProperties.setProperty(PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY, "au.com.cybersearch2.classyapp.TestOpenHelperCallbacks");
        OpenHelperConnectionSource result = 
        		(OpenHelperConnectionSource) sqLiteDatabaseSupport.getConnectionSource(DATABASE_NAME, testProperties);
        assertThat(result).isNotNull();
        assertThat(result.sqLiteOpenHelper).isInstanceOf(OpenEventHandler.class);
        assertThat(sqLiteDatabaseSupport.androidSQLiteMap.get(DATABASE_NAME)).isEqualTo(result);
        OpenHelperConnectionSource result2 = 
        		(OpenHelperConnectionSource) sqLiteDatabaseSupport.getConnectionSource(DATABASE_NAME, properties);
        assertThat(result2).isEqualTo(result);
    }

    @Test
    public void test_createSQLiteOpenHelper()
    {
        OpenHelperCallbacks testOpenCallbacks = mock(OpenHelperCallbacks.class);
        OpenEventHandler openEventHandler = new OpenEventHandler(testOpenCallbacks);
    	SQLiteOpenHelper sqLiteOpenHelper = sqLiteDatabaseSupport.createSQLiteOpenHelper(DATABASE_NAME + "3", 1, context, openEventHandler);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
		OpenHelperConnectionSource conn3 = new OpenHelperConnectionSource(db, sqLiteOpenHelper, testOpenCallbacks);
        sqLiteDatabaseSupport.androidSQLiteMap.put(DATABASE_NAME + "3", conn3);
        sqLiteOpenHelper.onCreate(db);
        verify(testOpenCallbacks).onCreate(conn3);
        sqLiteOpenHelper.onUpgrade(db, 1, 2);
        verify(testOpenCallbacks).onUpgrade(conn3, 1, 2);
    }
 */   
    @Test
    public void test_close() throws SQLException
    {
        OpenHelperConnectionSource conn1 = mock(OpenHelperConnectionSource.class);
        OpenHelperConnectionSource conn2 = mock(OpenHelperConnectionSource.class);
        sqLiteDatabaseSupport.androidSQLiteMap.put(DATABASE_NAME + "1", conn1);
        sqLiteDatabaseSupport.androidSQLiteMap.put(DATABASE_NAME + "2", conn2);
        sqLiteDatabaseSupport.close();
        verify(conn2).close();
        verify(conn1).close();
        assertThat(sqLiteDatabaseSupport.androidSQLiteMap.size()).isEqualTo(0);
    }
    @Test
    public void test_getResultList() throws SQLException
    {
        Integer RESULT1 = Integer.valueOf(97);
        Integer RESULT2 = Integer.valueOf(4320);
        QueryInfo queryInfo = getTestQueryInfo();
        QueryParams queryParams = ((TestAndroidDatabaseSupport)sqLiteDatabaseSupport).queryParams;
        when(queryParams.cursor.getCount()).thenReturn(2);
        when(queryParams.cursor.moveToFirst()).thenReturn(true);
        when(queryParams.cursor.moveToNext()).thenReturn(true, false);
        when(queryParams.cursor.getPosition()).thenReturn(0, 1);
        ArgumentCaptor<ResultRow> resultRowArg = ArgumentCaptor.forClass(ResultRow.class);
        when(queryInfo.getRowMapper().mapRow(resultRowArg.capture())).thenReturn(RESULT1, RESULT2);
        List<Object> resultList = sqLiteDatabaseSupport.getResultList(connectionSource, queryInfo, 0, 0);
        assertThat(resultList.size()).isEqualTo(2);
        assertThat(resultList.get(0)).isEqualTo(RESULT1);
        assertThat(resultList.get(1)).isEqualTo(RESULT2);
        assertThat(resultRowArg.getAllValues().get(0).getPosition()).isEqualTo(0);
        assertThat(resultRowArg.getAllValues().get(1).getPosition()).isEqualTo(1);
        assertThat(queryParams.columns).isEqualTo(SQL_COLUMNS);
        assertThat(queryParams.groupBy).isEqualTo(SQL_GROUP_BY);
        assertThat(queryParams.having).isEqualTo(SQL_HAVING);
        assertThat(queryParams.limit).isEqualTo(SQL_LIMIT);
        assertThat(queryParams.orderBy).isEqualTo(SQL_ORDER_BY);
        assertThat(queryParams.selection).isEqualTo(SQL_SELECTION);
        assertThat(queryParams.selectionArgs).isEqualTo(new String[]{ "Brown", "Smith" });
        assertThat(queryParams.table).isEqualTo(SQL_TABLES);
    }

    @Test
    public void test_SQLiteDatabaseSupport_getResultList_empty() throws SQLException
    {
        QueryInfo queryInfo = getTestQueryInfo();
        QueryParams queryParams = ((TestAndroidDatabaseSupport)sqLiteDatabaseSupport).queryParams;
        when(queryParams.cursor.getCount()).thenReturn(2);
        when(queryParams.cursor.moveToFirst()).thenReturn(false);
        List<Object> resultList = sqLiteDatabaseSupport.getResultList(connectionSource, queryInfo, 0, 0);
        assertThat(resultList.size()).isEqualTo(0);
    }
 
    @Test
    public void test_SQLiteDatabaseSupport_getSingleResult() throws SQLException
    {
        Integer RESULT1 = Integer.valueOf(809584);
        QueryInfo queryInfo = getTestQueryInfo();
        queryInfo.setLimit(null);
        QueryParams queryParams = ((TestAndroidDatabaseSupport)sqLiteDatabaseSupport).queryParams;
        when(queryParams.cursor.getCount()).thenReturn(1);
        when(queryParams.cursor.moveToFirst()).thenReturn(true);
        when(queryParams.cursor.moveToNext()).thenReturn(false);
        when(queryParams.cursor.getPosition()).thenReturn(0);

        ArgumentCaptor<ResultRow> resultRowArg = ArgumentCaptor.forClass(ResultRow.class);
        when(queryInfo.getRowMapper().mapRow(resultRowArg.capture())).thenReturn(RESULT1);
        Object resultObject = sqLiteDatabaseSupport.getSingleResult(connectionSource, queryInfo);
        assertThat(resultObject).isEqualTo(RESULT1);
        assertThat(resultRowArg.getValue().getPosition()).isEqualTo(0);
        assertThat(queryParams.columns).isEqualTo(SQL_COLUMNS);
        assertThat(queryParams.groupBy).isEqualTo(SQL_GROUP_BY);
        assertThat(queryParams.having).isEqualTo(SQL_HAVING);
        assertThat(queryParams.limit).isEqualTo("1");
        assertThat(queryParams.orderBy).isEqualTo(SQL_ORDER_BY);
        assertThat(queryParams.selection).isEqualTo(SQL_SELECTION);
        assertThat(queryParams.selectionArgs).isEqualTo(new String[]{ "Brown", "Smith" });
        assertThat(queryParams.table).isEqualTo(SQL_TABLES);
    }
    
    @Test
    public void test_SQLiteDatabaseSupport_getSingleResult_empty() throws SQLException
    {
        QueryInfo queryInfo = getTestQueryInfo();
        queryInfo.setLimit(null);
        QueryParams queryParams = ((TestAndroidDatabaseSupport)sqLiteDatabaseSupport).queryParams;
        when(queryParams.cursor.getCount()).thenReturn(0);
        when(queryParams.cursor.moveToFirst()).thenReturn(false);
        Object resultObject = sqLiteDatabaseSupport.getSingleResult(connectionSource, queryInfo);
        assertThat(resultObject).isNull();
    }
    
    @Test
    public void test_wrapDatabaseResults() throws SQLException
    {
        Cursor cursor = new TestCursor();
        AndroidResultRow resultRow = new AndroidResultRow(cursor);
        assertThat(resultRow.getPosition()).isEqualTo(5);
        assertThat(resultRow.getColumnIndex(COLUMN_NAME)).isEqualTo(1);
        assertThat(resultRow.getColumnNames()).isEqualTo(COLUMN_NAMES);
        assertThat(resultRow.getColumnCount()).isEqualTo(2);
        assertThat(resultRow.getBlob(3)).isEqualTo(TEST_BLOB);
        assertThat(resultRow.getString(7)).isEqualTo(DB_TEXT);
        assertThat(resultRow.getShort(3)).isEqualTo((short)1234);
        assertThat(resultRow.getInt(3)).isEqualTo(Integer.MAX_VALUE);
        assertThat(resultRow.getLong(3)).isEqualTo(Long.MAX_VALUE);
        assertThat(resultRow.getFloat(3)).isEqualTo(Float.MAX_VALUE);
        assertThat(resultRow.getDouble(3)).isEqualTo(Double.MAX_VALUE);
        assertThat(resultRow.isNull(3)).isEqualTo(true);
        StringBuffer buffer = new StringBuffer();
        resultRow.copyStringToBuffer(9, buffer);
        assertThat(buffer.toString()).isEqualTo(DB_TEXT);
    }
    
    @Test 
    public void test_adjustLimit()
    {
        QueryInfo queryInfo = getTestQueryInfo();
        assertThat(sqLiteDatabaseSupport.adjustLimit(queryInfo, 0, 0)).isEqualTo(SQL_LIMIT);
        assertThat(sqLiteDatabaseSupport.adjustLimit(queryInfo, 21, 100)).isEqualTo("21,100");
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
