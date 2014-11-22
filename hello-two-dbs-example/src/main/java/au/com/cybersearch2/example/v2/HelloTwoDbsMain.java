package au.com.cybersearch2.example.v2;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.PersistenceException;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.util.logging.Level;

import au.com.cybersearch2.classylog.*;
import au.com.cybersearch2.classydb.DatabaseSupport;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.ConnectionSourceFactory;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.example.PersistenceTask;
import au.com.cybersearch2.example.QueryForAllGenerator;

/**
 * Version 2 of HelloTwoDbsMain introduces a new "quote" field to both SimpleData and ComplexData entities.
 * CLASSYTOOLS COMMENTS:
 */
public class HelloTwoDbsMain 
{
    static public final String TAG = "HelloTwoDbsMain";
    static public final String PU_NAME1 = "simple";
    static public final String PU_NAME2 = "complex";
    //static public final String PU_NAME1_v1 = "simple_v1";
    //static public final String PU_NAME2_v1 = "complex_v1";
    static public final String SIMPLE_DATA_TABLENAME = "Simple";
    static public final String COMPLEX_DATA_TABLENAME = "Complex";
    /** Named query to find all SimpleData objects */
    static public final String ALL_SIMPLE_DATA = "all_simple_data";
    /** Named query to find all ComplexData objects */
    static public final String ALL_COMPLEX_DATA = "all_complex_data";
    static public final String DATABASE_INFO_NAME = "User_Info";
    static public final int DATABASE_VERSION = 2;
   
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
     * This creates and populates the database using JPA, provides verification logic and runs a test from main().
     */
    public HelloTwoDbsMain() 
    {
    	singleton = this;
    }

    /**
     * Test 2 Databases accessed by application version 2
     * @param args Not used
     */
	public static void main(String[] args)
	{
        HelloTwoDbsMain helloTwoDbsMain = new HelloTwoDbsMain();
        try
        {
            helloTwoDbsMain.setUp();
            SimpleTask simpleTask = new SimpleTask("main");
            helloTwoDbsMain.performPersistenceWork(PU_NAME1, simpleTask);
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
            ComplexTask complexTask = new ComplexTask("main");
            helloTwoDbsMain.performPersistenceWork(PU_NAME2, complexTask);
            helloTwoDbsMain.logMessage(TAG, "Test completed successfully at " + System.currentTimeMillis());
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
        System.exit(0);
	}
	
    /**
     * Initialize entity tables ensuring version is correct and contains initial data. 
     * Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException
     */
    public void setUp() throws InterruptedException
    {
    	if (!applicationInitialized)
    	{
    		initializeApplication();
    		initializeDatabase();
    		// Get database version and populate it with initial data if newly created
    		int versionDb1 = getDatabaseVersion(PU_NAME1);
    		if (versionDb1 == 0)
    		{
    			updateDatabaseVersion(PU_NAME1);
    			populateDatabase1();
    			versionDb1 = DATABASE_VERSION;
    		}
    		int versionDb2 = getDatabaseVersion(PU_NAME2);
    		if (versionDb2 == 0)
    		{
    			updateDatabaseVersion(PU_NAME2);
    			populateDatabase2();
    			versionDb2 = DATABASE_VERSION;
    		}
            logMessage(TAG, PU_NAME1 + " version = " + versionDb1);
            logMessage(TAG, PU_NAME2 + " version = " + versionDb2);
			if (versionDb1 == 1)
			{
				upgradeDatabase();
		        // Update version value in databases
				updateDatabaseVersion(PU_NAME1);
		 		updateDatabaseVersion(PU_NAME2);
		        logMessage(TAG, "Upgraded database version from 1 to " + DATABASE_VERSION);
			}
			applicationInitialized = true;
    	}
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
        Persistence persistence1 = persistenceFactory.getPersistenceUnit(PU_NAME1);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistence1.getPersistenceAdmin();
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
	 * Upgrade database from version 1 to version 2.
	 * @throws InterruptedException
	 */
	public void upgradeDatabase() throws InterruptedException
	{
		DatabaseUpgrader upgrader = new DatabaseUpgrader();
        displayMessage(upgrader.doUpgrade(1, DATABASE_VERSION));
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
	 * Populate database 1 with initial sample data
	 * @throws InterruptedException
	 */
	public void populateDatabase1() throws InterruptedException
	{
        // Persistence task adds 1 SimpleData entity object to the helloTwoDb1.db database using JPA. 
		performPersistenceWork(PU_NAME1, getPopulateTask1());
	}

	protected PersistenceTask getPopulateTask1()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				long millis = System.currentTimeMillis();
				// create some entries in the onCreate
				SimpleData simple1 = new SimpleData("Sarah", millis, QuoteSource.getQuote());
                entityManager.persist(simple1);
                SimpleData simple2 = new SimpleData("George", millis + 1, QuoteSource.getQuote());
                entityManager.persist(simple2);
                logMessage(PU_NAME1, "Created 2 new SimpleData entries: " + millis);
			}};
	}
	
	/**
	 * Populate database 2 with initial sample data
	 * @throws InterruptedException
	 */
	public void populateDatabase2() throws InterruptedException
	{
    	// Persistence task adds 1 ComplexData entity object to the helloTwoDb2.db database using JPA.
		performPersistenceWork(PU_NAME2, getPopulateTask2());
	}

	protected PersistenceTask getPopulateTask2()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				long millis = System.currentTimeMillis();
				// create some entries in the onCreate
				ComplexData complex1 = new ComplexData(millis, QuoteSource.getQuote());
				entityManager.persist(complex1);
				ComplexData complex2 = new ComplexData(millis + 1, QuoteSource.getQuote());
				entityManager.persist(complex2);
				logMessage(PU_NAME2, "Created 2 new ComplexData entries: " + millis);
			}};
	}
	
	/**
	 * Returns database version. Shows how to get a ConnectionSource to perform OrmLite operations without JPA.
	 * @param puName Persistence Unit name
	 * @return int
	 */
	public int getDatabaseVersion(String puName) throws InterruptedException
	{
		ConnectionSourceFactory connectionSourceFactory = persistenceFactory.getPersistenceUnit(puName).getPersistenceAdmin();
		ConnectionSource connectionSource = connectionSourceFactory.getConnectionSource();
		DatabaseSupport databaseSupport = persistenceFactory.getDatabaseSupport();
		return databaseSupport.getVersion(connectionSource);
	}


	/**
	 * Update database version to current
	 * @param puName Persistence Unit name
	 */
	public void updateDatabaseVersion(String puName)
	{
		ConnectionSourceFactory connectionSourceFactory = persistenceFactory.getPersistenceUnit(puName).getPersistenceAdmin();
		ConnectionSource connectionSource = connectionSourceFactory.getConnectionSource();
		DatabaseSupport databaseSupport = persistenceFactory.getDatabaseSupport();
		databaseSupport.setVersion(DATABASE_VERSION, connectionSource);
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
