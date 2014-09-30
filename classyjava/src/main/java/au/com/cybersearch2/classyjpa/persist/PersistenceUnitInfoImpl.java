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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

/**
 * PersistenceUnitInfoImpl
 * @author Andrew Bowley
 * 13/06/2014
 */
public class PersistenceUnitInfoImpl implements PersistenceUnitInfo
{
    public static final String PERSISTENCE_CONFIG_FILENAME = "persistence.xml";
    public static final String PU_NAME_PROPERTY = "persistence-unit-name";
    public static final String CUSTOM_OHC_PROPERTY = "open-helper-callbacks-classname";
    
    private String persistenceUnitName;
    String persistenceProviderClassName = "";
    List<String> managedClassNames;
    Properties properties;
    
    public PersistenceUnitInfoImpl(String persistenceUnitName)
    {
        this.persistenceUnitName = persistenceUnitName;
        managedClassNames = new ArrayList<String>();
        properties = new Properties();
        properties.setProperty(PU_NAME_PROPERTY, persistenceUnitName);
        
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) 
    {
    }

    @Override
    public boolean excludeUnlistedClasses() 
    {
        return false;
    }

    @Override
    public ClassLoader getClassLoader() 
    {
        return null;
    }

    @Override
    public List<URL> getJarFileUrls() 
    {
        return null;
    }

    @Override
    public DataSource getJtaDataSource() 
    {
        return null;
    }

    @Override
    public List<String> getManagedClassNames() 
    {
        return managedClassNames;
    }

    @Override
    public List<String> getMappingFileNames() 
    {
        return null;
    }

    @Override
    public ClassLoader getNewTempClassLoader() 
    {
        return null;
    }

    @Override
    public DataSource getNonJtaDataSource() 
    {
        return null;
    }

    @Override
    public String getPersistenceProviderClassName() 
    {
        return persistenceProviderClassName;
    }

    @Override
    public String getPersistenceUnitName() 
    {
        return persistenceUnitName;
    }

    @Override
    public URL getPersistenceUnitRootUrl() 
    {
        return null;
    }

    @Override
    public Properties getProperties() 
    {
        return properties;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() 
    {
        return null;
    }
    
    @Override
    public String getPersistenceXMLSchemaVersion() 
    {
        return "2.0";
    }

    @Override
    public SharedCacheMode getSharedCacheMode() 
    {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() 
    {
        return null;
    }
}
