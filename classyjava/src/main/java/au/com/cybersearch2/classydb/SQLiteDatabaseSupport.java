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
import java.sql.SQLException;
import java.util.Properties;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * SQLiteDatabaseSupport
 * SQLite implementation for direct database access to allow native operations to be performed
 * @author Andrew Bowley
 * 16/06/2014
 */
public class SQLiteDatabaseSupport extends DatabaseSupportBase
{
	/** Log name */
    private static final String TAG = "SQLiteDatabaseSupport";
    static Log log = JavaLogger.getLogger(TAG);
    /** SQLite memory path */
    private static final String IN_MEMORY_PATH = "jdbc:sqlite::memory:";
    /** SQLite location for file database */
    private static final String FILE_LOCATION = "resources/db";
    private File databaseDirectory;
 
    /**
     * Construct a SQLiteDatabaseSupport object
     * @param connectionType ConnectionType - memory, file or pooled
     */
    public SQLiteDatabaseSupport(ConnectionType connectionType)
    {
    	super(new SqliteDatabaseType(), connectionType, log, TAG);
    }

    public SQLiteDatabaseSupport(File databaseDirectory) {
    	super(new SqliteDatabaseType(), ConnectionType.file, log, TAG);
    	this.databaseDirectory = databaseDirectory;
    }

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
	            return new JdbcConnectionSource("jdbc:sqlite:" + fileLocation  + "/" + databaseName);
	        case pooled:
	            return new JdbcPooledConnectionSource("jdbc:sqlite:" + fileLocation  + "/" + databaseName); 
	        case memory: 
	        default:
	            return new JdbcConnectionSource(IN_MEMORY_PATH /*+ databaseName*/);
	        }
    }
}
