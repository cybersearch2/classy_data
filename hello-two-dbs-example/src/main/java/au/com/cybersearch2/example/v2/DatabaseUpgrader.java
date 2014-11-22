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
package au.com.cybersearch2.example.v2;

import java.sql.SQLException;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.Executable;

/**
 * DatabaseUpgrader
 * @author Andrew Bowley
 * 21 Nov 2014
 */
public class DatabaseUpgrader 
{
    /** Factory object to create "simple" and "complex" Persistence Unit implementations */
    @Inject PersistenceFactory persistenceFactory;

	public DatabaseUpgrader() 
	{
		DI.inject(this);
	}

	public String doUpgrade(int oldVersion, int newVersion, String... puNames) throws InterruptedException
	{
		if (!(oldVersion == 1) && (newVersion == 2))
			throw new PersistenceException("Upgrade from v" + oldVersion + " to v" + newVersion + " not supported");
		DatabaseType databaseType = persistenceFactory.getDatabaseSupport().getDatabaseType();
		if (!isSupportedDatabaseType(databaseType))
			throw new IllegalArgumentException("doUpgrade called for unsupported databaseType " + databaseType);
		if (puNames == null)
			puNames = new String[] { HelloTwoDbsMain.PU_NAME1, HelloTwoDbsMain.PU_NAME2 };
		else
		{
			for (String name: puNames)
				if (!(HelloTwoDbsMain.PU_NAME1.equals(name)) || (HelloTwoDbsMain.PU_NAME2.equals(name)))
					throw new IllegalArgumentException("doUpgrade invalid PU name: " + name);
		}
		// Add quote column to both entity tables
		for (String name: puNames)
			alterTable(name);
		// Our string builder for building the content-view
		StringBuilder sb = new StringBuilder();
		// Insert a quote in every row
		for (String name: puNames)
		{
			PersistenceContainer container = new PersistenceContainer(name);
			if (name.equals(HelloTwoDbsMain.PU_NAME1))
			{
				final Simple_v1Task simpleTask = new Simple_v1Task();
				waitForTask(container.executeTask(simpleTask));
				sb.append(HelloTwoDbsMain.SEPARATOR_LINE)
				   .append(simpleTask.getMessage());
			}
			else
            {
                final Complex_v1Task complexTask = new Complex_v1Task();
                waitForTask(container.executeTask(complexTask));
                sb.append(HelloTwoDbsMain.SEPARATOR_LINE)
				  .append(complexTask.getMessage());
            }
		}
        return sb.toString();
	}

    /**
     * Wait sychronously for task completion
     * @param exe Executable object returned upon starting task
     * @throws InterruptedException Should not happen
     */
    protected void waitForTask(Executable exe) throws InterruptedException
    {
        synchronized (exe)
        {
            exe.wait();
        }
    }

	/**
	 * Add quote column to entity table
	 * @param puName Persistence Unit name
	 */
	protected void alterTable(String puName)
	{
		// Get Simple table name based on puName
		String tableName = null;
		//if (puName.equals(HelloTwoDbsMain.PU_NAME1_v1))
		if (puName.equals(HelloTwoDbsMain.PU_NAME1))
			tableName = HelloTwoDbsMain.SIMPLE_DATA_TABLENAME;
		else
			tableName = HelloTwoDbsMain.COMPLEX_DATA_TABLENAME;
		PersistenceAdmin connectionSourceFactory = persistenceFactory.getPersistenceUnit(puName).getPersistenceAdmin();
		ConnectionSource connectionSource = connectionSourceFactory.getConnectionSource();
		DatabaseConnection connection = null;
		try 
		{
			connection = connectionSource.getReadOnlyConnection();
	        String alterTableStatement = "ALTER TABLE `" + tableName + "` ADD COLUMN `quote` VARCHAR";
			connection.executeStatement(alterTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
		} 
		catch (SQLException e) 
		{
			throw new PersistenceException(e);
		} 
		finally 
		{
			if (connection != null)
				try 
				{
					connectionSource.releaseConnection(connection);
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
		}
	}
	
    protected boolean isSupportedDatabaseType(DatabaseType databaseType)
    {
    	return databaseType instanceof SqliteDatabaseType;
    }
}
