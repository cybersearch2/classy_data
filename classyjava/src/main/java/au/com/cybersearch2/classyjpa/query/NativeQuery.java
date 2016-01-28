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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import au.com.cybersearch2.classylog.*;

/**
 * NativeQuery
 * Implements javax.persistence.Query using native query. Only a subset of PersistenceUnitAdmin API 1.0 methods supported.
 * @author Andrew Bowley
 * 30/05/2014
 */
public class NativeQuery extends QueryBase
{
    public static final String TAG = "NativeQuery";
    protected static Log log = JavaLogger.getLogger(TAG);
    /** Query invoked using Android SQLite interface */
    protected SqlQuery sqlQuery;
  
    /**
     * Create a NativeQuery object
     * @param sqlQuery Query invoked using Android SQLite interface
     */
    public NativeQuery(SqlQuery sqlQuery)
    {
        this.sqlQuery = sqlQuery;
    }

    /**
     * Execute an update or delete statement. NOT implemented.
     * @return 0
     */
    @Override
    public int executeUpdate() 
    {
        release();
        return 0; // Updates and deletes are not currently supported.
    }

    /**
     * Execute a SELECT query and return the query results as a List.
     * @return List of objects
     */   
    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getResultList() 
    {
        if (isClosed) // Only perform query once
            return new ArrayList<Object>();
        try
        {
            return (List<Object>) sqlQuery.getResultObjectList(startPosition, maxResults);
        }
        finally
        {
            release();
        }
    }

    /**
     * Execute a SELECT query that returns a single result.
     * @return Object
     * @throws NoResultException if there is no result
     */
    @Override
    public Object getSingleResult() 
    {
        Object result = null;
        if (isClosed) // Only perform query once
            throw new NoResultException("getSingleResult() called when query already executed");
        String message = sqlQuery.toString();
        try
        {
             result = sqlQuery.getResultObject();
        }
        catch (PersistenceException e)
        {
            message += ": " + ((e.getCause() != null) ? e.getCause().toString() : e.toString());
            log.error(TAG, message, e);
        }
        finally
        {
            release();
        }
        if (result == null)
        {
            throw new NoResultException(message);
        }
        return result;
    }

   
    /**
     * Bind an argument to a named parameter.
     * @param param The parameter name
     * @param value Object
     * @return The same query instance
     * @throws IllegalArgumentException if parameter name does not
     *    correspond to parameter in query string
     */
    @Override
    public Query setParameter(String param, Object value) 
    {
        if (!sqlQuery.setParam(param, value))
            throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
        return this;
    }

    /**
     * Bind an argument to a positional parameter.
     * @param position  Starts at 1
     * @param value Object
     * @return The same query instance
     * @throws IllegalArgumentException if position does not
     *    correspond to positional parameter of query
     */
    @Override
    public Query setParameter(int position, Object value) 
    {
        if (!sqlQuery.setParam(position, value))
            throw new IllegalArgumentException("Position \"" + position + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Date to a named parameter.
     * @param param The parameter name
     * @param value Date
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if parameter name does not
     *    correspond to parameter in query string
     */
    @Override
    public Query setParameter(String param, Date value, TemporalType type) 
    {
        if (!sqlQuery.setParam(param, value))
            throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Calendar to a named parameter.
     * @param param The parameter name
     * @param value Calendar
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if parameter name does not
     *    correspond to parameter in query string
     */
    @Override
    public Query setParameter(String param, Calendar value, TemporalType type) 
    {
        if (!sqlQuery.setParam(param, value.getTime()))
            throw new IllegalArgumentException("Parameter \"" + param + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Date to a positional parameter.
     * @param position  Starts at 1
     * @param value Date
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if position does not
     *    correspond to positional parameter of query
     */
    @Override
    public Query setParameter(int position, Date value, TemporalType type) 
    {
        if (!sqlQuery.setParam(position, value))
            throw new IllegalArgumentException("Position \"" + position + "\" is invalid");
        return this;
    }

    /**
     * Bind an instance of java.util.Calendar to a positional parameter.
     * @param position  Starts at 1
     * @param value Calendar
     * @param type Not used
     * @return The same query instance
     * @throws IllegalArgumentException if position does not
     *    correspond to positional parameter of query
     */
    @Override
    public Query setParameter(int position, Calendar value, TemporalType type) 
    {
        if (!sqlQuery.setParam(position, value.getTime()))
            throw new IllegalArgumentException("Position \"" + position + "\" is invalid");
        return this;
    }

}
