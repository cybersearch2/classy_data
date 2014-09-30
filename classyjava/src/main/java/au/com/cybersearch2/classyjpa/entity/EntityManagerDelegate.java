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
package au.com.cybersearch2.classyjpa.entity;

import java.util.Map;

import javax.persistence.EntityTransaction;

import com.j256.ormlite.support.ConnectionSource;

/**
 * EntityManagerDelegate
 * Object returned by ClassyFyEntityManager getDelegate() method to give caller access to OrmLite DAOs
 * @author Andrew Bowley
 * 03/05/2014
 */
public class EntityManagerDelegate
{
    /** Connection Source to use for all database connections */
    protected final ConnectionSource connectionSource;
    /** Enclosing transaction object */ 
    protected final EntityTransaction entityTransaction;
    /** Maps entity class name to ORMLite DAO helper */
    protected final Map<String,OrmDaoHelperFactory<?,?>> helperFactoryMap;
    
    /**
     * Constructor.
     * @param connectionSource Connection Source to use for all database connection
     * @param entityTransaction Enclosing transaction object
     * @param helperFactoryMap Maps entity class name to ORMLite DAO helper
     */
    public EntityManagerDelegate(ConnectionSource connectionSource, EntityTransaction entityTransaction, Map<String,OrmDaoHelperFactory<?,?>> helperFactoryMap)
    {
        this.connectionSource = connectionSource;
        this.entityTransaction = entityTransaction;
        this.helperFactoryMap = helperFactoryMap;
    }

    /**
     * Returns ORMLite DAO for specified entity class
     * @param clazz  Entity class
     * @return PersistenceDao
     * @throws UnsupportedOperationException if class is unknown for this persistence unit
     */
    public PersistenceDao<?, ?> getDaoForClass(Class<?> clazz)
    {
        OrmDaoHelperFactory<?,?> ormDaoHelperFactory = (OrmDaoHelperFactory<?, ?>) helperFactoryMap.get(clazz.getName());
        if (ormDaoHelperFactory == null)
            throw new UnsupportedOperationException("DAO for entity class " + clazz.getName() + " not supported because ormDaoHelper is not set");
        return ormDaoHelperFactory.getDao(connectionSource);
    }

    /**
     * Returns enclosing transaction. If in user transaction mode, this will be the actual transaction object, 
     * otherwise, it will be a proxy which only supports setRollbackOnly()
     * @return EntityTransaction
     */
    public EntityTransaction getTransaction() 
    {
        return entityTransaction;
    }

}
