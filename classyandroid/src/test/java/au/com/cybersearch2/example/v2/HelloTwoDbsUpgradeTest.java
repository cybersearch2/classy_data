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

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.example.HelloTwoDbsMain;

/**
 * HelloTwoDbsUpgradeTest
 * @author Andrew Bowley
 * 24 Apr 2015
 */
@RunWith(RobolectricTestRunner.class)
@Config(application = TestRoboApplication.class)
public class HelloTwoDbsUpgradeTest 
{

    private AndroidHelloTwoDbs helloTwoDbsMain;

    @Before
    public void setUp() throws Exception 
    {
    } 

    @After
    public void shutdown()
    {
    	helloTwoDbsMain.shutdown();
    }
 
    @Test 
    public void test_hello_two_dbs_upgrade() throws Exception
    {
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
        au.com.cybersearch2.example.HelloTwoDbsMain helloTwoDbsMain_v1 = 
                new au.com.cybersearch2.example.AndroidHelloTwoDbs();
        try
        {
            // Set up v1 from start which causes the database tables to be dropped and the version reset to 0
            helloTwoDbsMain_v1.setUp(true);
            au.com.cybersearch2.example.SimpleTask simpleTask = 
                    new au.com.cybersearch2.example.SimpleTask("main");
            helloTwoDbsMain_v1.performPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
            // Our string builder for building the content-view
            StringBuilder sb = new StringBuilder();
            au.com.cybersearch2.example.ComplexTask complexTask = 
                    new au.com.cybersearch2.example.ComplexTask("main");
            helloTwoDbsMain_v1.performPersistenceWork(HelloTwoDbsMain.PU_NAME2, complexTask);
            helloTwoDbsMain_v1.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully at " + System.currentTimeMillis());
            helloTwoDbsMain_v1.displayMessage(sb
                    .append(HelloTwoDbsMain.SEPARATOR_LINE)
                    .append(simpleTask.getMessage())
                    .append(HelloTwoDbsMain.SEPARATOR_LINE)
                    .append(complexTask.getMessage())
                    .toString());
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        // We cannot load a 2nd persistence.xml to get V2 configuration, so will 
        // update the V1 configuration instead.
        // We need to add the V2 entity classes and change the database version from 1 to 2.
        PersistenceContext persistenceContext = HelloTwoDbsMain.upgradePersistenceContext(helloTwoDbsMain_v1.getPersistenceContext());
        PersistenceAdmin simpleAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME1);
        PersistenceAdmin complexAdmin = persistenceContext.getPersistenceAdmin(HelloTwoDbsMain.PU_NAME2);
        DatabaseSupport databaseSupport = persistenceContext.getDatabaseSupport();
        ConnectionSource connectionSource1 = simpleAdmin.getConnectionSource();
        int simpleVersion = databaseSupport.getVersion(connectionSource1);
        assertThat(simpleVersion).isEqualTo(1);
        //System.out.println("Simple version = " + simpleVersion);
        ConnectionSource connectionSource2 = complexAdmin.getConnectionSource();
        int complexVersion = databaseSupport.getVersion(connectionSource2);
        assertThat(complexVersion).isEqualTo(1);
        //System.out.println("Complex version = " + complexVersion);
        // Reinitialize databases using PersistenceContext
        persistenceContext.upgradeAllDatabases();
        simpleVersion = databaseSupport.getVersion(connectionSource1);
        assertThat(simpleVersion).isEqualTo(2);
        //System.out.println("Simple version = " + simpleVersion);
        complexVersion = databaseSupport.getVersion(connectionSource2);
        assertThat(complexVersion).isEqualTo(2);
        //System.out.println("Complex version = " + complexVersion);
        // Need to close connections to clear caches etc and ensure updates are picked up
        databaseSupport.close();
        // Now it's OK to use V2 code
        helloTwoDbsMain = new AndroidHelloTwoDbs();
        System.out.println("Calling setUp() in object: " + helloTwoDbsMain);
        helloTwoDbsMain.setUp();
        SimpleTask simpleTask = new SimpleTask("main");
        helloTwoDbsMain.performPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
		// Our string builder for building the content-view
		StringBuilder sb = new StringBuilder();
        ComplexTask complexTask = new ComplexTask("main");
        helloTwoDbsMain.performPersistenceWork(HelloTwoDbsMain.PU_NAME2, complexTask);
        helloTwoDbsMain.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully page at " + System.currentTimeMillis());
        helloTwoDbsMain.displayMessage(sb
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append(simpleTask.getMessage())
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append(complexTask.getMessage())
				.toString());
    }
}
