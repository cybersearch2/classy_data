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
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.spi.PersistenceUnitInfo;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyinject.DI;

/**
 * PersistenceContext
 * Hides PersistenceFactory to simplify dependency configuration
 * @author Andrew Bowley
 * 05/07/2014
 */
public class PersistenceContext
{
    @Inject PersistenceFactory persistenceFactory;
   
    /**
     * Create PersistenceContext object
     */
    public PersistenceContext()
    {
        DI.inject(this);
    }

    /**
     * Returns native support
     * @return DatabaseSupport
     */
    public DatabaseSupport getDatabaseSupport()
    {
        return persistenceFactory. getDatabaseSupport();
    }
    
    /**
     * Returns persistence unit implementation, specified by name
     * @param puName Persistence unit name
     * @return Persistence
     */
    public Persistence getPersistenceUnit(String puName)
    {
        return persistenceFactory. getPersistenceUnit(puName);
    }

    public void initializeAllDatabases()
    {
        persistenceFactory.initializeAllDatabases();
    }
    
    /**
     * Returns Database-specific admin object for specified Persistence unit name
     * @param puName Persistence unit name
     * @return DatabaseAdmin
     */
    public DatabaseAdmin getDatabaseAdmin(String puName) 
    {
        return getPersistenceUnit(puName).getDatabaseAdmin();
    }

    /**
     * Returns JPA admin object for specified Persistence unit name
     * @param puName Persistence unit name
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
}
