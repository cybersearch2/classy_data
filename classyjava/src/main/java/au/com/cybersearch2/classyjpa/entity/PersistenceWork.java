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
package au.com.cybersearch2.classyjpa.entity;

import au.com.cybersearch2.classyjpa.EntityManagerLite;

/**
 * PersistenceWork
 * Interface for execution of work in a PersistenceUnitAdmin context. 
 * @see PersistenceContainer
 * @author Andrew Bowley
 * 28/06/2014
 */
public interface PersistenceWork extends PersistenceTask
{
    /**
     * Runs on separate thread after successful completion of {@link PersistenceTask#doTask(EntityManagerLite entityManager)}.
     * @param success True if PersistenceWork completed successfully, otherwise false
     */
    void onPostExecute(boolean success); 

    /**
     * Handle rollback caused by exception while executing {@link PersistenceTask#doTask(EntityManagerLite entityManager)}
     * @param rollbackException Throwable exception which caused rollback
     */
    void onRollback(Throwable rollbackException);
}
