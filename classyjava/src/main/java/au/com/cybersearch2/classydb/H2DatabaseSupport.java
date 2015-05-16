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
import java.util.Properties;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import org.h2.jdbcx.JdbcDataSource;

import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
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

    /**
     * Construct an H2DatabaseSupport object
     * @param connectionType ConnectionType - memory, file or pooled
     */
	public H2DatabaseSupport(ConnectionType connectionType) 
	{
        super(new H2DatabaseType(), connectionType, log, TAG);
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
		JdbcDataSource jdbcDataSource = new JdbcDataSource();
		String url = null;
        switch(connectionType)
	    {
	        case file:
	    		url = "jdbc:h2:" + FILE_LOCATION  + "/" + databaseName;
	    		jdbcDataSource.setURL(url);
	            return new DataSourceConnectionSource(jdbcDataSource, url);
	        case pooled: // TODO - Add H2 Connection Pool
	            return new JdbcPooledConnectionSource("jdbc:h2:" + FILE_LOCATION  + "/" + databaseName); 
	        case memory: 
	        default:
	    		url = IN_MEMORY_PATH + databaseName;
	    		jdbcDataSource.setURL(url);
	            return new DataSourceConnectionSource(jdbcDataSource, url);
	    }
    }
    

}
