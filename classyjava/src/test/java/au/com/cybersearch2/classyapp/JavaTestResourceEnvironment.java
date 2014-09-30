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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * JavaTestResourceEnvironment
 * @author Andrew Bowley
 * 05/08/2014
 */
public class JavaTestResourceEnvironment implements ResourceEnvironment
{
    public static final String DEFAULT_RESOURCE_LOCATION = "src/test/resources";
    
    Locale locale = new Locale("en", "AU");
    final String resourceLocation;

    public JavaTestResourceEnvironment()
    {
        resourceLocation = DEFAULT_RESOURCE_LOCATION;
    }
    
    public JavaTestResourceEnvironment(String resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }
    
    @Override
    public InputStream openResource(String resourceName) throws IOException 
    {
        File resourceFile = new File(resourceLocation, resourceName);
        if (!resourceFile.exists())
            throw new FileNotFoundException(resourceName);
        InputStream instream = new FileInputStream(resourceFile);
        return instream;
    }

    @Override
    public Locale getLocale() 
    {
        return locale;
    }
}
