package au.com.cybersearch2.example.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.inject.Singleton;

import au.com.cybersearch2.classydb.DatabaseAdminImpl;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWork;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.example.QueryForAllGenerator;
import dagger.Component;

/**
 * Version 2 of HelloTwoDbsMain introduces a new "quote" field to both SimpleData and ComplexData entities.
 * CLASSYTOOLS COMMENTS:
 */
public class HelloTwoDbsMain 
{
    @Singleton
    @Component(modules = HelloTwoDbsModule.class)  
    static interface ApplicationComponent extends ApplicationModule
    {
        void inject(HelloTwoDbsMain helloTwoDbsMain);
        void inject(PersistenceContext persistenceContext);
        void inject(PersistenceFactory persistenceFactory);
        void inject(DatabaseAdminImpl databaseAdminImpl);
        void inject(NativeScriptDatabaseWork nativeScriptDatabaseWork);
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
	protected static boolean applicationInitialized;

    private static HelloTwoDbsMain singleton;
    
    /** Factory object to create "simple" and "complex" Persistence Unit implementations */
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
     * This creates and populates the database using JPA, provides verification logic and runs a test from main().
     */
    public HelloTwoDbsMain() 
    {
    	singleton = this;
    }

    /**
	 * @return the persistenceContext
	 */
	public PersistenceContext getPersistenceContext() 
	{
		return persistenceContext;
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
			applicationInitialized = true;
			initializeDatabase();
    	}
    }

    /**
     * Initialize entity tables ensuring version is correct and contains initial data  
     * for integrated test. 
     * @throws InterruptedException
     */
    public void setUpNoDI() throws InterruptedException
    {
		applicationInitialized = true;
		initializeDatabase();
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
        createObjectGraph();
    }
    
    protected void initializeDatabase()
    {
        persistenceContext = new PersistenceContext();
    	// Initialize all databases. This handles create and update events automatically.
    	persistenceContext.initializeAllDatabases();
    	System.out.println("initializeAllDatabases() called");
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
    	launchPersistenceWork(puName, persistenceTask).waitForTask();
    }
    
	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
	 * Override to run with different database and/or platform. 
	 */
	protected void createObjectGraph()
	{
        ApplicationComponent component = 
                DaggerHelloTwoDbsMain_ApplicationComponent.builder()
                .helloTwoDbsModule(new HelloTwoDbsModule())
                .build();
        // Set up dependency injection, which creates an ObjectGraph from a ManyToManyModule configuration object
        DI.getInstance(component).validate();
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
		singleton.logMessage(tag, message);
	}
}
