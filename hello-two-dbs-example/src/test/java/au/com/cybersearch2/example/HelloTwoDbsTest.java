package au.com.cybersearch2.example;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classytask.Executable;


/**
 * ManyToManyTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class HelloTwoDbsTest
{
    private static HelloTwoDbsMain helloTwoDbsMain;

    @Before
    public void setUp() throws Exception 
    {
        if (helloTwoDbsMain == null)
            helloTwoDbsMain = new HelloTwoDbsMain();
        helloTwoDbsMain.setUp();
    } 

    @After
    public void shutdown()
    {
    	helloTwoDbsMain.shutdown();
    }
    
    @Test 
    public void test_hello_two_dbs_serial_jpa() throws Exception
    {
        PersistenceContainer container1 = new PersistenceContainer(HelloTwoDbsMain.PU_NAME1);
        SimpleTask simpleTask = new SimpleTask("main");
        helloTwoDbsMain.waitForTask(container1.executeTask(simpleTask));
		// Our string builder for building the content-view
		StringBuilder sb = new StringBuilder();
        PersistenceContainer container2 = new PersistenceContainer(HelloTwoDbsMain.PU_NAME2);
        ComplexTask complexTask = new ComplexTask("main");
        helloTwoDbsMain.waitForTask(container2.executeTask(complexTask));
        helloTwoDbsMain.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully page at " + System.currentTimeMillis());
        helloTwoDbsMain.displayMessage(sb
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append(simpleTask.getMessage())
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append(complexTask.getMessage())
				.toString());
    }

    @Test 
    public void test_hello_two_dbs_parallel_jpa() throws Exception
    {
        PersistenceContainer container1 = new PersistenceContainer(HelloTwoDbsMain.PU_NAME1);
        PersistenceContainer container2 = new PersistenceContainer(HelloTwoDbsMain.PU_NAME2);
        SimpleTask simpleTask = new SimpleTask("main");
        ComplexTask complexTask = new ComplexTask("main");
        Executable exe1 = container1.executeTask(simpleTask);
        Executable exe2 = container2.executeTask(complexTask);
        helloTwoDbsMain.waitForTask(exe1);
        helloTwoDbsMain.waitForTask(exe2);
        helloTwoDbsMain.logMessage(HelloTwoDbsMain.TAG, "Test completed successfully page at " + System.currentTimeMillis());
		// Our string builder for building the content-view
		StringBuilder sb = new StringBuilder();
        helloTwoDbsMain.displayMessage(sb
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append(simpleTask.getMessage())
				.append(HelloTwoDbsMain.SEPARATOR_LINE)
				.append(complexTask.getMessage())
				.toString());
    }

}

