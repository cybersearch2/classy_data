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

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;
import au.com.cybersearch2.classyapp.ContextModule;
import au.com.cybersearch2.classyapp.TestAndroidModule;
import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceFactory;
import au.com.cybersearch2.classyjpa.persist.TestPersistenceFactory;
import au.com.cybersearch2.classyutil.Transcript;
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
    
    @Inject PersistenceFactory persistenceFactory;

    @Override @Before
    public void setup() throws Exception
    {
        if (testPersistenceFactory == null)
        {
            Context context = TestRoboApplication.getTestInstance();
            new DI(new AndroidJpaIntegrationTestModule(), new ContextModule(context));
            DI.inject(this);
            Persistence persistence = persistenceFactory.getPersistenceUnit(TestClassyApplication.PU_NAME);
            testPersistenceFactory = new TestPersistenceFactory(persistence);
            testPersistenceFactory.setUpDatabase();
        }
        transcript = new Transcript();
        testContainer = new PersistenceContainer(TestClassyApplication.PU_NAME);
    }

    @Override @After
    public void shutdown()
    {
        testPersistenceFactory.onShutdown();
    }
    
}
