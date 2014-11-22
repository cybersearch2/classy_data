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
// Contains extracts from android.database.sqlite.SQLiteQueryBuilder, which has following copyright notice:
/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.com.cybersearch2.classydb;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Properties;

import javax.persistence.PersistenceException;

import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.ResultRow;
import au.com.cybersearch2.classylog.*;

/**
 * SQLiteDatabaseSupport
 * SQLite implementation for direct database access to allow native operations to be performed
 * @author Andrew Bowley
 * 16/06/2014
 */
public class SQLiteDatabaseSupport implements DatabaseSupport
{
	/** Log name */
    private static final String TAG = "DatabaseSupport";
    static Log log = JavaLogger.getLogger(TAG);
    /** Table to hold database version */
    public static final String DATABASE_INFO_NAME = "User_Info";
    /** SQLite memory path */
    private static final String IN_MEMORY_PATH = "jdbc:sqlite::memory:";
    /** SQLite location for file database */
    private static final String FILE_LOCATION = "resources/db";
    /** Limit clause validation */
    private static final Pattern LIMIT_PATTERN =
            Pattern.compile("\\s*\\d+\\s*(,\\s*\\d+\\s*)?");
    /** Map connectionSource to database name */
    protected Map<String, ConnectionSource> connectionSourceMap;
    /** Connection type: memory, file or pooled */
    protected ConnectionType connectionType;
    /** ORMLite databaseType */
    protected final DatabaseType databaseType;
 
    /**
     * Construct a SQLiteDatabaseSupport object
     * @param connectionType ConnectionType - memory, file or pooled
     */
    public SQLiteDatabaseSupport(ConnectionType connectionType)
    {
        this.connectionType = connectionType;
        databaseType = new SqliteDatabaseType();
        connectionSourceMap = new HashMap<String, ConnectionSource>();
    }

    /**
     * Perform any inititialization required prior to creating first database connection
     */
    public void initialize()
    {
        if (connectionType != ConnectionType.memory)
        {   // Create database directory
            File dbDir = new File(FILE_LOCATION);
            if (!dbDir.exists() && !dbDir.mkdirs())
                throw new PersistenceException("Failed to create database location: " + FILE_LOCATION);
        }
    }
    
