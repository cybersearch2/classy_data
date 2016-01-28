/**
 * 
 */
package au.com.cybersearch2.classyjpa.persist;

import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;

/**
 * @author andrew
 *
 */
public abstract class PersistenceWorker
{
	protected PersistenceContext persistenceContext;
    /** JPA container to execute named query */
	protected String persistenceUnit;
	/** Aggregate count of errors to track asynchronous work progress */
	protected int errorCount;

	/**
     * @param persistenceUnit Name of persistence unit defined in persistence.xml configuration file
	 * @param persistenceContext
	 */
	public PersistenceWorker(String persistenceUnit, PersistenceContext persistenceContext)
	{
		this.persistenceUnit = persistenceUnit;
		this.persistenceContext = persistenceContext;
	}
	
    public PersistenceContext getPersistenceContext() 
    {
		return persistenceContext;
	}

	public String getPersistenceUnit() 
	{
		return persistenceUnit;
	}

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
