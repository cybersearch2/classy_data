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
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.ConnectionSourceFactory;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classytask.Executable;

/**
 * ORIGINAL COMMENTS:
 * CLASSYTOOLS COMMENTS:
 */
public class HelloTwoDbsMain 
{
	interface PersistenceTask
	{
		void doTask(EntityManagerLite entityManager);
	}
	
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
	protected static boolean databaseInitialized;

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
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        createObjectGraph();
        // Inject persistenceFactory and create persistence units.
        DI.inject(this); 
        // Note that the table for each entity class will be created in the following step (assuming database is in memory).
        // To populate these tables, call setUp().
        Persistence persistence1 = persistenceFactory.getPersistenceUnit(PU_NAME1);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistence1.getPersistenceAdmin();
        // Create named queries to exploit UserPost join table.
        // Note ManyToManyGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator allSimpleDataObjects = 
                new QueryForAllGenerator(persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(SimpleData.class, ALL_SIMPLE_DATA, allSimpleDataObjects);
        Persistence persistence2 = persistenceFactory.getPersistenceUnit(PU_NAME2);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = persistence2.getPersistenceAdmin();
        // Create named queries to exploit UserPost join table.
        // Note ManyToManyGenerator class is reuseable as it allows any Many to Many association to be queried.
        QueryForAllGenerator allComplexDataObjects = 
                new QueryForAllGenerator(persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(ComplexData.class, ALL_COMPLEX_DATA, allComplexDataObjects);
    }

    /**
     * Populate entity tables. Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException
     */
    public void setUp() throws InterruptedException
    {
    	if (!databaseInitialized)
    	{
    		int versionDb1 = getDatabaseVersion(PU_NAME1);
    		int versionDb2 = getDatabaseVersion(PU_NAME2);
            logMessage(TAG, PU_NAME1 + " version = " + versionDb1);
            logMessage(TAG, PU_NAME2 + " version = " + versionDb2);
    		if (connectionType != ConnectionType.memory)
    		{
    			clearDatabaseTables();
    		}
    		databaseInitialized = true;
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

    void performPersistenceWork(final String puName, final PersistenceTask persistenceTask) throws InterruptedException
    {
        // There will be an enclosing transaction to ensure data consistency.
        // Any failure will result in an IllegalStateExeception being thrown from
        // the calling thread.
        PersistenceWork setUpWork = new PersistenceWork(){

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
        waitForTask(container.executeTask(setUpWork));
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
        logMessage(TAG, "Task final status = " + exe.getStatus().toString());
    }

    /**
     * Test ManyToMany association
     * @param args Not used
     */
	public static void main(String[] args)
	{
        HelloTwoDbsMain helloTwoDbsMain = new HelloTwoDbsMain();
        try
        {
            helloTwoDbsMain.setUp();
            PersistenceContainer container1 = new PersistenceContainer(PU_NAME1);
            SimpleTask simpleTask = new SimpleTask("main");
            helloTwoDbsMain.waitForTask(container1.executeTask(simpleTask));
			// Our string builder for building the content-view
			StringBuilder sb = new StringBuilder();
            PersistenceContainer container2 = new PersistenceContainer(PU_NAME2);
            ComplexTask complexTask = new ComplexTask("main");
            helloTwoDbsMain.waitForTask(container2.executeTask(complexTask));
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
	 * Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object.
	 * Override to run with different database and/or platform. 
	 * @see au.com.cybersearch2.example.AndroidManyToMany in classyandroid module for Android example.
	 */
	protected void createObjectGraph()
	{
        // 
        helloTwoDbsModule = new HelloTwoDbsModule();
        new DI(helloTwoDbsModule).validate();
	}
	
	public void logMessage(String tag, String message)
	{
		Log log = logMap.get(tag);
		if ((log != null) && log.isLoggable(tag, Level.INFO))
		{
			log.info(tag, message);
		}
	}
	
	public void displayMessage(String message)
	{
		System.out.println(message);
	}
	
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
	
	public void populateDatabases() throws InterruptedException
	{
        // Persistence task adds 1 SimpleData entity object to the helloTwoDb1.db database using JPA. 
		performPersistenceWork(PU_NAME1, new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				long millis = System.currentTimeMillis();
				// create some entries in the onCreate
				SimpleData simple1 = new SimpleData(millis);
                entityManager.persist(simple1);
                SimpleData simple2 = new SimpleData(millis + 1);
                entityManager.persist(simple2);
                logMessage(PU_NAME1, "Created new entries in onCreate: " + millis);
			}});
    	// Persistence task adds 1 ComplexData entity object to the helloTwoDb2.db database using JPA.
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
	
	public static void logInfo(String tag, String message) 
	{
		singleton.logMessage(tag, message);
	}
}
