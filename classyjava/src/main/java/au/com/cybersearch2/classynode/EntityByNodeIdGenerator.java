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
package au.com.cybersearch2.classynode;

import java.sql.SQLException;

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.query.DaoQuery;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.DaoQuery.SimpleSelectArg;

import com.j256.ormlite.stmt.QueryBuilder;

/**
 * EntityByNodeIdGenerator
 * Generate query to find Node by primary key
 * @author Andrew Bowley
 * 09/06/2014
 */
public class EntityByNodeIdGenerator implements DaoQueryFactory
{
    @Override
    /**
     * Generate query to find Node by primary key
     * @see au.com.cybersearch2.classyjpa.query.DaoQueryFactory#generateQuery(au.com.cybersearch2.classyjpa.entity.PersistenceDao)
     */
    public <T> DaoQuery<T> generateQuery(PersistenceDao<T, ?> dao)
            throws SQLException 
    {   // Only one select argument required for primary key 
        final SimpleSelectArg nodeIdArg = new SimpleSelectArg();
        // Set primary key column name
        nodeIdArg.setMetaInfo("node_id");
        return new DaoQuery<T>(dao, nodeIdArg){

            /**
             * Update supplied QueryBuilder object to add where clause
             * @see au.com.cybersearch2.classyjpa.query.DaoQuery#buildQuery(com.j256.ormlite.stmt.QueryBuilder)
             */
            @Override
            protected QueryBuilder<T, ?> buildQuery(QueryBuilder<T, ?> queryBuilder)
                    throws SQLException {
                // build a query with the WHERE clause set to 'node_id = ?'
                queryBuilder.where().eq("node_id", nodeIdArg);
                return queryBuilder;
            }};
    }
}
