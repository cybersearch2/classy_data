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
package au.com.cybersearch2.example;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import au.com.cybersearch2.classyapp.TestRoboApplication;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.WorkStatus;

/**
 * AndroidManyToManyTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class AndroidManyToMany extends ManyToManyMain
{
    private AndroidManyToManyFactory androidManyToManyFactory;
    
    @Override
    protected PersistenceContext createFactory()
    {
        try
        {
            androidManyToManyFactory = new AndroidManyToManyFactory((Context)TestRoboApplication.getTestInstance());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        return androidManyToManyFactory.getPersistenceContext();
    }
    
    @Override
    protected WorkStatus execute(PersistenceWork persistenceWork) throws InterruptedException
    {
        return androidManyToManyFactory.getExecutable(persistenceWork).waitForTask();
    }
}
