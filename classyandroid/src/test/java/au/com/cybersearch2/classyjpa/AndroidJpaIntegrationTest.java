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
package au.com.cybersearch2.classyjpa;

import org.junit.After;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyapp.TestAndroidModule;
import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import dagger.Module;

/**
 * AndroidJpaIntegrationTest
 * @author Andrew Bowley
 * 20/06/2014
 */
@RunWith(RobolectricTestRunner.class)
public class AndroidJpaIntegrationTest extends JpaIntegrationTest
{
    @Module(injects = { AndroidJpaIntegrationTest.class }, includes = TestAndroidModule.class)
    static class AndroidJpaIntegrationTestModule implements ApplicationModule
    {
    }
    
	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
	 * Override to run with different database and/or platform. 
	 */
    @Override
	protected void createObjectGraph()
	{
	    Context context = TestRoboApplication.getTestInstance();
	    new DI(new AndroidJpaIntegrationTestModule(), new ContextModule(context));
	    DI.inject(this);
	}

    @Override @After
    public void shutdown()
    {
        testPersistenceFactory.onShutdown();
    }
    
}
