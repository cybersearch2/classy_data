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

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;

/**
 * TestDatabaseAdminImpl
 * @author Andrew Bowley
 * 01/08/2014
 */
public class TestDatabaseAdminImpl extends DatabaseAdminImpl
{
	TransactionCallable processFilesCallable;
    
    /**
     * @param puName
     * @param persistenceAdmin
     */
    public TestDatabaseAdminImpl(String puName,
            PersistenceAdmin persistenceAdmin,
            ResourceEnvironment resourceEnvironment)
    {
        super(puName, persistenceAdmin, resourceEnvironment, null);

    }

    @Override
    protected void executeTask(ConnectionSource connectionSource, TransactionCallable processFilesCallable)
    {
        this.processFilesCallable = processFilesCallable;
    }
}
