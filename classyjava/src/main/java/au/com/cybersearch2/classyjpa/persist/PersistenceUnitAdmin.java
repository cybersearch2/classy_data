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
package au.com.cybersearch2.classyjpa.persist;

import au.com.cybersearch2.classydb.DatabaseAdmin;

/**
 * PersistenceUnitAdmin
 * PersistenceUnitAdmin Unit implementation
 * @author Andrew Bowley
 * 12/06/2014
 */
public interface PersistenceUnitAdmin
{
    /**
     * Returns persistence unit name
     * @return String
     */
    String getPersistenceUnitName();
    
    /**
     * Returns Database-specific admin object
     * @return DatabaseAdmin
     */
    DatabaseAdmin getDatabaseAdmin();
    
    /**
     * Returns JPA admin object
     * @return PersistenceAdmin
     */
    PersistenceAdmin getPersistenceAdmin();
}
