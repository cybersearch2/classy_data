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

import java.sql.SQLException;

import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyfy.data.alfresco.RecordFolder;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import com.j256.ormlite.support.ConnectionSource;

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
        PersistenceAdmin persistenceAdmin = persistence.getPersistenceAdmin();
        return persistenceAdmin.getConnectionSource();
    }

    public void onShutdown()
    {
        persistence.getPersistenceAdmin().close();
    }
    
    public Persistence getPersistenceEnvironment()
    {
        return persistence;
    }
}
