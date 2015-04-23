package au.com.cybersearch2.example.v2;
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

import java.util.Collections;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;


/**
 * HelloTwoDbsTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class HelloTwoDbsUpgradeTest
{
    private static HelloTwoDbsMain helloTwoDbsMain;

    @Before
    public void setUp() throws Exception 
    {
        // Run version 1 of example which will leave 2 database tables populated with version 1 objects.
    	au.com.cybersearch2.example.HelloTwoDbsMain helloTwoDbsMain_v1 = new au.com.cybersearch2.example.HelloTwoDbsMain();
        try
        {
        	// Set up v1 from start which causes the database tables to be dropped and the version reset to 0
        	helloTwoDbsMain_v1.setUp(true);
        	au.com.cybersearch2.example.SimpleTask simpleTask = new au.com.cybersearch2.example.SimpleTask("main");
            helloTwoDbsMain_v1.performPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
			au.com.cybersearch2.example.ComplexTask complexTask = new au.com.cybersearch2.example.ComplexTask("main");
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
    } 

    @After
    public void shutdown()
    {
    	helloTwoDbsMain.shutdown();
    }
 
    @Test 
    public void test_hello_two_dbs_serial_jpa() throws Exception
    {
        if (helloTwoDbsMain == null)
            helloTwoDbsMain = new HelloTwoDbsMain();
        // We cannot load a 2nd persistence.xml to get V2 configuration, so will 
        // update the V1 configuration instead.
        // We need to add the V2 entity classes and change the database version from 1 to 2.
        PersistenceContext persistenceContext = new PersistenceContext();
        persistenceContext.registerClasses(HelloTwoDbsMain.PU_NAME1, Collections.singletonList("au.com.cybersearch2.example.v2.SimpleData"));
        Properties dbV2 = new Properties();
        dbV2.setProperty(DatabaseAdmin.DATABASE_VERSION, "2");
        persistenceContext.putProperties(HelloTwoDbsMain.PU_NAME1, dbV2);
        persistenceContext.registerClasses(HelloTwoDbsMain.PU_NAME2, Collections.singletonList("au.com.cybersearch2.example.v2.ComplexData"));
        persistenceContext.putProperties(HelloTwoDbsMain.PU_NAME2, dbV2);
        // Use version of set up which does not include Dependency Injection initialization
        helloTwoDbsMain.setUpNoDI();
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

