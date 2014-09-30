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

import java.util.Properties;
import java.util.logging.Level;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

import com.j256.ormlite.support.ConnectionSource;

/**
 * DatabaseAdminImpl
 * Database implementation of android.database.sqlite.SQLiteOpenHelper abstract methods.
 * To support android.database.sqlite.SQLiteOpenHelper
 * @author Andrew Bowley
 * 29/07/2014
 */
public class DatabaseAdminImpl implements DatabaseAdmin
{
    private static final String TAG = "DatabaseAdminImpl";
    private static Log log = JavaLogger.getLogger(TAG);
    
    protected String puName;
    /** Persistence control and configuration implementation */
    protected PersistenceAdmin persistenceAdmin;

    protected Executable task;
    
    /**
     * 
     */
    public DatabaseAdminImpl(String puName, PersistenceAdmin persistenceAdmin)
    {
        this.puName = puName;
        this.persistenceAdmin = persistenceAdmin;
        task = new WorkTracker();
    }

    /**
     * See android.database.sqlite.SQLiteOpenHelper
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @return ConnectionSource used for implementation to allow post-creation operations.
     */
    @Override
    public ConnectionSource onCreate() 
    {
        // Block on any currently executing task 
        waitForTask();
        ConnectionSource connectionSource = persistenceAdmin.getConnectionSource();
        Properties properties = persistenceAdmin.getProperties();
        // Get SQL script file names from persistence.xml properties
        // A filename may be null if operation not supported
        String schemaFilename = properties.getProperty(DatabaseAdmin.DROP_SCHEMA_FILENAME);
        String dropSchemaFilename = properties.getProperty(DatabaseAdmin.SCHEMA_FILENAME);
        String dataFilename = properties.getProperty(DatabaseAdmin.DATA_FILENAME);
        // Database work is executed as background task
        DatabaseWork processFilesCallable = 
                new NativeScriptDatabaseWork(connectionSource, schemaFilename, dropSchemaFilename, dataFilename);    
        task = executeTask(processFilesCallable);
        return connectionSource;
    }

    /**
     * See android.database.sqlite.SQLiteOpenHelper
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     * @return ConnectionSource used for implementation to allow post-creation operations.
     */
    @Override
    public ConnectionSource onUpgrade(int oldVersion, int newVersion)
    {
        // No special arrangements for upgrade by default. Override this class for custom upgrade
        return onCreate();
    }

    /**
     * Wait for currently executing persistence unit task to complete
     * @return WorkStatus Status value will be FINISHED or FAILED
     */
    @Override
    public WorkStatus waitForTask()
    {
        return waitForTask(DatabaseAdmin.MAX_TASK_WAIT_SECS); 
    }
    
    /**
     * Wait up to specified number of seconds for currently executing persistence unit task to complete
     * @param timeoutSecs int
     * @return WorkStatus Status value will be FINISHED or FAILED
     */
    @Override
    public WorkStatus waitForTask(int timeoutSecs) 
    {
        if (timeoutSecs < 0)
            timeoutSecs = DatabaseAdmin.MAX_TASK_WAIT_SECS;
        WorkStatus workStatus = WorkStatus.PENDING;
        if (task.getStatus() == WorkStatus.RUNNING)
        {
            if ((timeoutSecs == 0) && !log.isLoggable(TAG, Level.FINE))
               // Only allow indeterminate wait for debugging
                timeoutSecs = DatabaseAdmin.MAX_TASK_WAIT_SECS;
            if (timeoutSecs == 0)
                synchronized(task)
                {
                    try
                    {
                        task.wait();
                    }
                    catch (InterruptedException e)
                    {
                        log.warn(TAG, "createDatabaseTask interrupted", e);
                    }
                }
            else
                waitTicks(task, timeoutSecs);
            workStatus = task.getStatus();
        }
        return workStatus;
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
     * Execute database work
     * @param processFilesCallable Object containing unit of work to perform SQL statements from a list of files
     * @return Object to track status and notified on completion of work
     */
    protected Executable executeTask(DatabaseWork processFilesCallable)
    {
        // Execute task on transaction commit using Callable
        DatabaseWorkerTask databaseWorkerTask = new DatabaseWorkerTask();
        return databaseWorkerTask.executeTask(processFilesCallable);
    }
    
}
