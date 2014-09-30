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

import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * TestPersistenceEnvironment
 * @author Andrew Bowley
 * 15/06/2014
 */
public class TestPersistenceEnvironment 
{
    private static final String TAG = "TestPersistenceEnvironment";
    protected static ConnectionType connectionType = ConnectionType.memory;
    
    static Log log = JavaLogger.getLogger(TAG);

    protected DatabaseSupport testDatabaseSupport;
    protected PersistenceFactory persistenceFactory;
     
    public TestPersistenceEnvironment()
    {
        testDatabaseSupport = new SQLiteDatabaseSupport(connectionType);
        persistenceFactory = new PersistenceFactory(testDatabaseSupport);
    }

    public void close()
    {
        testDatabaseSupport.close();
    }
    
    public static void setConnectionType(ConnectionType value)
    {
        connectionType = value;
    }

    public PersistenceFactory getPersistenceFactory() 
    {
        return persistenceFactory;
    }

    public DatabaseSupport getDatabaseSupport() 
    {
        return testDatabaseSupport;
    }
    
}
