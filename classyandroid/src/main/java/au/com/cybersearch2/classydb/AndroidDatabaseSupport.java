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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;

/**
 * AndroidDatabaseSupport
 * Implements DatabaseSupport interface while also satisfying SQLiteOpenHelper requirements.
 * The onCreate() and onUpgrade() methods are provided by an OpenHelperCallbacks implementation.
 * A custom OpenHelperCallbacks implementation is specified by perisistence.xml property 
 * "open-helper-callbacks-classname". If not specified, the default ClassyOpenHelperCallbacks
 * class is used which calls DatabaseAdmin onCreate() and onUpgrade() methods. The latter methods
 * assume database initialisation is performed using file-based SQL statements. 
 * @author Andrew Bowley
 * 20/06/2014
 */
public class AndroidDatabaseSupport implements DatabaseSupport
{
    /** 
     * SQLiteQueryExecutor
     * Interface to allow testing of database behaviour
     * @author Andrew Bowley
     * 10/07/2014
     */
    public interface SQLiteQueryExecutor
    {
        Cursor query(String table, String[] columns, String selection,
                String[] selectionArgs, String groupBy, String having,
                String orderBy, String limit);
    }
    
    private static final String TAG = "DatabaseSupport";
    static Log log = JavaLogger.getLogger(TAG);

    protected DatabaseType databaseType;
    protected AndroidConnectionSourceFactory androidConnectionSourceFactory;

    /**
     * Construct an AndroidDatabaseSupport instance. 
     */
    public AndroidDatabaseSupport()
    {
        databaseType = new SqliteAndroidDatabaseType();
        // Connection management is delegated to AndroidConnectionSourceFactory
        // which also manages SQLiteOpenHelper objects
        androidConnectionSourceFactory = new AndroidConnectionSourceFactory();
    }

    /**
     * Called to initialize Database following Persistence initialization
     * @see au.com.cybersearch2.classydb.DatabaseSupport#initialize()
     */
    public void initialize()
    {
    }

    /**
     * Returns ConnectionSource
     * @param databaseName
     * @param properties Properties defined in persistence unit
     * @return com.j256.ormlite.support.ConnectionSource
     * @see au.com.cybersearch2.classydb.DatabaseSupport#getConnectionSource(java.lang.String, java.util.Properties)
     */
    @Override
    public synchronized ConnectionSource getConnectionSource(final String databaseName, Properties properties)
    {
        return androidConnectionSourceFactory.getAndroidSQLiteConnection(databaseName, properties);
    }
    
    /**
     * Close database. 
     * @see au.com.cybersearch2.classydb.DatabaseSupport#close()
     */
    @Override
    public synchronized void close()
    {
        androidConnectionSourceFactory.close();
    }

    /**
     * Return DatabaseType
     * @return com.j256.ormlite.db.DatabaseType object
     * @see au.com.cybersearch2.classydb.DatabaseSupport#getDatabaseType()
     */
    @Override
    public DatabaseType getDatabaseType() 
    {
        return databaseType;
    }
    
    /**
     * Returns SQLiteOpenHelper
     * @param connectionSource Sub class of AndroidConnectionSource which exposes the internal SOLiteOpenHelper object
     */
    public SQLiteOpenHelper getSQLiteOpenHelper(ConnectionSource connectionSource)
    {
        return ((OpenHelperConnectionSource)connectionSource).getSQLiteOpenHelper();
    }

    /**
     * Returns Object list from SQL query
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo object
     * @param startPosition offset
     * @param maxResults limit
     * @return Object list which will be empty if no result returned from the query
     * @see au.com.cybersearch2.classydb.DatabaseSupport#getResultList(com.j256.ormlite.support.ConnectionSource, au.com.cybersearch2.classyjpa.query.QueryInfo, int, int)
     */
    @Override
    public List<Object> getResultList(
            ConnectionSource connectionSource, 
            QueryInfo queryInfo, 
            int startPosition, 
            int maxResults) 
    {   
        String limitValue = adjustLimit(queryInfo, startPosition, maxResults);
        SQLiteQueryExecutor db = getSQLiteDatabase(connectionSource);
        Cursor cursor = db.query(queryInfo.getTable(), queryInfo.getColumns(), queryInfo.getSelection(),
                queryInfo.getSelectionArgs(), queryInfo.getGroupBy(), queryInfo.getHaving(),
                queryInfo.getOrderBy(), limitValue);
        List<Object> results = new ArrayList<Object>(cursor.getCount());
        if (cursor.moveToFirst())
        {
            do
            {
                results.add(queryInfo.getRowMapper().mapRow(new AndroidResultRow(cursor)));
            } while (cursor.moveToNext());
        }
        return results;
    }

    /**
     * Returns Object from SQL query
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo object
     * @return Object or null if no result returned from the query
     * @see au.com.cybersearch2.classydb.DatabaseSupport#getSingleResult(com.j256.ormlite.support.ConnectionSource, au.com.cybersearch2.classyjpa.query.QueryInfo)
     */
    @Override
    public Object getSingleResult(ConnectionSource connectionSource, QueryInfo queryInfo) 
    {
        String limitValue = adjustLimit(queryInfo, 0, 1);
        SQLiteQueryExecutor db = getSQLiteDatabase(connectionSource);
        Object result = null;
        Cursor cursor = db.query(queryInfo.getTable(), queryInfo.getColumns(), queryInfo.getSelection(),
                queryInfo.getSelectionArgs(), queryInfo.getGroupBy(), queryInfo.getHaving(),
                queryInfo.getOrderBy(), limitValue);
        if (cursor.moveToFirst())
             result = queryInfo.getRowMapper().mapRow(new AndroidResultRow(cursor));
        return result;
    }

	@Override
	public int getVersion(ConnectionSource connectionSource) 
	{
		return ((OpenHelperConnectionSource)connectionSource).getDatabase().getVersion();
	}

	@Override
	public void setVersion(int version, ConnectionSource connectionSource) 
	{
		((OpenHelperConnectionSource)connectionSource).getDatabase().setVersion(version);
	}
 

    /**
     * Returns Object implementing WritableDatabase interface for performing a database query
     * @param connectionSource
     * @return SQLiteQueryExecutor
     */
    protected SQLiteQueryExecutor getSQLiteDatabase(ConnectionSource connectionSource)
    {
        final SQLiteOpenHelper sqliteOpenHelper = getSQLiteOpenHelper(connectionSource);
        return  new SQLiteQueryExecutor(){

            @Override
            public Cursor query(String table, String[] columns,
                    String selection, String[] selectionArgs, String groupBy,
                    String having, String orderBy, String limit) {
                return sqliteOpenHelper.getWritableDatabase()
                        .query(false, table, columns, selection, selectionArgs, groupBy,
                        having, orderBy, limit);
            }};
    }

    /**
     * Set limit value to match specified offset and limit values
     * @param queryInfo QueryInfo object. The limit value is overriden if limit is non-zero. 
     * Note offset is ignored if limit is specified to be 0.
     * @param startPosition offset
     * @param maxResults limit
     * @return Limit value to use in query
     */
    protected String adjustLimit(
            QueryInfo queryInfo, 
            int startPosition, 
            int maxResults)
    {
        // Override queryInfo limit if maxResults non-zero
        String limitValue = queryInfo.getLimit();
        if (maxResults > 0)
        {
            limitValue = Integer.toString(maxResults);
            if (startPosition > 0)
            {   // offset precedes limit
                StringBuilder builder = new StringBuilder(Integer.toString(startPosition));
                builder.append(',').append(limitValue);
                limitValue = builder.toString();
            }
        }
        return limitValue;
    }

}
