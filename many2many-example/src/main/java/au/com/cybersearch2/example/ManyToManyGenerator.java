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
package au.com.cybersearch2.example;

import java.sql.SQLException;
import java.util.List;

import javax.persistence.PersistenceException;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.QueryInfo.RowMapper;
import au.com.cybersearch2.classyjpa.query.DaoQuery;
import au.com.cybersearch2.classyjpa.query.ResultRow;
import au.com.cybersearch2.classyjpa.query.SqlQuery;

/**
 * ManyToManyGenerator
 * Query factory to find objects through a many to many association - reusable.
 * Features inner query on join table using native query.
 * @author Andrew Bowley
 * 01/06/2014
 */
public class ManyToManyGenerator implements DaoQueryFactory
{
    /** The name of the join table which has 2 foreign key columns to associate 2 entity classes in a many to many relationship */ 
    String table;
    /** Column name in join table to match on */
    String joinColumn;
    /** Column name for foreign key to retrieve on */
    String foreignKeyColumn;
    /** Column name in foreign table which foreignKeyColumn points to */
    String primaryKeyColumn;
    /** Interface for JPA Support */
    PersistenceAdmin persistenceAdmin;

    /**
     * ManyToManyQuery
     * The query object produced each time generateQuery() is called on containing class
     * @author Andrew Bowley
     * 23 Sep 2014
     */
    class ManyToManyQuery<T> extends DaoQuery<T>
    {
        /** Prepared statement to perform query */
        protected PreparedQuery<T> preparedStatement;
        /** Single primary key argument required to perform query */
        protected SimpleSelectArg primaryKeyArg;
        
        /**
         * Create ManyToManyQuery object
         * @param dao OrmLite data access object of generic type matching Entity class to be retrieved
         * @param primaryKey The ID to match on the join table join column, placed in an array to support the DaoQuery interface
         * @throws SQLException
         */
        public ManyToManyQuery(PersistenceDao<T, ?> dao, SimpleSelectArg... primaryKey) throws SQLException
        {
            // The super class populates the primary key selection argument with a value and executes the prepared statement
            super(dao, primaryKey);
            // The primary key selection argument is required for inner query on join table
            primaryKeyArg = primaryKey[0];
        }

        /**
         * Construct a query using supplied QueryBuilder. Performs inner query on join table using native query.
         * @see au.com.cybersearch2.classyjpa.query.DaoQuery#buildQuery(com.j256.ormlite.stmt.QueryBuilder)
         */
        @Override
        protected QueryBuilder<T, ?> buildQuery(
                QueryBuilder<T, ?> statementBuilder) throws SQLException 
        {
            // Validate primary key selection argument
            Object value = primaryKeyArg.getValue();
            if ((value == null) || !(value instanceof Integer))
                throw new PersistenceException("Invalid value for param " + joinColumn + ": " + value);
            int id = ((Integer)value).intValue();
            // Build our outer query for Entity objects
            // Where the id matches in the foreign key from the inner query
            statementBuilder.where().in(primaryKeyColumn, getPrimaryKeyList(id));
            return statementBuilder;
        }

        /**
         * Returns list of join table foreign keys with matching join id
         * @param joinId Primary key to match on in join table join column
         * @return List&lt;Integer&gt;
         */
        @SuppressWarnings("unchecked")
        List<Integer> getPrimaryKeyList(int joinId)
        {
            // Create QueryInfo object passing mandatory parameters
            // The query results will be a list of primary keys, for which only a simple RowMapper is required
            QueryInfo queryInfo = new QueryInfo(
                    // Maps a Cursor position to an object to be returned by the query
                    new RowMapper(){

                        @Override
                        public Object mapRow(ResultRow resultRow) 
                        {
                            return Integer.valueOf(resultRow.getInt(0));
                        }},
                     // The table name to compile the query against
                     table, 
                     // A list of which columns to return
                     new String[] { foreignKeyColumn });
            // The parameter names mapped to selection arguments
            queryInfo.setParameterNames(new String[] { joinColumn });
            // A filter declaring which rows to return
            queryInfo.setSelection(joinColumn + " = ?");
            // Perform query
            SqlQuery sqlQuery = new SqlQuery(persistenceAdmin, queryInfo);
            sqlQuery.setParam(joinColumn, joinId);
            return (List<Integer>) sqlQuery.getResultObjectList();
        }
    }

    /**
     * Create ManyToManyQuery object
     * @param persistenceAdmin Interface for JPA Support
     * @param table The name of the join table
     * @param joinColumn Column name in join table to match on
     * @param foreignKeyColumn Column name for foreign key to retrieve on
     * @param primaryKeyColumn Column name in foreign table which foreignKeyColumn points to
     */
    public ManyToManyGenerator(PersistenceAdmin persistenceAdmin, String table, String joinColumn, String foreignKeyColumn, String primaryKeyColumn)
    {
        this.persistenceAdmin = persistenceAdmin;
        this.table = table;
        this.joinColumn = joinColumn;
        this.foreignKeyColumn = foreignKeyColumn;
        this.primaryKeyColumn = primaryKeyColumn;
    }
    
    /**
     * Returns query object which will execute a prepared statement with a primary key selection argument
     * @see au.com.cybersearch2.classyjpa.query.DaoQueryFactory#generateQuery(au.com.cybersearch2.classyjpa.entity.PersistenceDao)
     */
    @Override
    public <T> DaoQuery<T> generateQuery(PersistenceDao<T, ?> dao)
            throws SQLException 
    {
        // Create The selection argument to contain the ID to match on the join table join column
        DaoQuery.SimpleSelectArg primaryKeyArg = new DaoQuery.SimpleSelectArg();
        DaoQuery.SimpleSelectArg[] selectionArguments = new DaoQuery.SimpleSelectArg[1];
        selectionArguments[0] = primaryKeyArg;
        primaryKeyArg.setMetaInfo(joinColumn);
        return new ManyToManyQuery<T>(dao, selectionArguments);
    }

}
