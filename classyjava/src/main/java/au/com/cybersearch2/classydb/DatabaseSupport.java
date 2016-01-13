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

import java.util.List;

import au.com.cybersearch2.classyjpa.query.QueryInfo;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

/**
 * DatabaseSupport
 * Interface for direct database access to allow native operations to be performed
 * @author Andrew Bowley
 * 16/06/2014
 */
public interface DatabaseSupport
{
    /** Connection type is implementation-specific */
    public enum ConnectionType
    {
        memory,
        file,
        pooled
    }

    /**
     * Perform any inititialization required prior to creating first database connection
     */
    void initialize();

    /**
     * Perform any clean up required on database shutdown
     */
    void close();

    /**
     * Returns database type
     * @return DatabaseType
     */
    DatabaseType getDatabaseType();
 
    /**
     * Returns list result of native query in Android SQLite API format
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo
     * @param startPosition int
     * @param maxResults int
     * @return List of Objects
     */
    List<Object> getResultList(ConnectionSource connectionSource, QueryInfo queryInfo, int startPosition, int maxResults);

    /**
     * Returns single result of native query in Android SQLite API format
     * @param connectionSource Open ConnectionSource object
     * @param queryInfo QueryInfo
     * @return Object
     */
    Object getSingleResult(ConnectionSource connectionSource, QueryInfo queryInfo);
    
    /**
     * Gets the database version.
     * @param  connectionSource Open ConnectionSource object of database. Can be null for Android SQLite. 
     * @return the database version
     */
    int getVersion(ConnectionSource connectionSource);

    /**
     * Sets the database version.
     * @param  connectionSource Open ConnectionSource object of database. Can be null for Android SQLite.
     * @param version the new database version
     */
    void setVersion(int version, ConnectionSource connectionSource);
    
    void registerOpenHelperCallbacks(OpenHelperCallbacks openHelperCallbacks);
    List<OpenHelperCallbacks> getOpenHelperCallbacksList();

}