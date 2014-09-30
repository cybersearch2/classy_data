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

import javax.persistence.Query;

/**
 * NamedSqlQuery
 * Named native query generator
 * @author Andrew Bowley
 * 30/05/2014
 */
public class NamedSqlQuery implements Comparable<NamedSqlQuery>
{
    /** Name of query */
    private String name;
    /** Native query information */
    private QueryInfo queryInfo;
    /** Native query generator */
    protected SqlQueryFactory sqlQueryFactory;
    
    /**
     * Create NamedSqlQuery object
     * @param name Name of query
     * @param queryInfo Native query information
     * @param sqlQueryFactory Native query generator
     */
    public NamedSqlQuery(String name, QueryInfo queryInfo, SqlQueryFactory sqlQueryFactory)
    {
        this.name = name;
        this.queryInfo = queryInfo;
        this.sqlQueryFactory = sqlQueryFactory;
    }

    /**
     * Returns native query
     * @return Query
     */
    public Query createQuery()
    {
        return sqlQueryFactory.createSqlQuery(queryInfo);
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
    public int compareTo(NamedSqlQuery another) 
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
        if ((other != null) && ! (other instanceof NamedSqlQuery))
            return name.equals(((NamedSqlQuery)other).name);
        return false;
    }
    
}
