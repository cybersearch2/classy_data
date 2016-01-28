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

import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import au.com.cybersearch2.classyjpa.EntityManagerLiteFactory;
import au.com.cybersearch2.classyjpa.PersistenceProvider;

/**
 * PersistenceProviderImpl
 * Implementation of PersistenceProvider interface
 * @author Andrew Bowley
 * 28/05/2014
 */
public class PersistenceProviderImpl implements PersistenceProvider
{
    /** PersistenceUnitAdmin Unit configuration information */
    protected PersistenceConfig persistenceConfig;
    /** Connection source factory */
    protected ConnectionSourceFactory connectionSourceFactory;
    /** PersistenceUnitAdmin Unit name */
    public final String puName;

    /**
     * Create PersistenceProviderImpl object
     * @param puName PersistenceUnitAdmin unit name
     * @param persistenceConfig PersistenceUnitAdmin configuration
     * @param connectionSourceFactory Database connection provider object
     */
    public PersistenceProviderImpl(
            String puName, 
            PersistenceConfig persistenceConfig, 
            ConnectionSourceFactory connectionSourceFactory)
    {
        this.puName = puName;
        this.persistenceConfig = persistenceConfig;
        this.connectionSourceFactory = connectionSourceFactory;
    }

    /**
     * Called by PersistenceUnitAdmin class when an EntityManagerFactory is to be created.
     * 
     * @param emName
     *            The name of the persistence unit
     * @param map
     *            Not used - set to null
     * @return EntityManagerFactory for the persistence unit, or null if the provider is not the right provider
     */
    @Override
    public EntityManagerLiteFactory createEntityManagerFactory(String emName,
            @SuppressWarnings("rawtypes") Map map) 
    {
        if (puName.equals(emName))
            return new EntityManagerFactoryImpl(connectionSourceFactory.getConnectionSource(), persistenceConfig);
        return null;
    }

    /**
     * Called by the container when an EntityManagerFactory is to be created.
     * 
     * @param info
     *            Metadata for use by the persistence provider
     * @param map
     *            Not used - set to null
     * @return EntityManagerFactory for the persistence unit specified by the metadata, or null if the provider is not the right provider
     */
    @Override
    public EntityManagerLiteFactory createContainerEntityManagerFactory(
            PersistenceUnitInfo info, @SuppressWarnings("rawtypes") Map map) {
        if ((puName).equals(info.getPersistenceUnitName()))
            return new EntityManagerFactoryImpl(connectionSourceFactory.getConnectionSource(), persistenceConfig);
        return null;
    }

}
