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
package au.com.cybersearch2.example;

import au.com.cybersearch2.classyinject.DI;

/**
 * H2ManyToManyMain
 * @author Andrew Bowley
 * 16 May 2015
 * 
 * H2 database version uses a connection source which allows multiple connections.
 * 
 * ORIGINAL COMMENTS:
 * Main sample routine to show how to do many-to-many type relationships. It also demonstrates how we user inner queries
 * as well foreign objects.
 * 
 * <p>
 * <b>NOTE:</b> We use asserts in a couple of places to verify the results but if this were actual production code, we
 * would have proper error handling.
 * </p>
 * <p>
 * CLASSYTOOLS COMMENTS:
 * </p>
 * <p>
 * This example shows JPA in action. The application code exempifies use of a standard persistence interface. 
 * The OrmLite implemention is mostly hidden in library code, but does show up in named queries where, to keep things
 * lightweight, OrmLite QueryBuilder is employed in place of a JQL implementation @see ManyToManyGenerator.
 * </p>
 * <p>
 * Also noteable is dependency injection using Dagger @see ManyToManyModule. If one studies the details, what the
 * dependency inject allows is flexibility in 3 ways:
 * <ol>
 * <li>Choice of database - the PersistenceContext binding</li>
 * <li>Location of resource files such as persistence.xml (and Locale too) - the ResourceEnvironment binding</li>
 * <li>How to reduce background thread priority - the ThreadHelper binding</li>
 * </ol>
 * </p>
 */
public class H2ManyToManyMain extends ManyToManyMain 
{

    /**
     * Create H2ManyToManyMain object
     * This creates and populates the database using JPA, provides verification logic and runs a test from main().
     */
	public H2ManyToManyMain() 
	{
		super();
	}

    /**
     * Test ManyToMany association
     * @param args Not used
     */
	public static void main(String[] args)
	{
        new H2ManyToManyMain().runApplication();
	}
	

	/**
	 * Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object.
	 * Override to run with different database and/or platform. 
	 * Refer au.com.cybersearch2.example.AndroidManyToMany in classyandroid module for Android example.
	 */
	@Override
	protected void createObjectGraph()
	{
         new DI(new H2ManyToManyModule()).validate();
	}

}
