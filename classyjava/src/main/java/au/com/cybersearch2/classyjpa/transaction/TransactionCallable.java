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
package au.com.cybersearch2.classyjpa.transaction;

import com.j256.ormlite.support.DatabaseConnection;

/**
 * TransactionCallable
 * Executes Callable object passing open database connection on which transaction is active
 * @author Andrew Bowley
 * 01/08/2014
 */
public interface TransactionCallable
{
    /**
     * Computes a Boolean result to indicate success or failure (rollback required), or throws an exception if unexpected error happens.
     * databaseConnection Open database connection on which transaction is active
     * @return Boolean - Boolean.TRUE indicates success
     * @throws Exception if unable to compute a result
     */
    Boolean call(DatabaseConnection databaseConnection) throws Exception;

}
