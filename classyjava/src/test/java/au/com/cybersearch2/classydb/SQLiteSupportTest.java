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
package au.com.cybersearch2.classydb;

import org.junit.Test;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * SQLiteSupportTest
 * @author Andrew Bowley
 * 15/06/2014
 */
public class SQLiteSupportTest
{
    private static final String IN_MEMORY_PATH = "jdbc:sqlite::memory:";

    @Test
    public void test_SQLiteConnection_open_close() throws Exception
    {
        ConnectionSource connectionSource = null;
        boolean success = false;
        try
        {
             // Single connection source example for a database URI
             connectionSource = new JdbcConnectionSource(IN_MEMORY_PATH );
        }
        finally
        {
            if (connectionSource != null)
            {
                connectionSource.close();
                success = true;
             }
        }
        assertThat(success).isTrue();
    }
}
