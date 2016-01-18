package au.com.cybersearch2.example.v2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.inject.Singleton;

import au.com.cybersearch2.classydb.DatabaseAdmin;
import au.com.cybersearch2.classydb.DatabaseSupport.ConnectionType;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.example.QueryForAllGenerator;
import dagger.Component;
import dagger.Subcomponent;

/**
 * Version 2 of HelloTwoDbsMain introduces a new "quote" field to both SimpleData and ComplexData entities.
 * CLASSYTOOLS COMMENTS:
 */
public class HelloTwoDbsMain 
{
    @Singleton
    @Component(modules = HelloTwoDbsModule.class)
    public  
    static interface ApplicationComponent extends ApplicationModule
    {
        PersistenceFactory persistenceFactory();
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
    static public final String ALL_SIMPLE_DATA2 = "all_simple_data2";
    /** Named query to find all ComplexData objects */
    static public final String ALL_COMPLEX_DATA2 = "all_complex_data2";
   
    private final static Map<String, Log> logMap;
	public static final Object SEPARATOR_LINE = "------------------------------------------\n"; 
	protected boolean applicationInitialized;

    /** Factory object to create "simple" and "complex" Persistence Unit implementations */
    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    protected PersistenceContext persistenceContext;
    
    static
    {
    	logMap = new HashMap<String, Log>(2); 
    	logMap.put(TAG, JavaLogger.getLogger(TAG));
    	logMap.put(PU_NAME1, JavaLogger.getLogger(PU_NAME1));
    	logMap.put(PU_NAME2, JavaLogger.getLogger(PU_NAME2));
    }
    
    /**
	 * @return the persistenceContext
	 */
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
    /**
     * Initialize entity tables ensuring version is correct and contains initial data. 
     * Call this before doing any queries. 
     * Note the calling thread is suspended while the work is performed on a background thread. 
     * @throws InterruptedException
     */
    public void setUp() throws InterruptedException
    {
        System.out.println("applicationInitialized = " + applicationInitialized);
    	if (!applicationInitialized)
    	{
    		initializeApplication();
			applicationInitialized = true;
			initializeDatabase();
    	}
    }

    public void shutdown()
    {
    	if (persistenceContext == null)
    		return;
        String[] puNames = { PU_NAME1, PU_NAME2 }; 
        {
        	for (String puName: puNames)
        	{
        		Persistence pu = persistenceContext.getPersistenceUnit(puName);
        		if (pu != null)
        			pu.getPersistenceAdmin().close();
        	}
        }
    }

    protected void initializeApplication()
    {
        // Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object
        persistenceContext = createObjectGraph();
    }
    
    protected void initializeDatabase()
    {
    	// Initialize all databases. This handles create and update events automatically.
    	persistenceContext.initializeAllDatabases();
    	//System.out.println("initializeAllDatabases() called");
        // To populate these tables, call setUp().
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin1 = persistenceContext.getPersistenceAdmin(PU_NAME1);
        QueryForAllGenerator allSimpleDataObjects = 
                new QueryForAllGenerator(persistenceAdmin1);
        persistenceAdmin1.addNamedQuery(SimpleData.class, ALL_SIMPLE_DATA2, allSimpleDataObjects);
        // Get Interface for JPA Support, required to create named queries
        PersistenceAdmin persistenceAdmin2 = persistenceContext.getPersistenceAdmin(PU_NAME2);
        QueryForAllGenerator allComplexDataObjects = 
                new QueryForAllGenerator(persistenceAdmin2);
        persistenceAdmin2.addNamedQuery(ComplexData.class, ALL_COMPLEX_DATA2, allComplexDataObjects);
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
     * @param puName Persistence Unit name
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
        component = 
                DaggerHelloTwoDbsMain_ApplicationComponent.builder()
                .helloTwoDbsModule(new HelloTwoDbsModule())
                .build();
        return component.persistenceContext();
	}

    protected Executable getExecutable(String puName, PersistenceWork persistenceWork)
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
