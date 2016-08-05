package au.com.cybersearch2.example;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.inject.Singleton;
import javax.persistence.PersistenceException;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableUtils;

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import dagger.Component;
import dagger.Subcomponent;

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
    @Singleton
    @Component(modules = HelloTwoDbsModule.class)
    static interface ApplicationComponent
    {
        PersistenceContext persistenceContext();
        ConnectionType connectionType();
        PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
    }

    @Singleton
    @Subcomponent(modules = PersistenceWorkModule.class)
    static interface PersistenceWorkSubcontext
    {
        Executable executable();
    }

    static public final String TAG = "HelloTwoDbsMain";
    static public final String PU_NAME1 = "simple";
    static public final String PU_NAME2 = "complex";
    /** Named query to find all SimpleData objects */
    static public final String ALL_SIMPLE_DATA = "all_simple_data";
    /** Named query to find all ComplexData objects */
    static public final String ALL_COMPLEX_DATA = "all_complex_data";
    static public final String DATABASE_INFO_NAME = "User_info";
   
    private final static Map<String, Log> logMap;
	public static final Object SEPARATOR_LINE = "------------------------------------------\n"; 

    /** Factory object to create "simple" and "complex" PersistenceUnitAdmin Unit implementations */
    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    protected boolean testInMemory;
    protected PersistenceContext persistenceContext;
    
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
    	testInMemory = true;
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

	public PersistenceContext getPersistenceContext()
	{
	    return persistenceContext;
	}
	
    /**
     * @return the upgrade persistenceContext
     */
    public static PersistenceContext upgradePersistenceContext(PersistenceContext persistenceContextV1) 
    {
        // We cannot load a 2nd persistence.xml to get V2 configuration, so will 
        // update the V1 configuration instead.
        // We need to add the V2 entity classes and change the database version from 1 to 2.
        persistenceContextV1.registerClasses(PU_NAME1, Collections.singletonList("au.com.cybersearch2.example.v2.SimpleData"));
        Properties dbV2 = new Properties();
        dbV2.setProperty(DatabaseAdmin.DATABASE_VERSION, "2");
        persistenceContextV1.putProperties(PU_NAME1, dbV2);
        persistenceContextV1.registerClasses(PU_NAME2, Collections.singletonList("au.com.cybersearch2.example.v2.ComplexData"));
        persistenceContextV1.putProperties(PU_NAME2, dbV2);
        return persistenceContextV1;
    }

    public void setTestInMemory(boolean value)
    {
        testInMemory = value;
    }

    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException
     */
    public void setUp() throws InterruptedException
    {
    	setUp(false);
    }
    
    public void setUp(boolean fromStart) throws InterruptedException
    {
		initializeApplication();
		if (fromStart)
			dropDatabaseTables();
		initializeDatabase();
	    // ConnectionType - if not memory, then database may be populated on startup 
		if (!fromStart && (getConnectionType() != ConnectionType.memory))
            clearDatabaseTables();
    	populateDatabases();
    }

    protected void initializeApplication()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        persistenceContext = createObjectGraph();
    }
    
    protected void initializeDatabase()
    {
        // Note that the table for each entity class will be created in the following step (assuming database is in memory).
        // To populate these tables, call setUp().
        persistenceContext.initializeAllDatabases();
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistenceContext.getPersistenceAdmin(PU_NAME1);
        // Create named queries to find all objects of an entity class.
        // Note QueryForAllGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator allSimpleDataObjects = 
                new QueryForAllGenerator(persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(SimpleData.class, ALL_SIMPLE_DATA, allSimpleDataObjects);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = persistenceContext.getPersistenceAdmin(PU_NAME2);
        QueryForAllGenerator allComplexDataObjects = 
                new QueryForAllGenerator(persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(ComplexData.class, ALL_COMPLEX_DATA, allComplexDataObjects);
    }
    
    /**
     * Launch persistence work to run in background thread
     * @param puName PersistenceUnitAdmin Unit name
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
            public void doTask(EntityManagerLite entityManager)
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
        return getExecutable(puName, todo);
    }

    /**
     * Launch persistence work to run in background thread and wait for completion
     * @param puName PersistenceUnitAdmin Unit name
     * @param persistenceTask PersistenceTask object
     * @throws InterruptedException
     */
    public void performPersistenceWork(final String puName, final PersistenceTask persistenceTask) throws InterruptedException
    {
    	launchPersistenceWork(puName, persistenceTask).waitForTask();
    }
    
	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
	 * Override to run with different database and/or platform. 
	 */
	protected PersistenceContext createObjectGraph()
	{
	    HelloTwoDbsModule helloTwoDbsModule = new HelloTwoDbsModule();
	    helloTwoDbsModule.setTestInMemory(testInMemory);
        component = 
                DaggerHelloTwoDbsMain_ApplicationComponent.builder()
                .helloTwoDbsModule(helloTwoDbsModule)
                .build();
        return component.persistenceContext();
 	}

    public Executable getExecutable(String puName, PersistenceWork persistenceWork)
    {
        persistenceWorkModule = new PersistenceWorkModule(puName, true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }

    protected ConnectionType getConnectionType()
	{
	    return component.connectionType();
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
        // PersistenceUnitAdmin task clears Simple table the helloTwoDb1.db database using JPA. 
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
    	// PersistenceUnitAdmin task drops then creates Complex table in the helloTwoDb2.db database using JPA.
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
	
	public void dropDatabaseTables() throws InterruptedException
	{
        // PersistenceUnitAdmin task clears Simple table the helloTwoDb1.db database using JPA. 
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
		    			TableUtils.dropTable(simpleDao.getConnectionSource(), SimpleData.class, false);
				} 
		    	catch (SQLException e) 
		    	{
					throw new PersistenceException(e);
				}
                logMessage(PU_NAME1, "Dropped table \"Simple\"");
			}});
		dropDatabaseVersionTable(PU_NAME1);
    	// PersistenceUnitAdmin task drops then creates Complex table in the helloTwoDb2.db database using JPA.
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
		    			TableUtils.dropTable(complexDao.getConnectionSource(), ComplexData.class, false);
				} 
		    	catch (SQLException e) 
		    	{
					throw new PersistenceException(e);
				}
				logMessage(PU_NAME2, "Dropped table \"Complex\"");
			}});
		dropDatabaseVersionTable(PU_NAME2);
	}
	
	public void dropDatabaseVersionTable(String puName) throws InterruptedException
	{
		ConnectionSource connectionSource = persistenceContext.getPersistenceAdmin(puName).getConnectionSource();
		boolean tableExists = false;
		DatabaseConnection connection = null;
		try 
		{
			connection = connectionSource.getReadOnlyConnection(DATABASE_INFO_NAME);
			tableExists = connection.isTableExists(DATABASE_INFO_NAME);
			if (tableExists)
			{
				String dropTableStatement = "DROP TABLE " + DATABASE_INFO_NAME ;
				connection.executeStatement(dropTableStatement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
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
	}
	

	/**
	 * Populate databases with initial sample data
	 * @throws InterruptedException
	 */
	public void populateDatabases() throws InterruptedException
	{
        // PersistenceUnitAdmin task adds 2 SimpleData entity objects to the helloTwoDb1.db database using JPA. 
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
    	// PersistenceUnitAdmin task adds 2 ComplexData entity objects to the helloTwoDb2.db database using JPA.
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

    public void shutdown()
    {
        String[] puNames = { PU_NAME1, PU_NAME2 }; 
        {
            for (String puName: puNames)
                persistenceContext.getPersistenceAdmin(puName).close();
        }
    }
    
	/**
	 * Public accessor for logMessage()
	 * @param tag
	 * @param message
	 */
	public static void logInfo(String tag, String message) 
	{
        Log log = logMap.get(tag);
        if ((log != null) && log.isLoggable(tag, Level.INFO))
        {
            log.info(tag, message);
        }
	}
}
