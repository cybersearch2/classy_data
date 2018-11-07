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

import au.com.cybersearch2.classyjpa.entity.EntityClassLoader;

/**
 * ResourceEnvironment
 * Adapts access to resources according to platform and locale
 * @author Andrew Bowley
 * 16/06/2014
 */
public interface ResourceEnvironment
{
    /**
     * Provides read access to a resource stream such as a file.
     * @param resourceName
     * @throws IOException
     */
    InputStream openResource(String resourceName) throws IOException;
    /**
     * Get locale. 
     * Android lint complains if Locale is omitted where it can be specified as an optional parameter.
     */
    Locale getLocale();
  
    /**
     * Returns database location when ConnectionType = "file"
     * @return File object for a directory location
     */
    File getDatabaseDirectory();

    /**
     * Returns Class Loader for instantiating entity classes
     * @return EntityClassLoader object or null if not provided
     */
    EntityClassLoader getEntityClassLoader();
}
