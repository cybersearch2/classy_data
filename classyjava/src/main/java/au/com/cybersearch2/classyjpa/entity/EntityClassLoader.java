/**
    Copyright (C) 2018  www.cybersearch2.com.au

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
package au.com.cybersearch2.classyjpa.entity;

/**
 * Instantiates entity classes
 * @author Andrew Bowley
 * 05/09/2018
 */
public interface EntityClassLoader {
	/**
	 * @param name The name of the class to load.
	 * @return The Class object for the requested class.
	 * @throws ClassNotFoundException If no such class can be found 
	 */
	Class<?> loadClass(String name) throws ClassNotFoundException;

}
