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

import java.util.Locale;

import javax.inject.Inject;

import au.com.cybersearch2.classyinject.DI;

/**
 * ApplicationLocale
 * Provides access to Application Locale by dependency injection
 * @author Andrew Bowley
 * 18/07/2014
 */
public class ApplicationLocale
{
    /** System resource adapter, which includes application locale. Inject into a singleton rather than everywhere this ubiquitous object is required. */
    @Inject ResourceEnvironment resourceEnvironment;
    
    /** 
     * Create ApplicationLocale object
     */
    public ApplicationLocale()
    {
        DI.inject(this);
    }

    /**
     * Returns application locale
     * @return Locale
     */
    public Locale getLocale()
    {
        return resourceEnvironment.getLocale();
    }
}
