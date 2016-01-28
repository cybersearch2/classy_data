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

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.ConnectionSourceFactory;

/**
 * PersistenceContext
 * Application persistence interface
 * @author Andrew Bowley
 * 05/07/2014
 */
public class PersistenceContext
{
    protected PersistenceFactory persistenceFactory;
    protected ConnectionSourceFactory connectionSourceFactory;
    protected boolean isInitialized;
   
    /**
     * Create PersistenceContext object
     */
    public PersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory)
    {
        this(persistenceFactory, connectionSourceFactory, true);
    }

    /**
     * Create PersistenceContext object
     */
    public PersistenceContext(PersistenceFactory persistenceFactory, ConnectionSourceFactory connectionSourceFactory, boolean initialize)
    {
        this.persistenceFactory = persistenceFactory;
        this.connectionSourceFactory = connectionSourceFactory;
        persistenceFactory.initializeAllConnectionSources(connectionSourceFactory);
        if (initialize)
            initializeAllDatabases();
    }

    /**
     * Returns persistence unit implementation, specified by name
     * @param puName PersistenceUnitAdmin unit name
     * @return PersistenceUnitAdmin
     */
    public PersistenceUnitAdmin getPersistenceUnit(String puName)
    {
        return persistenceFactory.getPersistenceUnit(puName);
    }

    /**
     * Returns Database-specific admin object for specified PersistenceUnitAdmin unit name
     * @param puName PersistenceUnitAdmin unit name
     * @return DatabaseAdmin
     */
    public DatabaseAdmin getDatabaseAdmin(String puName) 
    {
        return getPersistenceUnit(puName).getDatabaseAdmin();
    }

    /**
     * Returns JPA admin object for specified PersistenceUnitAdmin unit name
     * @param puName PersistenceUnitAdmin unit name
     * @return PersistenceAdmin
     */
    public PersistenceAdmin getPersistenceAdmin(String puName) 
    {
        return getPersistenceUnit(puName).getPersistenceAdmin();
    }

    public void registerClasses(String puName, List<String> managedClassNames)
    {
    	getPersistenceAdmin(puName).registerClasses(managedClassNames);
    }
    
    public void putProperties(String puName, Properties properties)
    {
    	getPersistenceAdmin(puName).getProperties().putAll(properties);
    }
    
    public void close()
    {
        persistenceFactory.getDatabaseSupport().close();
    }

    public void initializeAllDatabases()
    {
        if (isInitialized)
            return;
        persistenceFactory.initializeAllDatabases(connectionSourceFactory);
        isInitialized = true;
    }

    public void upgradeAllDatabases()
    {
        if (!isInitialized)
            throw new PersistenceException("PersistenceContext upgrade request while uninitialized");
        persistenceFactory.initializeAllDatabases(connectionSourceFactory);
    }

    public DatabaseSupport getDatabaseSupport()
    {
        return persistenceFactory.getDatabaseSupport();
    }
}
