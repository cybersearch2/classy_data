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
package au.com.cybersearch2.classydb;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyapp.ResourceEnvironment;
import au.com.cybersearch2.classybean.BeanException;
import au.com.cybersearch2.classybean.BeanUtil;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdminImpl;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfig;
import au.com.cybersearch2.classyjpa.persist.PersistenceUnitInfoImpl;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

import com.j256.ormlite.support.ConnectionSource;

/**
 * DatabaseAdminImpl
 * Handler of database create and upgrade events

 * @author Andrew Bowley
 * 29/07/2014
 */
public class DatabaseAdminImpl implements DatabaseAdmin
{
    private static final String TAG = "DatabaseAdminImpl";
    private static Log log = JavaLogger.getLogger(TAG);
    /** Default filename template for upgrade */
    protected String DEFAULT_FILENAME_TEMPLATE = "{2}-upgrade-v{0}-v{1}.sql";
    /** Persistence unit name*/
    protected String puName;
    /** Persistence control and configuration implementation */
    protected PersistenceAdmin persistenceAdmin;
    /** Resource environment provides system-specific file open method. */
    @Inject ResourceEnvironment resourceEnvironment;
    
    /**
     * Construct a DatabaseAdminImpl object
     * @param puName The persistence unit name
     * @param persistenceAdmin The persistence unit connectionSource and properties provider  
     */
    public DatabaseAdminImpl(String puName, PersistenceAdmin persistenceAdmin)
    {
        this.puName = puName;
        this.persistenceAdmin = persistenceAdmin;
        DI.inject(this);
    }

    /**
     * Database create handler.
     * Optionaly runs native scripts to drop and create schema and populate the database with data.
     * Note that because ORMLite uses a ThreadLocal variable for a special connection, this 
     * executes in a single thread.
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        Properties properties = persistenceAdmin.getProperties();
        // Get SQL script file names from persistence.xml properties
        // A filename may be null if operation not supported
        String schemaFilename = properties.getProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME);
        String dropSchemaFilename = properties.getProperty(DatabaseAdmin.SCHEMA_FILENAME);
        String dataFilename = properties.getProperty(DatabaseAdmin.DATA_FILENAME);
        if (!((schemaFilename == null) && (dropSchemaFilename == null) && (dataFilename == null)))
        {
        	// Database work is executed as background task
        	TransactionCallable processFilesCallable = 
                new NativeScriptDatabaseWork(connectionSource, schemaFilename, dropSchemaFilename, dataFilename);    
        	executeTask(connectionSource, processFilesCallable);
        }
    }

    /**
     * Database upgrade handler.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p>  
     * @param connectionSource An open ConnectionSource to be employed for all database activities.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        Properties properties = persistenceAdmin.getProperties();
        // Get SQL script upgrade file name format from persistence.xml properties
        boolean upgradeSupported = false;
        String upgradeFilenameFormat = properties.getProperty(DatabaseAdmin.UPGRADE_FILENAME_FORMAT);
        String filename = null;
        // Default filename format: "{puName}-upgrade-v{old-version}-v{new-version}.sql" 
        if (upgradeFilenameFormat == null)
        {
         	MessageFormat messageFormat = new MessageFormat(DEFAULT_FILENAME_TEMPLATE, resourceEnvironment.getLocale());
        	filename = messageFormat.format(new String[] { Integer.toString(oldVersion), Integer.toString(newVersion), puName }).toString();
        }
        // Custom format can be defined in PU properties. [0] and [1] substitute for old-version and new-version respectively
        else
        {
        	MessageFormat messageFormat = new MessageFormat(upgradeFilenameFormat, resourceEnvironment.getLocale());
      	    filename = messageFormat.format(new String[] { Integer.toString(oldVersion), Integer.toString(newVersion) }).toString();
        }
        InputStream instream = null;
        try
        {
            instream = resourceEnvironment.openResource(filename);
            upgradeSupported = true;
        } 
        catch (IOException e) 
        {
        	log.error(TAG, "Error opening \"" + filename + "\" for database upgrade", e);
		}
        finally
        {
            close(instream, filename);
        }
        if (!upgradeSupported)
        	throw new PersistenceException("\"" + puName + "\" database upgrade from v" + oldVersion + " to v" + newVersion + " is not possible");
        if (log.isLoggable(TAG, Level.INFO))
            log.info(TAG, "Upgrade file \"" + filename + "\" exists: " + upgradeSupported);
    	// Database work is executed in a transaction
        TransactionCallable processFilesCallable = 
            new NativeScriptDatabaseWork(connectionSource, filename);    
    	executeTask(connectionSource, processFilesCallable);
    }
    
	/**
	 * Open database and handle create/upgrade events
	 * @param persistenceConfig Persistence Unit Configuration
	 * @param databaseSupport Database Support for specific database type 
	 */
    public void initializeDatabase(PersistenceConfig persistenceConfig, DatabaseSupport databaseSupport)
    {
        // Ensure database version is up to date.
    	Properties properties = persistenceConfig.getPuInfo().getProperties();
    	int currentDatabaseVersion = PersistenceAdminImpl.getDatabaseVersion(properties);
        // Get a connection to open the database and possibly trigger a create or upgrade event (eg. AndroidSQLite)
        ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
        int reportedDatabaseVersion = databaseSupport.getVersion(connectionSource);
        if (reportedDatabaseVersion != currentDatabaseVersion)
        {   // No assistance provided by helper to trigger create/upgrade event
            // Allow custom creat/upgrade handler
        	OpenHelperCallbacks openHelperCallbacks = getOpenHelperCallbacks(properties);
        	if (reportedDatabaseVersion == 0)
        	{
        		if (openHelperCallbacks == null)
        			onCreate(connectionSource);
        		else
        			openHelperCallbacks.onCreate(connectionSource);
        		// Get database version again in case onCreate() set it
        		reportedDatabaseVersion = databaseSupport.getVersion(connectionSource);
        		if (reportedDatabaseVersion == 0)
        		    databaseSupport.setVersion(1, connectionSource);
        	}
        	else
        	{
        		if (openHelperCallbacks == null)
        			onUpgrade(connectionSource, reportedDatabaseVersion, currentDatabaseVersion);
        		else
        			openHelperCallbacks.onUpgrade(connectionSource, reportedDatabaseVersion, currentDatabaseVersion);
        	}
        	persistenceConfig.checkEntityTablesExist(connectionSource);
        }
        /* TODO - Investigate how to avoid closing an in-memory database which causes all changes to be lost
        try 
        {
			connectionSource.close();
		} 
        catch (IOException e) 
        {
			throw new PersistenceException("Error closing " + puName + " connctionSource");
		}
		*/
    }

