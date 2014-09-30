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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.persistence.spi.PersistenceUnitInfo;

import org.xmlpull.v1.XmlPullParserException;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyinject.DI;

/**
 * PersistenceFactory
 * Creates Persistence Unit implementations based on persistence.xml configuration
 * @author Andrew Bowley
 * 05/07/2014
 */
public class PersistenceFactory
{
    /** Native support. */
    protected DatabaseSupport databaseSupport;
    /** Maps PersistenceAdmin implementation to persistence unit name */
    protected Map<String, PersistenceAdminImpl> persistenceImplMap;
    /** Maps DatabaseAdmin implementation to persistence unit name */
    protected Map<String, DatabaseAdminImpl> databaseAdminImplMap;
    /** Interface for access to persistence.xml */
    @Inject ResourceEnvironment resourceEnvironment;
   
    /**
     * Create PersistenceFactory object
     * @param databaseSupport Native support
     * @throws PersistenceException for error opening or parsing persistence.xml
     */
    public PersistenceFactory(DatabaseSupport databaseSupport)
    {
        this.databaseSupport = databaseSupport;
        DI.inject(this);
        persistenceImplMap = new HashMap<String, PersistenceAdminImpl>();
        databaseAdminImplMap = new HashMap<String, DatabaseAdminImpl>();
        initializePersistenceContext(databaseSupport);
    }

    /**
     * Returns native support
     * @return DatabaseSupport
     */
    public DatabaseSupport getDatabaseSupport()
    {
        return databaseSupport;
    }
    
    /**
     * Returns persistence unit implementation, specified by name
     * @param puName Persistence unit name
     * @return Persistence
     */
    public Persistence getPersistenceUnit(final String puName)
    {
        return new Persistence(){

            /**
             * Returns Database-specific admin object
             * @return DatabaseAdmin
             */
            @Override
            public DatabaseAdmin getDatabaseAdmin() 
            {
                return databaseAdminImplMap.get(puName);
            }

            /**
             * Returns JPA admin object
             * @return PersistenceAdmin
             */
            @Override
            public PersistenceAdmin getPersistenceAdmin() 
            {
                return persistenceImplMap.get(puName);
            }

            /**
             * Returns persistence unit name
             * @return String
             */
            @Override
            public String getPersistenceUnitName() 
            {
                return puName;
            }
        };
    }

    /**
     * Initialize persistence unit implementations based on persistence.xml configuration
     * @param databaseSupport Native support used to obtain database type
     * @throws PersistenceException for error opening or parsing persistence.xml
     */
    protected synchronized void initializePersistenceContext(DatabaseSupport databaseSupport)
    {
        // Input persistence.xml
        Map<String, PersistenceUnitInfo> puMap = null;
        try
        {
            puMap  = getPersistenceUnitInfo();
        }
        catch (IOException e)
        {
            throw new PersistenceException("Error opening persistence configuration file", e);
        }
        catch (XmlPullParserException e)
        {
            throw new PersistenceException("Error parsing persistence configuration file", e);
        }
        // Set up PU implementations
        for (String name: puMap.keySet())
        {
            // Create configuration object and initialize it according to PU info read from persistence.xml
            // This includes setting up DAOs for all entity classes
            PersistenceConfig persistenceConfig = new PersistenceConfig(databaseSupport.getDatabaseType());
            persistenceConfig.setPuInfo(puMap.get(name));
            // Create objects for JPA and native support which are accessed using PersistenceFactory
            PersistenceAdminImpl persistenceAdmin = new PersistenceAdminImpl(name, databaseSupport, persistenceConfig);
            persistenceImplMap.put(name, persistenceAdmin);
            databaseAdminImplMap.put(name, new DatabaseAdminImpl(name, persistenceAdmin));
        }
        databaseSupport.initialize();
    }

    /**
     * Returns object to which persistence.xml is unmarshalled
     * @return Map&lt;String, PersistenceUnitInfo&gt; - maps each peristence unit data to it's name
     * @throws IOException for error reading persistence.xml
     * @throws XmlPullParserException for error parsing persistence.xml
     */
    protected Map<String, PersistenceUnitInfo> getPersistenceUnitInfo() throws IOException, XmlPullParserException
    {
        InputStream inputStream = null;
        PersistenceXmlParser parser = null;
        Map<String, PersistenceUnitInfo> persistenceUnitInfoMap = null;
        try
        {
            inputStream = resourceEnvironment.openResource(PersistenceUnitInfoImpl.PERSISTENCE_CONFIG_FILENAME);
            parser = new PersistenceXmlParser();
            persistenceUnitInfoMap = parser.parsePersistenceXml(inputStream);
        }
        finally
        {
            if (inputStream != null)
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                }
        }
        return persistenceUnitInfoMap;
    }

}
