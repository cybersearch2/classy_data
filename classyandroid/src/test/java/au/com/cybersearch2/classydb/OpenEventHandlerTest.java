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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.j256.ormlite.support.ConnectionSource;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * TODO - test default callbacks dependency injection
 * OpenEventHandlerTest
 * @author Andrew Bowley
 * 26 Nov 2014
 */
@RunWith(RobolectricTestRunner.class)
public class OpenEventHandlerTest 
{
    static final String PU_NAME = "classyfy";
    static final String DATABASE_NAME = "classyfy.db";
    Properties properties;
    Context context;
    OpenHelperCallbacks openHelperCallbacks;

    @Before
    public void setUp()
    {
    	context = mock(Context.class);
    	openHelperCallbacks = mock(OpenHelperCallbacks.class);
    }
    
    @Test
    public void test_create_OpenEventHandler()
    {
        AndroidSqliteParams androidSqliteParams = mock(AndroidSqliteParams.class);
        when(androidSqliteParams.getContext()).thenReturn(context);
        when(androidSqliteParams.getName()).thenReturn(DATABASE_NAME);
        when(androidSqliteParams.getVersion()).thenReturn(1);
        when(androidSqliteParams.getOpenHelperCallbacks()).thenReturn(openHelperCallbacks);
    	OpenEventHandler openEventHandler = new OpenEventHandler(androidSqliteParams);
        assertThat(openEventHandler.openHelperCallbacks).isNotNull();
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        openEventHandler.onCreate(db);
        verify(openHelperCallbacks).onCreate(isA(ConnectionSource.class));
        openEventHandler.onUpgrade(db, 1, 2);
        verify(openHelperCallbacks).onUpgrade(isA(ConnectionSource.class), eq(1), eq(2));
        openEventHandler.onDowngrade(db, 2, 1);
        verify(openHelperCallbacks).onUpgrade(isA(ConnectionSource.class), eq(2), eq(1));
    }
}
