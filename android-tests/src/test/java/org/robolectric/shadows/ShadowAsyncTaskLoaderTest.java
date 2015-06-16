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
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;

@RunWith(TestRunnerWithManifest.class)
public class ShadowAsyncTaskLoaderTest 
{
  private final Transcript transcript = new Transcript();

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
      ShadowApplication.getInstance().getBackgroundThreadScheduler().post(new Runnable() {
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

  @Before
  public void setUp() 
  {
    Robolectric.getForegroundThreadScheduler().pause();
    Robolectric.getBackgroundThreadScheduler().pause();
  }

  @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
  @Test
  public void forceLoad_shouldEnqueueWorkOnSchedulers() {
    new TestLoader(42).forceLoad();
    transcript.assertNoEventsSoFar();

    Robolectric.flushBackgroundThreadScheduler();
    transcript.assertEventsSoFar("loadInBackground");

    Robolectric.flushForegroundThreadScheduler();
    transcript.assertEventsSoFar("deliverResult 42");
  }

  public class TestLoader extends AsyncTaskLoader<Integer> {
    private final Integer data;

    public TestLoader(Integer data) {
      super(RuntimeEnvironment.application);
      this.data = data;
    }

    @Override
    public Integer loadInBackground() {
      transcript.add("loadInBackground");
      return data;
    }

    @Override
    public void deliverResult(Integer data) {
      transcript.add("deliverResult " + data.toString());
    }
  }
}
