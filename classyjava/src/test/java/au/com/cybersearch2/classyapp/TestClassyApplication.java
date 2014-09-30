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
package au.com.cybersearch2.classyapp;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupport;
import au.com.cybersearch2.classynode.EntityByNodeIdGenerator;
import au.com.cybersearch2.classyfy.data.Model;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyfy.data.alfresco.RecordFolder;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.JpaIntegrationTest;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classynode.Node;

/**
 * TestClassyApplication
 * @author Andrew Bowley
 * 13/06/2014
 */
public class TestClassyApplication
{
    @Module(injects = { 
            TestClassyApplication.class, 
            JpaIntegrationTest.class,
            PersistenceContainer.class})
    static class PersistenceModule
    {
        @Provides @Singleton PersistenceFactory providePersistenceModule()
        {
            return new PersistenceFactory(new SQLiteDatabaseSupport(ConnectionType.memory));
        }
    }
    
    public static final String PU_NAME = "classyfy";
    public static final String DATABASE_NAME = "classyfy.db";
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + Model.recordCategory.ordinal();
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + Model.recordFolder.ordinal();
    
    protected TestClassyApplicationModule applicationModule;
    protected DI dependencyInjection;
    @Inject PersistenceFactory persistenceFactory;
    
    public TestClassyApplication()
    {
        applicationModule = new TestClassyApplicationModule();
    }

    public void onCreate()
    {
        dependencyInjection = new DI(applicationModule);
        dependencyInjection.validate();
        DI.add(new PersistenceModule());
        DI.inject(this);
        Persistence persistence = persistenceFactory.getPersistenceUnit(PU_NAME);
        EntityByNodeIdGenerator entityByNodeIdGenerator = new EntityByNodeIdGenerator();
        PersistenceAdmin persistenceAdmin = persistence.getPersistenceAdmin();
        persistenceAdmin.addNamedQuery(RecordCategory.class, CATEGORY_BY_NODE_ID, entityByNodeIdGenerator);
        persistenceAdmin.addNamedQuery(RecordFolder.class, FOLDER_BY_NODE_ID, entityByNodeIdGenerator);
    }
}
