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

/**
 * TestOpenHelperCallbacks
 * @author Andrew Bowley
 * 10/07/2014
 */
public class TestOpenHelperCallbacks implements OpenHelperCallbacks
{
    static ConnectionSource connectionSource; 
    static int oldVersion;
    static int newVersion;
    
    public TestOpenHelperCallbacks()
    {
    }

    /**
     * @see au.com.cybersearch2.classydb.OpenHelperCallbacks#onCreate(android.database.sqlite.SQLiteDatabase, com.j256.ormlite.support.ConnectionSource)
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        TestOpenHelperCallbacks.connectionSource = connectionSource;
    }

    /**
     * @see au.com.cybersearch2.classydb.OpenHelperCallbacks#onUpgrade(android.database.sqlite.SQLiteDatabase, com.j256.ormlite.support.ConnectionSource, int, int)
     */
    @Override
    public void onUpgrade(
            ConnectionSource connectionSource, int oldVersion, int newVersion) 
    {
        TestOpenHelperCallbacks.connectionSource = connectionSource;
        TestOpenHelperCallbacks.oldVersion = oldVersion;
        TestOpenHelperCallbacks.newVersion = newVersion;
    }

}
