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
package au.com.cybersearch2.classynode;

/**
 * NodeType
 * A Node is the anchor for a component of a graph. 
 * Each node has a model which identifies what the node contains.
 * The top node of a graph has a special model called "root".
 * @author Andrew Bowley
 * 05/09/2014
 */
public interface NodeType<T extends Enum<T>>
{
    static final int ROOT = 0;
    static final String ROOT_NAME = "root";
    
    /**
     * Returns the root mode, which has name defined as ROOT static constant 
     * @return The an enum constant
     */
    T root(); 
    
    /**
     * Returns the model with the specified name
     *
     * @param name The name of the constant value to find.
     * @return An enum constant
     * @throws IllegalArgumentException
     *             if {@code name} is {@code null} or does not
     *             have a constant value called {@code name}
     */
    T valueOf(final String name);
    
    /**
     * Returns the model with the specified ordinal value
     *
     * @param ordinal The ordinal of the constant value to find.
     * @return An enum constant
     * @throws IllegalArgumentException
     *             if {@code name} is {@code null} or does not
     *             have a constant value called {@code name}
     */
    T valueOf(final int ordinal);

}
