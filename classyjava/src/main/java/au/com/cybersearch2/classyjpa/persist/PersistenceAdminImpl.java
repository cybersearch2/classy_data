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

import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupportBase;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImpl;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

/**
 * PersistenceAdminImpl
 * JPA Support implementation
 * @author Andrew Bowley
 * 29/07/2014
 */
public class PersistenceAdminImpl implements PersistenceAdmin
{
    private static final String TAG = "PersistenceAdminImpl";
    private static Log log = JavaLogger.getLogger(TAG);

    protected String puName;
    protected PersistenceConfig config;
    protected PersistenceUnitInfo puInfo;
    protected PersistenceProviderImpl provider;
    protected DatabaseSupport databaseSupport;
    protected String databaseName;
    protected Boolean singleConnection;
    protected ConnectionSource connectionSource;
    
    /**
     * Create PersistenceAdminImpl object
     * @param puName PersistenceUnitAdmin Unit (PU) name
     * @param databaseSupport Native support
     * @param config PersistenceUnitAdmin Unit configuration
     */
    public PersistenceAdminImpl(String puName, DatabaseSupport databaseSupport, PersistenceConfig config)
    {
        this.puName = puName;
        this.config = config;
        this.puInfo = config.getPuInfo();
        this.databaseSupport = databaseSupport;
        databaseName = getDatabaseName(puInfo);
        provider = new PersistenceProviderImpl(puName, config, this);
    }

    /**
     * Returns connection source
     * @return com.j256.ormlite.support.ConnectionSource
     */
    @Override
    public ConnectionSource getConnectionSource() 
    {
        return connectionSource;
    }

    protected void setConnectionSource(ConnectionSource connectionSource)
    {
        this.connectionSource = connectionSource;
    }
    
    /**
     * Add named query to persistence unit context
     * @param clazz Entity class
     * @param name Query name
     * @param daoQueryFactory Query generator
     */
    @Override
    public void addNamedQuery(Class<?> clazz, String name,
            DaoQueryFactory daoQueryFactory) 
    {
        config.addNamedQuery(clazz, name, daoQueryFactory);
    }

    /**
     * Add native named query to persistence unit context
     * @param name Query name
     * @param queryInfo Native query information
     * @param queryGenerator Native query generator
     */
    @Override
    public void addNamedQuery(String name, QueryInfo queryInfo, SqlQueryFactory queryGenerator) 
    {
        config.addNamedQuery(name, queryInfo, queryGenerator);
    }

    /**
     * Returns EntityManager Factory for this perisistence unit
     * @return EntityManagerLiteFactory
     */
    @Override
    public EntityManagerLiteFactory getEntityManagerFactory() 
    {
        return provider.createContainerEntityManagerFactory(puInfo, null);
    }

    /**
     * Create a EntityManager bound to an existing connectionSource. Use only for special case of database creation or update.
     * @param connectionSource The existing ConnectionSource object 
     * @return Eentity manager instance
     */
    @Override
    public EntityManagerLite createEntityManager(ConnectionSource connectionSource) 
    {
         return new EntityManagerImpl(
                connectionSource, 
                config);
    }

    /**
     * Returns list of objects from executing a native query
     * @param queryInfo Native query details
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return List&lt;Object&gt;
     */
    @Override
    public List<Object> getResultList(QueryInfo queryInfo, int startPosition,
            int maxResults) 
    {
        return databaseSupport.getResultList(connectionSource, queryInfo, startPosition, maxResults);
    }

    /**
     * Returns object from executing a native query
     * @param queryInfo Native query details
     * @return Object or null if nothing returned by query
     */
    @Override
    public Object getSingleResult(QueryInfo queryInfo) 
    {
        return databaseSupport.getSingleResult(connectionSource, queryInfo);
    }

    /**
     * Returns database name, which is defined as PU property "database-name"
     * @return String
     */
    @Override
    public String getDatabaseName() 
    {
        return databaseName;
    }

    @Override
    public int getDatabaseVersion()
    {
        return getDatabaseVersion(puInfo.getProperties());
    }
    
    /**
     * Returns database version, which is defined as PU property "database-version". Defaults to 1 if not defined
     * @return String
     */
    public static int getDatabaseVersion(Properties properties)
    {
        // Database version defaults to 1
        int databaseVersion = 1;
        if (properties != null)
        {
        	String textVersion = properties.getProperty(DatabaseAdmin.DATABASE_VERSION);
	        try
	        {
	        	if (textVersion != null)
	        	    databaseVersion = Integer.parseInt(textVersion);
	        }
	        catch (NumberFormatException e)
	        {
	        	log.error(TAG, "Invalid " + DatabaseAdmin.DATABASE_VERSION + " value: \"" + textVersion);
	        }
        }
    	return databaseVersion;
    }
    
    /**
     * Close all database connections
     */
    @Override
    public void close() 
    {
        databaseSupport.close();
    }

    /**
     * Returns database type
     * @return com.j256.ormlite.db.DatabaseType
     */
    @Override
    public DatabaseType getDatabaseType() 
    {
        return databaseSupport.getDatabaseType();
    }

    /**
     * Returns PU properties
     * @return java.util.Properties
     */
    @Override
    public Properties getProperties() 
    {
        return puInfo.getProperties();
    }

    protected PersistenceConfig getConfig()
    {
    	return config;
    }
    
    protected DatabaseSupport getDatabaseSupport()
    {
    	return databaseSupport;
    }

	@Override
	public boolean isSingleConnection() 
	{
		// Default to true until setSingleConnection() is called
		return singleConnection == null ? true : singleConnection;
	}
	
	public void setSingleConnection()
	{
		ConnectionSource connectionSource = getConnectionSource();
		singleConnection = Boolean.valueOf(connectionSource.isSingleConnection(DatabaseSupportBase.DATABASE_INFO_NAME));
	}

	@Override
	public void registerClasses(List<String> managedClassNames) 
	{
		config.registerClasses(managedClassNames);
	}
	
	public static String getDatabaseName(PersistenceUnitInfo puInfo)
	{
        String databaseName = puInfo.getProperties().getProperty(DatabaseAdmin.DATABASE_NAME);
        if ((databaseName == null) || (databaseName.length() == 0))
            throw new PersistenceException("\"" + puInfo.getPersistenceUnitName() + "\" does not have property \"" + DatabaseAdmin.DATABASE_NAME + "\"");
        return databaseName;
	}
}
