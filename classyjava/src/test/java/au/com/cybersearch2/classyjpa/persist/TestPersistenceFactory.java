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

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.SQLException;

import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyfy.data.alfresco.RecordFolder;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classytask.WorkStatus;

import com.j256.ormlite.db.SqliteDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * AndroidEnv
 * @author Andrew Bowley
 * 20/06/2014
 */
public class TestPersistenceFactory
{
    ConnectionSource connectionSource;
    Persistence persistence;
    
    public TestPersistenceFactory(Persistence persistence)
    {
        this.persistence = persistence;
        PersistenceAdmin persistenceAdmin = persistence.getPersistenceAdmin();
        EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
        persistenceAdmin.addNamedQuery(RecordCategory.class, TestClassyApplication.CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
        persistenceAdmin.addNamedQuery(RecordFolder.class, TestClassyApplication.FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
    }
    
    public ConnectionSource setUpDatabase() throws SQLException 
    {
        DatabaseAdmin databaseAdmin = persistence.getDatabaseAdmin();
        PersistenceAdmin persistenceAdmin = persistence.getPersistenceAdmin();
        if (persistenceAdmin.getDatabaseType().getClass().equals(SqliteDatabaseType.class))
        {
            connectionSource = databaseAdmin.onCreate();
            assertThat(databaseAdmin.waitForTask(0)).isEqualTo(WorkStatus.FINISHED);
        }
        else
        {   // Android controls database creation, so just request a connection to trigger it.
            connectionSource = persistenceAdmin.getConnectionSource();
            DatabaseConnection dbConn = connectionSource.getReadWriteConnection();
            assertThat(dbConn.isTableExists("models")).isTrue();
        }
        return connectionSource;
    }

    public void onShutdown()
    {
        persistence.getPersistenceAdmin().close();
        assertThat(connectionSource.isOpen()).isFalse();
    }
    
    public Persistence getPersistenceEnvironment()
    {
        return persistence;
    }
}
