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

import java.io.File;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Properties;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;

import org.h2.jdbcx.JdbcDataSource;

import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * H2DatabaseSupport
 * @author Andrew Bowley
 * 16 May 2015
 */
public class H2DatabaseSupport extends DatabaseSupportBase 
{
	/** Log name */
    private static final String TAG = "H2DatabaseSupport";
    static Log log = JavaLogger.getLogger(TAG);
    /** SQLite memory path */
    private static final String IN_MEMORY_PATH = "jdbc:h2:mem:";
    /** SQLite location for file database */
    private static final String FILE_LOCATION = "resources/db";
    
    private static final String[] EXCLUDE_KEYS = { DatabaseAdmin.DATABASE_NAME, PersistenceUnitInfoImpl.PU_NAME_PROPERTY };
    
    private File databaseDirectory;

    /**
     * Construct an H2DatabaseSupport object
     * @param connectionType ConnectionType - memory, file or pooled
     */
	public H2DatabaseSupport(ConnectionType connectionType) 
	{
        super(new H2DatabaseType(), connectionType, log, TAG);
	}

    /**
     * Construct an H2DatabaseSupport object
     * @param databaseDirectory Database location. ConnectionType is automatically "file"
     */
	public H2DatabaseSupport(File databaseDirectory) 
	{
        super(new H2DatabaseType(), databaseDirectory != null ? ConnectionType.file : ConnectionType.memory, log, TAG);
    	this.databaseDirectory = databaseDirectory;
	}

	@Override
    protected File getDatabaseLocation()
    {
    	return new File(FILE_LOCATION);
    }
    
	@Override
	protected String getVersionUpdateStatement(int version) 
	{
		return "UPDATE `" + DATABASE_INFO_NAME + "` set `version` = " + version;
	}

	@Override
	protected String getVersionCreateStatement() 
	{
		return "CREATE TABLE `" + DATABASE_INFO_NAME + "` (`version` INTEGER )";
	}

	@Override
	protected String getVersionInsertStatement(int version) 
	{
		return "INSERT INTO `" + DATABASE_INFO_NAME + "` (`version`) values (" + version  + ")";
	}

	@Override
	protected ConnectionSource getConnectionSourceForType(String databaseName, Properties properties) throws SQLException
    {
		String fileLocation = databaseDirectory == null ? FILE_LOCATION : databaseDirectory.getAbsolutePath();
        switch(connectionType)
	    {
	        case file:
	        {
	        	String url = "jdbc:h2:" + fileLocation  + "/" + databaseName;
	            return getDataSourceConnectionSource(url, properties);
	        }
	        case pooled: // TODO - Add H2 Connection Pool
				return getPooledConnectionSource(databaseName, fileLocation, properties);
	        case memory: 
	        default:
	        {
	        	String url = IN_MEMORY_PATH + databaseName;
	            return getDataSourceConnectionSource(url, properties);
	        }
	    }
    }
 
	private DataSourceConnectionSource getDataSourceConnectionSource(String url, Properties properties) throws SQLException {
	    JdbcDataSource jdbcDataSource = new JdbcDataSource();
	    String finalUrl = appendProperties(url, properties, jdbcDataSource);
	    jdbcDataSource.setURL(finalUrl);
        return new DataSourceConnectionSource(jdbcDataSource, finalUrl);
	}

	private JdbcPooledConnectionSource getPooledConnectionSource(String databaseName, String fileLocation, Properties properties) throws SQLException {
		String url = appendProperties("jdbc:h2:" + fileLocation  + "/" + databaseName, properties);
        return new JdbcPooledConnectionSource(url); 
	}
	
	private String appendProperties(String url, Properties properties, JdbcDataSource jdbcDataSource) {
		String newUrl = url;
		Properties filtered = filterProperties(properties);
		if (!filtered.isEmpty()) {
			StringBuilder builder = new StringBuilder(url);
			for (Entry<Object, Object> entry: filtered.entrySet()) {
			    String key = entry.getKey().toString();
			    String value = entry.getValue().toString();
			    if ("USER".equalsIgnoreCase(key))
			        jdbcDataSource.setUser(value);
			    else if ("PASSWORD".equalsIgnoreCase(key))
			        jdbcDataSource.setPassword(value);
			    else
				    builder.append(';').append(key).append('=').append(value);
			}
			newUrl = builder.toString();
		}
		return newUrl;
	}
	private String appendProperties(String url, Properties properties) {
		String newUrl = url;
		Properties filtered = filterProperties(properties);
		if (!filtered.isEmpty()) {
			StringBuilder builder = new StringBuilder(url);
			for (Entry<Object, Object> entry: filtered.entrySet()) {
			    String key = entry.getKey().toString();
			    String value = entry.getValue().toString();
				builder.append(';').append(key).append('=').append(value);
			}
			newUrl = builder.toString();
		}
		return newUrl;
	}

    private Properties filterProperties(Properties properties) {
        Properties filtered = new Properties();
		if ((properties != null) && !properties.isEmpty()) {
 			for (Entry<Object, Object> entry: properties.entrySet()) {
			    String key = entry.getKey().toString();
			    for (String exclude:  EXCLUDE_KEYS) {
			        if (exclude.equalsIgnoreCase(key)) {
			           key = null;
			           break;
			        }
			    }
			    if (key != null)
			        filtered.put(key, entry.getValue().toString());
			}
		}
		return filtered;
    }
}
