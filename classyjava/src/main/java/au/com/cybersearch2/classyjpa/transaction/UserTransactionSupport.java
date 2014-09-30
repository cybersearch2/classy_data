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

/**
 * UserTransactionSupport
 * Interface to extend javax.persistence.EntityManager to support user transactions
 * @author Andrew Bowley
 * 10/06/2014
 */
public interface UserTransactionSupport
{
    /**
     * Set user transaction flag
     * @param value boolean
     */
    void setUserTransaction(boolean value);
}
