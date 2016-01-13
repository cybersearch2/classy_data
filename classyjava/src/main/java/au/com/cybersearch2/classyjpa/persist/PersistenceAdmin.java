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

import java.util.List;
import java.util.Properties;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;

/**
 * PersistenceAdmin
 * Interface for JPA Support
 * @author Andrew Bowley
 * 05/07/2014
 */
public interface PersistenceAdmin extends ConnectionSourceFactory
{
    /**
     * Add named query to persistence unit context
     * @param clazz Entity class
     * @param name Query name
     * @param daoQueryFactory Query generator
     */
    void addNamedQuery(Class<?> clazz, String name, DaoQueryFactory daoQueryFactory);
    
    /**
     * Add native named query to persistence unit context
     * @param name Query name
     * @param queryInfo Native query information
     * @param queryGenerator Native query generator
     */
    void addNamedQuery(String name, QueryInfo queryInfo, SqlQueryFactory queryGenerator);
    
    /**
     * Returns EntityManager Factory for this perisistence unit
     * @return EntityManagerLiteFactory
     */
    EntityManagerLiteFactory getEntityManagerFactory();
    
    /**
     * Returns list of objects from executing a native query
     * @param queryInfo Native query details
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return List&lt;Object&gt;
     */
    List<Object> getResultList(QueryInfo queryInfo, int startPosition, int maxResults);
    
    /**
     * Returns object from executing a native query
     * @param queryInfo Native query details
     * @return Object or null if nothing returned by query
     */
    Object getSingleResult(QueryInfo queryInfo);
    
    /**
     * Returns database name
     * @return String
     */
    String getDatabaseName();
    
    /**
     * Close all database connections
     */
    void close();
    
    /**
     * Returns database type
     * @return com.j256.ormlite.db.DatabaseType
     */
    DatabaseType getDatabaseType();

    int getDatabaseVersion();
    
    /**
     * Returns PU properties
     * @return java.util.Properties
     */
    Properties getProperties();
    
    void registerClasses(List<String> managedClassNames);

    /**
     * Create a EntityManager bound to an existing connectionSource. Use only for special case of database creation or update.
     * @param connectionSource The existing ConnectionSource object 
     * @return Eentity manager instance
     */
    EntityManagerLite createEntityManager(ConnectionSource connectionSource);
    
    /** Flag set true if connection source is for a single connection */
    boolean isSingleConnection();

 }
