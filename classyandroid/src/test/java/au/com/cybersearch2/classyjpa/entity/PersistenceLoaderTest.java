/**
 * 
 */
package au.com.cybersearch2.classyjpa.entity;

import java.util.concurrent.Callable;

import javax.persistence.EntityExistsException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.SimpleFuture;
import org.robolectric.util.TestRunnerWithManifest;
import org.robolectric.util.Transcript;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import dagger.Module;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyapp.TestAndroidModule;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.transaction.EntityTransactionImpl;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * @author andrew
 *
 */
@RunWith(TestRunnerWithManifest.class)
public class PersistenceLoaderTest 
{
    @Module(includes = TestAndroidModule.class)
    static class PersistenceLoaderTestModule implements ApplicationModule
    {
    }

	@Implements(value = SystemClock.class, callThroughByDefault = true)
    public static class MyShadowSystemClock {
        public static long elapsedRealtime() {
            return 0;
        }
    }

	@Implements(AsyncTaskLoader.class)
	public static class MyShadowAsyncTaskLoader<D> 
	{
		  @RealObject private AsyncTaskLoader<D> realLoader;
		  private SimpleFuture<D> future;

		  public void __constructor__(Context context) {
		    BackgroundWorker worker = new BackgroundWorker();
		    future = new SimpleFuture<D>(worker) {
		      @Override protected void done() {
		        try {
		          final D result = get();
		          Robolectric.getForegroundThreadScheduler().post(new Runnable() {
		            @Override public void run() {
		              realLoader.deliverResult(result);
		            }
		          });
		        } catch (InterruptedException e) {
		          // Ignore
		        }
		      }
		    };
		  }

		  @Implementation
		  public void onForceLoad() {
		      Robolectric.getBackgroundThreadScheduler().post(new Runnable() {
		      @Override
		      public void run() {
		        future.run();
		      }
		    });
		  }

		  private final class BackgroundWorker implements Callable<D> {
		    @Override public D call() throws Exception {
		      return realLoader.loadInBackground();
		    }
		  }
	}
	
    protected PersistenceLoader testLoaderTask;
    protected PersistenceLoader testUserTransLoaderTask;
    protected PersistenceContext persistenceContext;

    @Before
    public void setup() throws Exception
    {
    	Context context = createObjectGraph();
	    testLoaderTask = new PersistenceLoader(context);
	    testUserTransLoaderTask = new PersistenceLoader(context);
	    testUserTransLoaderTask.setUserTransactionMode(true);
        persistenceContext = new PersistenceContext();
    	initializeDatabase();
    	Robolectric.getForegroundThreadScheduler().pause();
        Robolectric.getBackgroundThreadScheduler().pause();
    }

    @After
    public void shutdown()
    {
    	closeDatabase();
    }

    protected void initializeDatabase()
    {
    	persistenceContext.initializeAllDatabases();
    }

    protected void closeDatabase()
    {
    }



	/**
	 * Set up dependency injection, which creates an ObjectGraph from test configuration object.
	 * Override to run with different database and/or platform. 
	 */
	protected Context createObjectGraph()
	{
	    Context context = RuntimeEnvironment.application;
	    new DI(new PersistenceLoaderTestModule(), new ContextModule(context));
	    return context;
	}

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
	@Test
    public void do_background_called() throws Throwable
    {
		Transcript transcript = new Transcript();

        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        Executable exe = testLoaderTask.execute("classyfy", persistenceWork);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("background task");
        Robolectric.getForegroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("onPostExecute true");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }
    
    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
	@Test
    public void do_rollback_only() throws Throwable
    {
		Transcript transcript = new Transcript();
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        // Return false to cause transaction setRollbackOnly() to be called
                        return false;
                    }});
        Executable exe = testLoaderTask.execute("classyfy", persistenceWork);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("background task");
        Robolectric.getForegroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("onPostExecute false");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
	@Test
    public void do_exception_thrown() throws Throwable
    {   
		Transcript transcript = new Transcript();
        final EntityExistsException persistException = new EntityExistsException("Entity of class RecordCategory, primary key 1 already exists");
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        throw persistException;
                    }});
        Executable exe = testLoaderTask.execute("classyfy", persistenceWork);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("background task");
        Robolectric.getForegroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("onRollback " + persistException.toString());
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FAILED);
    }

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
	@Test
    public void do_npe_thrown() throws Throwable
    {
		Transcript transcript = new Transcript();
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @SuppressWarnings("null")
                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        Object object = null;
                        object.toString();
                        return true;
                    }});
        testLoaderTask.execute("classyfy", persistenceWork);
        try
        {
            Robolectric.getBackgroundThreadScheduler().runOneTask();
        	failBecauseExceptionWasNotThrown(RuntimeException.class);
        }
        catch (RuntimeException e)
        {
        	assertThat(e.getCause()).isNotNull();
        	assertThat(e.getCause()).isInstanceOf(NullPointerException.class);
        }
    }

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
	@Test
    public void do_user_transaction() throws Throwable
    {
		Transcript transcript = new Transcript();
        final PersistenceWork persistenceWork = new TestPersistenceWork(transcript,
                new TestPersistenceWork.Callable(){

                    @Override
                    public Boolean call(EntityManagerLite entityManager)
                            throws Exception 
                    {
                        // Return false to cause transaction setRollbackOnly() to be called
                        // User Transactions get access to actual transaction
                        return entityManager.getTransaction() instanceof EntityTransactionImpl;
                    }});
        Executable exe = testUserTransLoaderTask.execute("classyfy", persistenceWork);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("background task");
        Robolectric.getForegroundThreadScheduler().runOneTask();
        transcript.assertEventsSoFar("onPostExecute true");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }
}
