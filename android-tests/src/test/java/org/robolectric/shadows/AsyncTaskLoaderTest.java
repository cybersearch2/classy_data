package org.robolectric.shadows;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.SimpleFuture;
import org.robolectric.util.Transcript;
import org.robolectric.util.TestRunnerWithManifest;

import android.content.Context;
import android.os.Looper;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunnerWithManifest.class)
public class AsyncTaskLoaderTest 
{
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
		          ShadowLooper.getUiThreadScheduler().post(new Runnable() {
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
		    ShadowApplication.getInstance().getBackgroundScheduler().post(new Runnable() {
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
	
	private Transcript transcript;

    @Before public void setUp() 
    {
	    transcript = new Transcript();
	    ShadowLooper.getUiThreadScheduler().pause();
	    ShadowApplication.getInstance().getBackgroundScheduler().pause();
    }

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
    @Test 
    public void forceLoad_shouldEnqueueWorkOnSchedulers() 
    {
    	TestLoader testLoader = new TestLoader();
    	testLoader.forceLoad();
	    transcript.assertNoEventsSoFar();
	    assertThat(ShadowApplication.getInstance().getBackgroundScheduler().runOneTask()).isTrue();
	    transcript.assertEventsSoFar("loadInBackground");
	
	    ShadowLooper.getUiThreadScheduler().runOneTask();
	    transcript.assertEventsSoFar("deliverResult");
    }

    public class TestLoader extends AsyncTaskLoader<Void> 
    {
	    public TestLoader() 
	    {
	        super(RuntimeEnvironment.application);
        }

    @Override
    public Void loadInBackground() 
    {
        transcript.add("loadInBackground");
        return null;
    }

    @Override
    public void deliverResult(Void data) 
    {
        transcript.add("deliverResult");
    }
  }
}