    /**
     * Wait for currently executing persistence unit task to complete
     * @param workTracker WorkTracker object used to monitor task
     * @return WorkStatus Status value will be FINISHED or FAILED
     */
    @Override
    public WorkStatus waitForTask(WorkTracker workTracker)
    {
        return waitForTask(workTracker, DatabaseAdmin.MAX_TASK_WAIT_SECS); 
    }
    
    /**
     * Wait up to specified number of seconds for currently executing persistence unit task to complete
     * @param workTracker WorkTracker object used to monitor task
     * @param timeoutSecs int
     * @return WorkStatus Status value will be FINISHED or FAILED
     */
    @Override
    public WorkStatus waitForTask(WorkTracker workTracker, int timeoutSecs) 
    {
        if (timeoutSecs < 0)
            timeoutSecs = DatabaseAdmin.MAX_TASK_WAIT_SECS;
        WorkStatus workStatus = WorkStatus.PENDING;
        if (workTracker.getStatus() == WorkStatus.RUNNING)
        {
            if ((timeoutSecs == 0) && !log.isLoggable(TAG, Level.FINE))
               // Only allow indeterminate wait for debugging
                timeoutSecs = DatabaseAdmin.MAX_TASK_WAIT_SECS;
            if (timeoutSecs == 0)
                synchronized(workTracker)
                {
                    try
                    {
                    	workTracker.wait();
                    }
                    catch (InterruptedException e)
                    {
                        log.warn(TAG, "createDatabaseTask interrupted", e);
                    }
                }
            else
                waitTicks(workTracker, timeoutSecs);
            workStatus = workTracker.getStatus();
        }
        return workStatus;
    }

    /**
     * Execute database work
     * @paramm connectionSource Open ConnectionSource
     * @param processFilesCallable TransactionCallable Object containing unit of work to perform
     */
    protected void executeTask(ConnectionSource connectionSource, TransactionCallable processFilesCallable)
    {
        // Execute task on transaction commit using Callable
    	EntityTransaction transaction = new EntityTransactionImpl(connectionSource, processFilesCallable);
        transaction.begin();
        transaction.commit();
    }
   
    /**
     * Returns OpenHelperCallbacks object, if defined in the PU properties
     * @param properties Properties object
     * @return OpenHelperCallbacks or null if not defined
     */
    protected OpenHelperCallbacks getOpenHelperCallbacks(Properties properties)
    {
        OpenHelperCallbacks openHelperCallbacks = null;
        // Property "open-helper-callbacks-classname"
        String openHelperCallbacksClassname = properties.getProperty(PersistenceUnitInfoImpl.CUSTOM_OHC_PROPERTY);
        if (openHelperCallbacksClassname != null)
        {   // Custom
            try
            {
                openHelperCallbacks = (OpenHelperCallbacks) BeanUtil.newClassInstance(openHelperCallbacksClassname);
            }
            catch(BeanException e)
            {
                throw new PersistenceException(e.getMessage(), e.getCause());
            }
        }
        return openHelperCallbacks;
    }
    
    /**
     * Wait specified number of seconds for task to complte
     *@param task Executable tracking Database work
     *@param timeoutSecs int
     */
    private void waitTicks(Executable task, int timeoutSecs) 
    {
        int tick = 0;
        while (tick < timeoutSecs)
        {
            synchronized(task)
            {
                try
                {
                    task.wait(1000);
                }
                catch (InterruptedException e)
                {
                    log.warn(TAG, "createDatabaseTask interrupted", e);
                    return;
                }
            }
            if (task.getStatus() != WorkStatus.RUNNING)
                break;
            ++tick;
        }
        if (tick == DatabaseAdmin.MAX_TASK_WAIT_SECS)
        {
            log.warn(TAG, "Task for persistence unit \"" + puName + "\" taking more than " + DatabaseAdmin.MAX_TASK_WAIT_SECS + " seconds to complete");
        }
    }

    /**
     * Cloes input stream quietly
     * @param instream InputStream
     * @param filename Name of file being closed
     */
    private void close(InputStream instream, String filename) 
    {
        if (instream != null)
            try
            {
                instream.close();
            }
            catch (IOException e)
            {
                log.warn(TAG, "Error closing file " + filename, e);
            }
    }

}
