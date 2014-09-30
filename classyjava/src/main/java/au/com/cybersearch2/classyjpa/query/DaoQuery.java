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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

/**
 * DaoQuery
 * OrmLite query for generic entity class. This is an abstract class as it contains abstract method buildQuery() to  
 * construct a query using an object of class com.j256.ormlite.stmt.QueryBuilder. 
 * @author Andrew Bowley
 * 01/06/2014
 */
public abstract class DaoQuery<T>
{
    /**
     * SimpleSelectArg
     * Makes value accessible without potential SQLException
     * @see com.j256.ormlite.stmt.SelectArg#getValue()
     * @author Andrew Bowley
     * 26/08/2014
     */
    public static class SimpleSelectArg extends SelectArg
    {
        /**
         * Returns value 
         * @return Object
         * @see com.j256.ormlite.stmt.SelectArg#getValue()
         */
        @Override
        public Object getValue() {
            return super.getValue();
        }
    }
    
    /** Entity DAO, which has open connection source */
    protected PersistenceDao<T, ?> dao;
    /** Maps selection argument to name ie. columnName attribute */
    protected Map<String, SelectArg> argumentMap;
    /** Selection arguments which are used to construct the WHERE clause */
    protected SelectArg[] argumentArray;
 
    /**
     * Create new DaoQuery object
     * @param dao Entity DAO, which has open connection sourcev
     * @param selectionArguments Selection arguments which are used to construct the WHERE clause
     */
    public DaoQuery(PersistenceDao<T, ?> dao, SimpleSelectArg... selectionArguments)
    {
        this.dao = dao;
        argumentMap = new HashMap<String, SelectArg>();
        if ((selectionArguments != null) && (selectionArguments.length > 0))
        {
            // Copy arguments to local array
            argumentArray = new SelectArg[selectionArguments.length];
            System.arraycopy(selectionArguments, 0, argumentArray, 0, selectionArguments.length);
            // Set up map to access arguments by name
            for (SelectArg selectArg: selectionArguments)
            {
                selectArg.setValue(null); // Reset to avoid using stale data
                argumentMap.put(selectArg.getColumnName(), selectArg);
            }
        }
        else
            argumentArray = new SelectArg[]{};
    }

    /**
     * Construct a query using supplied QueryBuilder
     * @param statementBuilder QueryBuilder of Entity generic type
     * @return QueryBuilder - updated with query to be performed
     * @throws SQLException
     */
    abstract protected QueryBuilder<T, ?> buildQuery(QueryBuilder<T, ?> statementBuilder) throws SQLException;

    /**
     * Returns list of objects from executing prepared query
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return List of Entity objects
     */
    protected List<T> getResultList(int startPosition, int maxResults) 
    {
        return dao.query(prepare(startPosition, maxResults));
    }

    /**
     * Returns object from executing prepared query
     * @return Entity object or null if nothing returned by query
     */
    protected T getSingleResult() 
    {
        return dao.queryForFirst(prepare(0, 1));
    }

    /**
     * Returns prepared query
     * @param startPosition The start position of the first result, numbered from 0
     * @param maxResults Maximum number of results to retrieve, or 0 for no limit
     * @return PreparedQuery
     */
    protected PreparedQuery<T> prepare(int startPosition, int maxResults)
    {
        PreparedQuery<T> prepared = null;
        try
        {
            QueryBuilder<T, ?> statementBuilder = dao.queryBuilder();
            if (startPosition > 0)
                statementBuilder.offset(Long.valueOf(startPosition));
            if (maxResults > 0)
                statementBuilder.limit(Long.valueOf(maxResults));
            prepared = buildQuery(statementBuilder).prepare();
        }
        catch (SQLException e)
        {
            throw new PersistenceException("Error preparing query", e);
        }
        return prepared;
    }

    /**
     * Returns true if position value is in range of 1 to number of arguments
     * @param position
     * @return boolean
     */
    public boolean isValidPosition(int position)
    {
        return (position <= argumentArray.length) && (position > 0);
    }
 
    /**
     * Returns selection argument at specified position
     * @param position int
     * @return SelectArg
     */
    public SelectArg get(int position)
    {
        return argumentArray[position - 1];
    }

    /**
     * Returns selection argument for specified name
     * @param param
     * @return SelectArg
     */
    public SelectArg get(String param)
    {
        return argumentMap.get(param);
    }
}
