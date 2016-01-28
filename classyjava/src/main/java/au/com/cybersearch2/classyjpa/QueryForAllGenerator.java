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
package au.com.cybersearch2.classyjpa;

import java.sql.SQLException;

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.query.DaoQuery;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;

import com.j256.ormlite.stmt.QueryBuilder;

/**
 * QueryForAllGenerator
 * Query factory to find all objects belonging to a particular Entity class.
 * @author Andrew Bowley
 * 01/06/2014
 */
public class QueryForAllGenerator implements DaoQueryFactory
{
    /** Interface for JPA Support */
    PersistenceAdmin persistenceAdmin;

    /**
     * ForAllQuery
     * The query object produced each time generateQuery() is called on containing class
     * @author Andrew Bowley
     * 23 Sep 2014
     */
    class ForAllQuery<T> extends DaoQuery<T>
    {
        /**
         * Create ForAllQuery object
         * @param dao OrmLite data access object of generic type matching Entity class to be retrieved
         * @throws SQLException
         */
        public ForAllQuery(PersistenceDao<T, ?> dao) throws SQLException
        {
            // The super class executes the prepared statement
            super(dao);
        }

        /**
         * Construct a query using supplied QueryBuilder.
         * @see au.com.cybersearch2.classyjpa.query.DaoQuery#buildQuery(com.j256.ormlite.stmt.QueryBuilder)
         */
        @Override
        protected QueryBuilder<T, ?> buildQuery(
                QueryBuilder<T, ?> statementBuilder) throws SQLException 
        {
            // Query for all objects in database by leaving out where clause
            return statementBuilder;
        }

    }

    /**
     * Create QueryForAllGenerator object
     * @param persistenceAdmin Interface for JPA Support
     */
    public QueryForAllGenerator(PersistenceAdmin persistenceAdmin)
    {
        this.persistenceAdmin = persistenceAdmin;
    }
    
    /**
     * Returns query object which will execute a prepared statement with a primary key selection argument
     * @see au.com.cybersearch2.classyjpa.query.DaoQueryFactory#generateQuery(au.com.cybersearch2.classyjpa.entity.PersistenceDao)
     */
    @Override
    public <T> DaoQuery<T> generateQuery(PersistenceDao<T, ?> dao)
            throws SQLException 
    {
        return new ForAllQuery<T>(dao);
    }

}
