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

import java.sql.SQLException;

import javax.persistence.PersistenceException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * OrmDaoHelperFactory
 * @author Andrew Bowley
 * 18/08/2014
 */
public class OrmDaoHelperFactory<T,ID>
{
    private Class<T> entityClass;

    public OrmDaoHelperFactory(Class<T> entityClass)
    {
        this.entityClass = entityClass;
    }

    OrmDaoHelper<T,ID> getOrmDaoHelper(ConnectionSource connectionSource)
    {
        PersistenceDao<T, ID> entityDao = getDao(connectionSource);
        checkTableExists(connectionSource, entityDao);
        return new OrmDaoHelper<T,ID>(entityDao);
    }

    public PersistenceDao<T, ID> getDao(ConnectionSource connectionSource) 
    {
        try
        {
            PersistenceDao<T, ID> dao = createDao(connectionSource);
            dao.setObjectCache(true);
            return dao;
        }
        catch (SQLException e)
        {
            throw new IllegalArgumentException("Error creating DAO for class " + entityClass.getName(), e);
        }
    }

    public boolean checkTableExists(ConnectionSource connectionSource) 
    {
        PersistenceDao<T, ID> entityDao = getDao(connectionSource);
        return checkTableExists(connectionSource, entityDao);
    }
    
    protected boolean checkTableExists(ConnectionSource connectionSource, PersistenceDao<T, ID> entityDao) 
    {
        try
        {
            if (!entityDao.isTableExists())
            {
                createTable(connectionSource);
                return false;
            }
            return true;
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error creating table for class " + entityClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected PersistenceDao<T, ID> createDao(ConnectionSource connectionSource) throws SQLException
    {
        return new PersistenceDao<T, ID>((Dao<T, ID>) DaoManager.createDao(connectionSource, entityClass));
    }
    
    protected void createTable(ConnectionSource connectionSource) throws SQLException
    {
        TableUtils.createTable(connectionSource, entityClass);
    }
}
