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
package au.com.cybersearch2.classyapp;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.SQLException;

import javax.inject.Singleton;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.xmlpull.v1.XmlPullParserException;

import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;

import android.content.Context;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import dagger.Component;

/**
 * RoboTest
 * @author Andrew Bowley
 * 19/06/2014
 */
@RunWith(RobolectricTestRunner.class)
public class RoboTest
{
    @Singleton
    @Component(modules = TestAndroidModule.class)  
    static interface TestComponent
    {
        PersistenceContext persistenceContext();
    }
 
    @Test
    public void test_Robolectric() throws SQLException, InterruptedException, IOException, XmlPullParserException
    {
        final PersistenceContext persistenceContext = createObjectGraph();
        Runnable connectionTask = new Runnable(){

			@Override
			public void run() {
		        ConnectionSource connectionSource = persistenceContext.getPersistenceAdmin(TestClassyApplication.PU_NAME).getConnectionSource();
		        assertThat(connectionSource.getDatabaseType()).isInstanceOf(SqliteAndroidDatabaseType.class);
		        synchronized(this)
		        {
		        	notifyAll();
		        }
			}};
		Thread background = new Thread(connectionTask);
		background.start();
		synchronized(connectionTask)
		{
			connectionTask.wait();
		}
    }

    /**
     * Set up dependency injection, which creates an ObjectGraph from test configuration object.
     * Override to run with different database and/or platform. 
     * @throws XmlPullParserException 
     * @throws IOException 
     */
    protected PersistenceContext createObjectGraph() throws IOException, XmlPullParserException
    {
        Context context = TestRoboApplication.getTestInstance();
        ResourceEnvironment resourceEnvironment = new TestResourceEnvironment();
        TestAndroidModule testAndroidModule = new TestAndroidModule(context, resourceEnvironment, TestClassyApplication.PU_NAME); 
        TestComponent component = 
                DaggerRoboTest_TestComponent.builder()
                .testAndroidModule(testAndroidModule)
                .build();
        return component.persistenceContext();
    }

}
