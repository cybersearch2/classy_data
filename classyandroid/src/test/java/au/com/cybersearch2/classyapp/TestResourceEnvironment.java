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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.content.Context;
import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;

/**
 * TestResourceEnvironment
 * @author Andrew Bowley
 * 07/07/2014
 */
public class TestResourceEnvironment implements ResourceEnvironment
{
    Locale locale = new Locale("en", "AU");

    @Override
    public InputStream openResource(String resourceName) throws IOException 
    {
        Context context = (Context)TestRoboApplication.getTestInstance();
        return context.getAssets().open(resourceName);
    }

    @Override
    public Locale getLocale() 
    {
        return locale;
    }

	@Override
	public File getDatabaseDirectory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityClassLoader getEntityClassLoader() {
		// TODO Auto-generated method stub
		return null;
	}

}