    /**
     * Gets the database version.
     * @param  connectionSource Open ConnectionSource object of database. 
     * @return the database version
     */
	@Override
	public int getVersion(ConnectionSource connectionSource) 
	{
		int databaseVersion = 0;
		boolean tableExists = false;
		DatabaseConnection connection = null;
		try 
		{
			connection = connectionSource.getReadOnlyConnection();
			tableExists = connection.isTableExists(DATABASE_INFO_NAME);
			if (tableExists)
				databaseVersion = ((Long)connection.queryForLong("select version from " + DATABASE_INFO_NAME)).intValue();
		} 
		catch (SQLException e) 
		{
			throw new PersistenceException(e);
		} 
		finally 
		{
			try 
			{
				connectionSource.releaseConnection(connection);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return databaseVersion;
	}

    /**
     * Sets the database version.
     * @param  connectionSource Open ConnectionSource object of database. Can be null for Android SQLite.
     * @param version the new database version
     */
	@Override
	public void setVersion(int version, ConnectionSource connectionSource) 
	{
		boolean tableExists = false;
		DatabaseConnection connection = null;
		try 
		{
			connection = connectionSource.getReadOnlyConnection();
			tableExists = connection.isTableExists(DATABASE_INFO_NAME);
			if (tableExists)
			{
				String initTableStatement = "UPDATE `" + DATABASE_INFO_NAME + "` set `version` = " + version ;
				connection.executeStatement(initTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
			}
			else
			{
				String createTableStatement = "CREATE TABLE `" + DATABASE_INFO_NAME + "` (`version` INTEGER )";
				connection.executeStatement(createTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
				String initTableStatement = "INSERT INTO `" + DATABASE_INFO_NAME + "` (`version`) values (" + version  + ")";
				connection.executeStatement(initTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
			}
		} 
		catch (SQLException e) 
		{
			throw new PersistenceException(e);
		} 
		finally 
		{
			try 
			{
				connectionSource.releaseConnection(connection);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
	}
    
    /** 
     * Returns ConnectionSource object
     * @param databaseName
     * @param properties Properties defined in persistence.xml
     * @return ConnectionSource
     */
    @Override
    public synchronized ConnectionSource getConnectionSource(String databaseName, Properties properties)
    {
        ConnectionSource connectionSource = connectionSourceMap.get(databaseName);
        if (connectionSource == null)
        {
            try
            {
                switch(connectionType)
                {
                case file:
                    connectionSource = new JdbcConnectionSource("jdbc:sqlite:resources/db/" + databaseName);
                    break;
                case pooled:
                    connectionSource = new JdbcPooledConnectionSource("jdbc:sqlite:resources/db/" + databaseName); 
                    break;
                case memory: 
                default:
                    connectionSource = new JdbcConnectionSource(IN_MEMORY_PATH /*+ databaseName*/);
                    break;
                }
                connectionSourceMap.put(databaseName, connectionSource);
                // TODO - Consider connection management
                // Keep a reference to the single database connection to prevent it being closed 
                // until close() is called.
                connectionSource.getReadWriteConnection();
            }
            catch (SQLException e)
            {
                throw new PersistenceException("Cannot create connectionSource for database " + databaseName, e);
            }
        }
        return connectionSource;
    }
    
    /**
     * Perform any clean up required on database shutdown
     */
    @Override
    public synchronized void close()
    {   // Close all ConnectionSource objects and clear ConnectionSource map
        for (Entry<String, ConnectionSource> entry: connectionSourceMap.entrySet())
        {
            ConnectionSource connectionSource = entry.getValue();
            try
            {
                connectionSource.close();
            }
            catch (IOException e)
            {
                log.warn(TAG, "Error closing connection for database " + entry.getKey(), e);
            }
        }
        connectionSourceMap.clear();
    }

    /**
     * Returns database type
     * @return DatabaseType
     */
    @Override
    public DatabaseType getDatabaseType() 
    {
        return databaseType;
    }

    /**
     * Returns list result of native query in Android SQLite API format
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo
     * @param startPosition int
     * @param maxResults int
     * @return List of Objects
     */
    @Override
    public List<Object> getResultList(ConnectionSource connectionSource, QueryInfo queryInfo, int startPosition, int maxResults) 
    {
        List<Object> resultList = new ArrayList<Object>();
        DatabaseConnection connection = null;
        String databaseName = databaseType.getDatabaseName();
        try
        {
            connection = connectionSource.getReadWriteConnection();
            DatabaseResults results = getDatabaseResults(connection, queryInfo, startPosition, maxResults);
            if (results.first())
            {
                int position = 0;
                do
                {
                    ResultRow resultRow = new SqliteResultRow(position, results);
                    resultList.add(queryInfo.getRowMapper().mapRow(resultRow));
                    ++position;
                } while (results.next());
            }
        }
        catch (SQLException e)
        {
             throw new PersistenceException("Error getting database connection for database \"" + databaseName + "\"", e);
        }
        return resultList;
    }

    /**
     * Returns single result of native query in Android SQLite API format
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo
     * @return Object
     */
    @Override
    public Object getSingleResult(ConnectionSource connectionSource,
            QueryInfo queryInfo) 
    {
        List<Object> resultList = getResultList(connectionSource, queryInfo, 0, 1);
        return resultList.size() > 0 ? resultList.get(0) : null;
    }

    /**
     * Build an SQL query string from the given clauses.
     *
     * @param tables The table names to compile the query against.
     * @param columns A list of which columns to return. Passing null will
     *            return all columns, which is discouraged to prevent reading
     *            data from storage that isn't going to be used.
     * @param where A filter declaring which rows to return, formatted as an SQL
     *            WHERE clause (excluding the WHERE itself). Passing null will
     *            return all rows for the given URL.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @param having A filter declare which row groups to include in the results,
     *            if row grouping is being used, formatted as an SQL HAVING
     *            clause (excluding the HAVING itself). Passing null will cause
     *            all row groups to be included, and is required when row
     *            grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @param limit Limits the number of rows returned by the query,
     *            formatted as LIMIT clause. Passing null denotes no LIMIT clause.
     * @return the SQL query string
     */
    public static String buildQueryString(
            String tables, String[] columns, String where,
            String groupBy, String having, String orderBy, String limit) 
    {
        if (isEmpty(groupBy) && !isEmpty(having))
        {
            throw new IllegalArgumentException(
                    "HAVING clauses are only permitted when using a groupBy clause");
        }
        if (!isEmpty(limit) && !LIMIT_PATTERN.matcher(limit).matches()) 
        {
            throw new IllegalArgumentException("Invalid LIMIT clauses:" + limit);
        }

        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");
        if (columns != null && columns.length != 0) 
        {
            appendColumns(query, columns);
        } 
        else 
        {
            query.append("* ");
        }
        query.append("FROM ");
        query.append(tables);
        appendClause(query, " WHERE ", where);
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);
        appendClause(query, " LIMIT ", limit);

        return query.toString();
    }

    /**
     * Builds a SQL query, compiles and runs it and finally returns result
     *@param connection DatabaseConnection object
     *@param queryInfo QueryInfo object containing query elements
     *@param startPosition int
     *@param maxResults int
     *@return DatabaseResults
     *@throws SQLException
     */
    protected DatabaseResults getDatabaseResults(
            DatabaseConnection connection, 
            QueryInfo queryInfo, 
            int startPosition, 
            int maxResults) throws SQLException
    {
        String limitValue = queryInfo.getLimit();
        if (maxResults > 0)
        {
            limitValue = Integer.toString(maxResults);
            if (startPosition > 0)
            {   // offset precedes limit
                StringBuilder builder = new StringBuilder(Integer.valueOf(startPosition));
                builder.append(',').append(limitValue);
                limitValue = builder.toString();
            }
        }
        String statement = buildQueryString(
                queryInfo.getTable(),
                queryInfo.getColumns(),
                queryInfo.getSelection(),
                queryInfo.getGroupBy(),
                queryInfo.getHaving(),
                queryInfo.getOrderBy(),
                limitValue);
        CompiledStatement compiledStatement = connection.compileStatement(
                statement, 
                StatementType.SELECT_RAW, 
                new FieldType[] {},
                DatabaseConnection.DEFAULT_RESULT_FLAGS);
        int parameterIndex = 0;
        for (String arg: queryInfo.getSelectionArgs())
        {
            compiledStatement.setObject(parameterIndex, arg, SqlType.STRING);
            if (++parameterIndex >= compiledStatement.getColumnCount())
                break;
        }
        return compiledStatement.runQuery(null /*objectCache*/);
    }

    protected static boolean isEmpty(String text) 
    {
        return (text == null) || (text.length() == 0);
    }

    protected static void appendClause(StringBuilder s, String name, String clause) 
    {
        if (!isEmpty(clause)) 
        {
            s.append(name);
            s.append(clause);
        }
    }

    /**
     * Add the names that are non-null in columns to s, separating
     * them with commas.
     */
    protected static void appendColumns(StringBuilder s, String[] columns) 
    {
        int n = columns.length;

        for (int i = 0; i < n; i++) 
        {
            String column = columns[i];

            if (column != null) 
            {
                if (i > 0) 
                {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }
 
    /**
     * Close database connection
     *@param connection DatabaseConnection object
     *@param databaseName
     */
    protected void close(DatabaseConnection connection, String databaseName) 
    {
        if (connection != null)
            try
            {
                 connection.close();
            }
            catch (IOException e)
            {
                log.error(TAG, "Error closing database connection for database \"" + databaseName + "\"", e);
            }
    }


}
