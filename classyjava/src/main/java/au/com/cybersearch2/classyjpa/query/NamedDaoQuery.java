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
package au.com.cybersearch2.classyjpa.query;

import java.sql.SQLException;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

/**
 * NamedDaoQuery
 * Named OrmLite query generator
 * @author Andrew Bowley
 * 13/05/2014
 */
public class NamedDaoQuery implements Comparable<NamedDaoQuery>
{
    /** Entity class */
    protected Class<?> clazz;
    /** Name of query */
    protected String name;
    /** Query generator which incorporates selection arguments */
    protected DaoQueryFactory daoQueryFactory;
    
    /**
     * Create NamedDaoQuery object
     * @param clazz Entity class
     * @param name Name of query
     * @param daoQueryFactory Query generator which incorporates selection arguments
     */
    public NamedDaoQuery(Class<?> clazz, String name, DaoQueryFactory daoQueryFactory)
    {
        this.clazz = clazz;
        this.name = name;
        this.daoQueryFactory = daoQueryFactory;
    }

    /**
     * Returns OrmLite query
     * @param dao Entity DAO containing open ConnectionSource
     * @return Query
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Query createQuery(PersistenceDao<?, ?> dao)
    {
        try
        {
            DaoQuery<?> daoQuery = daoQueryFactory.generateQuery(dao);
            return new EntityQuery(daoQuery);
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Named query \"" + name + "\" failed on start", e);
        }
    }

    /**
     * Returns Entity class
     * @return Class
     */
    public Class<?> getEntityClass()
    {
        return clazz;
    }
    
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @param   another The object to be compared.
     * @return  A negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(NamedDaoQuery another) 
    {
        return name.compareTo(another.name);
    }
  
    /**
     * Returns a hash code value for the object.
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other)
    {
        if ((other != null) && ! (other instanceof NamedDaoQuery))
            return name.equals(((NamedDaoQuery)other).name);
        return false;
    }
    
}
