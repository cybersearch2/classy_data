package au.com.cybersearch2.example;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

import java.util.logging.Level;

import au.com.cybersearch2.classylog.*;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.ConnectionSourceFactory;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.Executable;

/**
 * ORIGINAL COMMENTS:
 * This is an application similar to HelloAndroid but it supports two
 * databases and manages the helper on its own.

 * CLASSYTOOLS COMMENTS:
 * This version shows supporting two databases requires no more than placing two persistence units in 
 * the persistence.xml configuration file. Each persistence unit is then referenced in the code by name.
 * To support version upgrades, this application also includes a database version stored in a 
 * "User_Info" table on each database - see test java au.com.cybersearch2.example.v2.HelloTwoDbsMain. 
 * 
 * Also demonstrated is the important principle of layering an application to separate concerns.
 * Porting the code to Android requires only changing the dependency injection configuration.
 * See project "AndroidHelloTwoDbs".
 */
public class HelloTwoDbsMain 
{
    static public final String TAG = "HelloTwoDbsMain";
    static public final String PU_NAME1 = "simple";
    static public final String PU_NAME2 = "complex";
    /** Named query to find all SimpleData objects */
    static public final String ALL_SIMPLE_DATA = "all_simple_data";
    /** Named query to find all ComplexData objects */
    static public final String ALL_COMPLEX_DATA = "all_complex_data";
    static public final String DATABASE_INFO_NAME = "User_Info";
   
    private final static Map<String, Log> logMap;
	public static final Object SEPARATOR_LINE = "------------------------------------------\n"; 
	protected static boolean applicationInitialized;

    /** Dependency injection data object */
    private HelloTwoDbsModule helloTwoDbsModule;
    private static HelloTwoDbsMain singleton;
    
    /** Factory object to create "simple" and "complex" Persistence Unit implementations */
    @Inject PersistenceFactory persistenceFactory;
    /** ConnectionType - if not memory, then database may be populated on startup */
    @Inject ConnectionType connectionType;
    
    static
    {
    	logMap = new HashMap<String, Log>(2); 
    	logMap.put(TAG, JavaLogger.getLogger(TAG));
    	logMap.put(PU_NAME1, JavaLogger.getLogger(PU_NAME1));
    	logMap.put(PU_NAME2, JavaLogger.getLogger(PU_NAME2));
    }
    
    /**
     * Create HelloTwoDbsMain object
     * This creates and populates the database using JPA and runs a test from main().
     */
    public HelloTwoDbsMain() 
    {
    	singleton = this;
    }

    /**
     * Test 2 Databases accessed by application
     * @param args Not used
     */
	public static void main(String[] args)
	{
        HelloTwoDbsMain helloTwoDbsMain = new HelloTwoDbsMain();
        try
        {
        	// Setup clears tables if using file databases and populates tables with sample data
            helloTwoDbsMain.setUp();
            // Run tasks serially to exercise databases
            SimpleTask simpleTask = new SimpleTask("main");
            helloTwoDbsMain.performPersistenceWork(PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
            ComplexTask complexTask = new ComplexTask("main");
            helloTwoDbsMain.performPersistenceWork(PU_NAME2, complexTask);
            helloTwoDbsMain.logMessage(TAG, "Test completed successfully page at " + System.currentTimeMillis());
            helloTwoDbsMain.displayMessage(sb
					.append(SEPARATOR_LINE)
					.append(simpleTask.getMessage())
					.append(SEPARATOR_LINE)
					.append(complexTask.getMessage())
					.toString());
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
        	helloTwoDbsMain.shutdown();
        }
	}
	
    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException
     */
    public void setUp() throws InterruptedException
    {
    	if (!applicationInitialized)
    	{
    		initializeApplication();
    		initializeDatabase();
    		int versionDb1 = getDatabaseVersion(PU_NAME1);
    		int versionDb2 = getDatabaseVersion(PU_NAME2);
            logMessage(TAG, PU_NAME1 + " version = " + versionDb1);
            logMessage(TAG, PU_NAME2 + " version = " + versionDb2);
    		if (connectionType != ConnectionType.memory)
    		{
    			clearDatabaseTables();
    		}
    		applicationInitialized = true;
    	}
    	populateDatabases();
    }

    public void shutdown()
    {
        String[] puNames = { PU_NAME1, PU_NAME2 }; 
        {
        	for (String puName: puNames)
        	{
        		persistenceFactory.getPersistenceUnit(puName).getPersistenceAdmin().close();
        	}
        }
    }

    protected void initializeApplication()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        createObjectGraph();
        // Inject persistenceFactory and create persistence units.
        DI.inject(this); 
    }
    
