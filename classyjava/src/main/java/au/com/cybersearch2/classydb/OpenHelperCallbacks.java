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
package au.com.cybersearch2.classydb;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;

/**
 * OpenHelperCallbacks
 * Interface for onCreate() and onUpdate() SQLiteOpenHelper abstract methods
 * @author Andrew Bowley
 * 24/06/2014
 */
public interface OpenHelperCallbacks
{
    void setDatabaseAdmin(DatabaseAdmin databaseAdmin);
    void setPersistenceAdmin(PersistenceAdmin persistenceAdmin);
    
    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    void onCreate(ConnectionSource connectionSource);

    /**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    void onUpgrade(ConnectionSource connectionSource, int oldVersion,
            int newVersion);

}
