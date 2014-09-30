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

import java.lang.reflect.Method;

import org.robolectric.Robolectric;
import org.robolectric.TestLifecycleApplication;

import android.app.Application;

/**
 * TestClassyFyApplication
 * @author Andrew Bowley
 * 14/04/2014
 */
public class TestRoboApplication extends Application implements TestLifecycleApplication
{
    public static final String TAG = "TestClassyFyApplication";
    private static TestRoboApplication singleton;
 
    public TestRoboApplication()
    {
        singleton = this;
        Robolectric.application = singleton;

    }

    @Override 
    public void onCreate() 
    {
        super.onCreate();
    }

    @Override
    public void beforeTest(Method method) 
    {
    }

    @Override
    public void prepareTest(Object test) 
    {
    }

    @Override
    public void afterTest(Method method) 
    {
    }

    public static TestRoboApplication getTestInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("TestClassyApplication called while not initialized");
        return singleton;
    }
    
    public static void init()
    {
    }
}
