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

import au.com.cybersearch2.classytask.Executable;


/**
 * HelloTwoDbsTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class HelloTwoDbsTest
{
    private static HelloTwoDbsMain_v1 helloTwoDbsMain;

    @Before
    public void setUp() throws Exception 
    {
        if (helloTwoDbsMain == null)
            helloTwoDbsMain = new HelloTwoDbsMain_v1();
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

    @Test 
    public void test_hello_two_dbs_parallel_jpa() throws Exception
    {
        SimpleTask simpleTask = new SimpleTask("main");
        ComplexTask complexTask = new ComplexTask("main");
        Executable exe1 = helloTwoDbsMain.launchPersistenceWork(HelloTwoDbsMain.PU_NAME1, simpleTask);
        Executable exe2 = helloTwoDbsMain.launchPersistenceWork(HelloTwoDbsMain.PU_NAME2, complexTask);
        exe1.waitForTask();
        exe2.waitForTask();
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

