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

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;

/**
 * PersistenceWorker
 * Base class for execution of persistence tasks. Aggregates error count for reporting success of multiple executions.
 * @author andrew
 * 25 Jan 2016
 */
public abstract class PersistenceWorker
{
    /** Application persistence interface */
	protected PersistenceContext persistenceContext;
    /** JPA container to execute named query */
	protected String persistenceUnit;
	/** Aggregate count of errors to track asynchronous work progress */
	protected int errorCount;

	/**
	 * Construct PersistenceWorker object
     * @param persistenceUnit Name of persistence unit defined in persistence.xml configuration file
	 * @param persistenceContext Application persistence interface
	 */
	public PersistenceWorker(String persistenceUnit, PersistenceContext persistenceContext)
	{
		this.persistenceUnit = persistenceUnit;
		this.persistenceContext = persistenceContext;
	}

    /**
     * Return persistence context
     * @return PersistenceContext object
     */ 	
    public PersistenceContext getPersistenceContext() 
    {
		return persistenceContext;
	}

    /**
     * Return persistence unit name
     * @return String
     */
	public String getPersistenceUnit() 
	{
		return persistenceUnit;
	}

    /**
     * Execute task
     * @param persistenceWork Task to execute
     * @return Executable object to track progress
     */
    public abstract Executable doWork(PersistenceWork persistenceWork);

	/**
	 * Reset error count
	 */
	public void resetErrorCount()
	{
		errorCount = 0;
	}

	/**
	 * Returns aggregate error count
	 * @return int
	 */
	int getErrorCount()
	{
		return errorCount;
	}

}
