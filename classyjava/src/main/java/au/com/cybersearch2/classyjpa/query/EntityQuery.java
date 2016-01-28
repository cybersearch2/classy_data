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

import com.j256.ormlite.stmt.SelectArg;

import au.com.cybersearch2.classylog.*;

/**
 * EntityQuery
 * Implements javax.persistence.Query using OrmLite query. Only a subset of PersistenceUnitAdmin API 1.0 methods supported.
 * @author Andrew Bowley
 * 13/05/2014
 */
public class EntityQuery<T> extends QueryBase
{
    private static final String TAG = "EntityQuery";
    private static Log log = JavaLogger.getLogger(TAG);
    /** OrmLite query for generic entity class */
    protected DaoQuery<T> daoQuery;

    public EntityQuery(DaoQuery<T> daoQuery)
    {
        this.daoQuery = daoQuery;
    }

    /**
     * Execute an update or delete statement. NOT implemented.
     * @return 0
     */
    @Override
    public int executeUpdate() 
    {
        release();
        return 0; // Updates and deletes are currently not supported.
    }

    /**
     * Execute a SELECT query and return the query results as a List.
     * @return a list of the results
     */   
    @Override
    public List<T> getResultList() 
    {
        if (isClosed) // Only perform query once
            return new ArrayList<T>();
        try
        {
            return daoQuery.getResultList(startPosition, maxResults);
        }
        finally
        {
            release();
        }
    }

    /**
     * Execute a SELECT query that returns a single result.
     * @return The result
     * @throws NoResultException if there is no result
     */
   @Override
    public Object getSingleResult() 
    {
        Object result = null;
        if (isClosed) // Only perform query once
            throw new NoResultException("getSingleResult() called when query already executed");
        try
        {
            result = daoQuery.getSingleResult();
        }
        catch (PersistenceException e)
        {
            String detail = e.getCause() == null ? e.toString() : e.getCause().toString();
            String message = "Named query error: " + detail;
            log.error(TAG, message, e);
            throw new NoResultException(message);
        }
        finally
        {
            release();
        }
        if (result == null)
            throw new NoResultException("getSingleResult() query returned null");
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
        SelectArg selectArg = validateParam(param);
        if (selectArg != null)
            selectArg.setValue(value);
        else
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
        if (daoQuery.isValidPosition(position))
        {
            SelectArg selectArg = daoQuery.get(position);
            selectArg.setValue(value);
        }
        else
            logInvalidPosition(position);
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
        SelectArg selectArg = validateParam(param);
        if (selectArg != null)
            selectArg.setValue(value);
        else
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
        SelectArg selectArg = validateParam(param);
        if (selectArg != null)
            selectArg.setValue(value.getTime());
        else
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
        if (daoQuery.isValidPosition(position))
        {
            SelectArg selectArg = daoQuery.get(position);
            selectArg.setValue(value);
        }
        else
            logInvalidPosition(position);
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
        if (daoQuery.isValidPosition(position))
        {
            SelectArg selectArg = daoQuery.get(position);
            selectArg.setValue(value.getTime());
        }
        else
            logInvalidPosition(position);
        return this;
    }

    /**
     * Log "position out of range" error and throw IllegalArgumentException
     * @param position Invalid value
     */
    protected void logInvalidPosition(int position)
    {
        log.error(TAG, "Query parameter " + position + " out of range for named query");
        throw new IllegalArgumentException("Parameter \"" + position + "\" is invalid");
    }

    /**
     * Returns selection argument for named parameter
     * @param param The parameter name
     * @return SelectArg
     */
    protected SelectArg validateParam(String param)
    {
        if (param == null)
        {
            log.error(TAG, "Null query parameter encountered for named query");
            return null;
        }
        SelectArg selectArg = daoQuery.get(param); 
        if (selectArg == null)
            log.error(TAG, "Query parameter '" + param + "' not found for named query");
        return selectArg;
    }
    
}
