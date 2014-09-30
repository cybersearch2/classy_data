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

import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

/**
 * QueryGenerator
 * Factory for DaoQuery objects.
 * DaoQuery is a OrmLite query for generic entity class.
 * @author Andrew Bowley
 * 13/05/2014
 */
public interface DaoQueryFactory
{
    /**
     * Returns query object which will execute a prepared statement when required selection arguments are provided
     * @param dao OrmLite data access object of generic type matching Entity class to be retrieved
     * @return DaoQuery
     * @throws SQLException
     */
    <T> DaoQuery<T> generateQuery(PersistenceDao<T, ?> dao) throws SQLException;
}
