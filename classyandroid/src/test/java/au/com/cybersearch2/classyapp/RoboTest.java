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

import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.content.Context;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;

import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;

import dagger.Module;

/**
 * RoboTest
 * @author Andrew Bowley
 * 19/06/2014
 */
@RunWith(RobolectricTestRunner.class)
public class RoboTest
{
    @Module(injects = { RoboTest.class }, includes = TestAndroidModule.class)
    static class RoboTestModule implements ApplicationModule
    {
    }
    
    @Test
    public void test_Robolectric() throws SQLException
    {
        Context context = TestRoboApplication.getTestInstance();
        new DI(new RoboTestModule(), new ContextModule(context));
        PersistenceContext persistenceContext = new PersistenceContext();
        ConnectionSource connectionSource = persistenceContext.getPersistenceAdmin(TestClassyApplication.PU_NAME).getConnectionSource();
        assertThat(connectionSource.getDatabaseType()).isInstanceOf(SqliteAndroidDatabaseType.class);
        //DatabaseConnection connection = connectionSource.getReadWriteConnection();
        //assertThat(connection.isAutoCommit()).isTrue();
        //classyOpenHelper.close();
        //assertThat(classyOpenHelper.isOpen()).isFalse();
    }

}
