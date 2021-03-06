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

import au.com.cybersearch2.classyfy.data.Model;
import au.com.cybersearch2.classynode.Node;

/**
 * TestClassyApplication
 * @author Andrew Bowley
 * 13/06/2014
 */
public class TestClassyApplication
{
    public static final String PU_NAME = "classyfy";
    public static final String DATABASE_NAME = "classyfy.db";
    public static final String CATEGORY_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + Model.recordCategory.ordinal();
    public static final String FOLDER_BY_NODE_ID = Node.NODE_BY_PRIMARY_KEY_QUERY + Model.recordFolder.ordinal();
}