    protected void initializeDatabase()
    {
        // Note that the table for each entity class will be created in the following step (assuming database is in memory).
        // To populate these tables, call setUp().
    	persistenceFactory.initializeAllDatabases();
        Persistence persistence1 = persistenceFactory.getPersistenceUnit(PU_NAME1);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistence1.getPersistenceAdmin();
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator allSimpleDataObjects = 
                new QueryForAllGenerator(persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(SimpleData.class, ALL_SIMPLE_DATA, allSimpleDataObjects);
        Persistence persistence2 = persistenceFactory.getPersistenceUnit(PU_NAME2);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = persistence2.getPersistenceAdmin();
        QueryForAllGenerator allComplexDataObjects = 
                new QueryForAllGenerator(persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(ComplexData.class, ALL_COMPLEX_DATA, allComplexDataObjects);
    }
    
    /**
     * Launch persistence work to run in background thread
     * @param puName Persistence Unit name
     * @param persistenceTask PersistenceTask object
     * @return Executable object to signal completion of task
     * @throws InterruptedException
     */
    Executable launchPersistenceWork(final String puName, final PersistenceTask persistenceTask) throws InterruptedException
    {
        // There will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork todo = new PersistenceWork(){

            @Override
            public void doInBackground(EntityManagerLite entityManager)
            {
            	persistenceTask.doTask(entityManager);
                // Database updates commited upon exit
            }

            @Override
            public void onPostExecute(boolean success)
            {
                if (!success)
                    throw new IllegalStateException("Database set up failed. Check console for error details.");
            }

            @Override
            public void onRollback(Throwable rollbackException)
            {
                throw new IllegalStateException("Database set up failed. Check console for stack trace.", rollbackException);
            }
        };
        // Execute work and wait synchronously for completion
        PersistenceContainer container = new PersistenceContainer(puName);
        return container.executeTask(todo);
    }

    /**
     * Launch persistence work to run in background thread and wait for completion
     * @param puName Persistence Unit name
     * @param persistenceTask PersistenceTask object
     * @throws InterruptedException
     */
    public void performPersistenceWork(final String puName, final PersistenceTask persistenceTask) throws InterruptedException
    {
    	Executable exe = launchPersistenceWork(puName, persistenceTask);
        waitForTask(exe);
        logMessage(puName, "Task final status = " + exe.getStatus().toString());
    }
    
    /**
     * Wait sychronously for task completion
     * @param exe Executable object returned upon starting task
     * @throws InterruptedException Should not happen
     */
    protected void waitForTask(Executable exe) throws InterruptedException
    {
        synchronized (exe)
        {
            exe.wait();
        }
    }

	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
	 * Override to run with different database and/or platform. 
	 */
	protected void createObjectGraph()
	{
        // 
        helloTwoDbsModule = new HelloTwoDbsModule();
        new DI(helloTwoDbsModule).validate();
	}

	/**
	 * Log message
	 * @param tag
	 * @param message
	 */
	public void logMessage(String tag, String message)
	{
		Log log = logMap.get(tag);
		if ((log != null) && log.isLoggable(tag, Level.INFO))
		{
			log.info(tag, message);
		}
	}

	/**
	 * Display message to user
	 * @param message
	 */
	public void displayMessage(String message)
	{
		System.out.println(message);
	}

	/**
	 * Clear database tables if they exist
	 * @throws InterruptedException
	 */
	public void clearDatabaseTables() throws InterruptedException
	{
        // Persistence task clears Simple table the helloTwoDb1.db database using JPA. 
		performPersistenceWork(PU_NAME1, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
		    	@SuppressWarnings("unchecked")
				PersistenceDao<SimpleData, Integer> simpleDao = (PersistenceDao<SimpleData, Integer>) delegate.getDaoForClass(SimpleData.class);
		    	try 
		    	{
		    		if (simpleDao.isTableExists())
		    			TableUtils.clearTable(simpleDao.getConnectionSource(), SimpleData.class);
				} 
		    	catch (SQLException e) 
		    	{
					throw new PersistenceException(e);
				}
                logMessage(PU_NAME1, "Cleared table \"Simple\"");
			}});
    	// Persistence task drops then creates Complex table in the helloTwoDb2.db database using JPA.
		performPersistenceWork(PU_NAME2, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
		    	@SuppressWarnings("unchecked")
		    	PersistenceDao<ComplexData, Integer> complexDao = (PersistenceDao<ComplexData, Integer>) delegate.getDaoForClass(ComplexData.class);
		    	try 
		    	{
		    		if (complexDao.isTableExists())
		    			TableUtils.clearTable(complexDao.getConnectionSource(), ComplexData.class);
				} 
		    	catch (SQLException e) 
		    	{
					throw new PersistenceException(e);
				}
				logMessage(PU_NAME2, "Cleared table \"Complex\"");
			}});
	}

	/**
	 * Populate databases with initial sample data
	 * @throws InterruptedException
	 */
	public void populateDatabases() throws InterruptedException
	{
        // Persistence task adds 2 SimpleData entity objects to the helloTwoDb1.db database using JPA. 
		performPersistenceWork(PU_NAME1, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				long millis = System.currentTimeMillis();
				// create some entries in the onCreate
				SimpleData simple1 = new SimpleData("Alice", millis);
                entityManager.persist(simple1);
                SimpleData simple2 = new SimpleData("Robert", millis + 1);
                entityManager.persist(simple2);
                logMessage(PU_NAME1, "Created new entries in onCreate: " + millis);
			}});
    	// Persistence task adds 2 ComplexData entity objects to the helloTwoDb2.db database using JPA.
		performPersistenceWork(PU_NAME2, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				long millis = System.currentTimeMillis();
				// create some entries in the onCreate
				ComplexData complex1 = new ComplexData(millis);
				entityManager.persist(complex1);
				ComplexData complex2 = new ComplexData(millis + 1);
				entityManager.persist(complex2);
				logMessage(PU_NAME2, "Created new ComplexData entries in onCreate: " + millis);
			}});
	}

	/**
	 * Returns database version. Shows how to get a ConnectionSource to perform OrmLite operations without JPA.
	 * @param puName Persistence Unit name
	 * @return int
	 */
	public int getDatabaseVersion(String puName)
	{
		int databaseVersion = 1;
		ConnectionSourceFactory connectionSourceFactory = persistenceFactory.getPersistenceUnit(puName).getPersistenceAdmin();
		ConnectionSource connectionSource = connectionSourceFactory.getConnectionSource();
		boolean tableExists = false;
		DatabaseConnection connection = null;
		try 
		{
			connection = connectionSource.getReadOnlyConnection();
			tableExists = connection.isTableExists(DATABASE_INFO_NAME);
			if (tableExists)
			{
				databaseVersion = (int)connection.queryForLong("select version from " + DATABASE_INFO_NAME);
			}
			else
			{
				String createTableStatement = "CREATE TABLE `" + DATABASE_INFO_NAME + "` (`version` INTEGER )";
				connection.executeStatement(createTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
				String initTableStatement = "INSERT INTO `" + DATABASE_INFO_NAME + "` (`version`) values (1)";
				connection.executeStatement(initTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
			}
		} 
		catch (SQLException e) 
		{
			throw new PersistenceException(e);
		} 
		finally 
		{
			try 
			{
				connectionSource.releaseConnection(connection);
			} 
			catch (SQLException e) 
			{
				e.printStackTrace();
			}
		}
		return databaseVersion;
	}

	/**
	 * Public accessor for logMessage()
	 * @param tag
	 * @param message
	 */
	public static void logInfo(String tag, String message) 
	{
		singleton.logMessage(tag, message);
	}
}
