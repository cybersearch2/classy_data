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
// Adapted from com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
// Original copyright license:
/*
Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
granted, provided that this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
USE OR PERFORMANCE OF THIS SOFTWARE.

The author may be contacted via http://ormlite.com/ 
*/
package au.com.cybersearch2.classydb;

import java.sql.SQLException;

import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.AndroidDatabaseConnection;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * OpenHelperConnectionSource
 * Sub class of AndroidConnectionSource which exposes the internal SOLiteOpenHelper object 
 * @author kevingalligan, graywatson
 * @author Andrew Bowley
 * 22/06/2014
 */
public class OpenHelperConnectionSource extends AndroidConnectionSource
{
    /** Support for optional android.os.CancellationSignal beginning with Jelly Bean onwards */
    protected boolean cancelQueriesEnabled;
    /** Flag to remember close() called */
    private volatile boolean isOpen = true;
    /** Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods */
    protected OpenHelperCallbacks openHelperCallbacks;
    /** The open helper object internal to super class and otherwise inaccessible */
    protected SQLiteOpenHelper sqLiteOpenHelper;
    /** The SQLiteDatabase db in the onOpen() callback */
    protected SQLiteDatabase database;

    /**
     * Create OpenHelperConnectionSource object
     * @param sqLiteOpenHelper The open helper object internal to super class and otherwise inaccessible
     * @param openHelperCallbacks Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods
     */
    public OpenHelperConnectionSource(SQLiteOpenHelper sqLiteOpenHelper, OpenHelperCallbacks openHelperCallbacks)
    {
        super(sqLiteOpenHelper);
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        this.openHelperCallbacks = openHelperCallbacks;
    }

    /**
     * Satisfies the {@link SQLiteOpenHelper#onCreate(SQLiteDatabase)} interface method.
     */
    public final void onCreate(final SQLiteDatabase db) 
    {
        /*
         * The method is called by Android database helper's get-database calls when Android detects that we need to
         * create or update the database. So we have to use the database argument and save a connection to it on the
         * AndroidConnectionSource, otherwise it will go recursive if the subclass calls getConnectionSource().
         */
        wrappedDatabaseOperation(db, new Runnable(){

            @Override
            public void run() {
                openHelperCallbacks.onCreate(db, OpenHelperConnectionSource.this);
            }});
    }

    /**
     * Satisfies the {@link SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)} interface method.
     */
    public final void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) 
    {
        /*
         * The method is called by Android database helper's get-database calls when Android detects that we need to
         * create or update the database. So we have to use the database argument and save a connection to it on the
         * AndroidConnectionSource, otherwise it will go recursive if the subclass calls getConnectionSource().
         */
        wrappedDatabaseOperation(db, new Runnable(){

            @Override
            public void run() {
                openHelperCallbacks.onUpgrade(db, OpenHelperConnectionSource.this, oldVersion, newVersion);
            }});
    }

    /**
     * Returns the SQLiteDatabase db in the onOpen callback
     * @return SQLiteDatabase
     * @throws IllegalStateException if connection is closed
     */
    public SQLiteDatabase getDatabase()
    {
    	if (!isOpen || (database== null))
    		throw new IllegalStateException("getDatabase() called when connection is not open");
    	return database;
    }

    /**
     * Set database - to be called by OpenHelperCallbacks implementation when onOpen() is called.
     * @param database SQLiteDatabase
     */
	protected void setDatabase(SQLiteDatabase database) 
	{
		this.database = database;
	}

    /**
     * Execute a task in the context database create or update. 
     * Saves a database connection to the specified SQLiteDatabase object to avoid recursion on calls to getConnectionSource().
     * @param db SQLiteDatabase undergoing create or update task
     * @param runnable Create or update task
     */
    protected void wrappedDatabaseOperation(SQLiteDatabase db, Runnable runnable)
    {
        DatabaseConnection conn = getSpecialConnection();
        boolean clearSpecial = false;
        if (conn == null) 
        {
            conn = new AndroidDatabaseConnection(db, true, cancelQueriesEnabled);
            try 
            {   // Note that multi-threading not permitted due to use of ThreadLocal variable:
                //  private ThreadLocal<NestedConnection> specialConnection = new ThreadLocal<NestedConnection>();
                saveSpecialConnection(conn);
                clearSpecial = true;
            } 
            catch (SQLException e) 
            {
                throw new IllegalStateException("Could not save special connection", e);
            }
        }
        try 
        {
            runnable.run();
        } 
        finally 
        {
            if (clearSpecial) 
                clearSpecialConnection(conn);
        }
    }
    
    /**
     * Return true if the helper is still open. Once {@link #close()} is called then this will return false.
     */
    public boolean isOpen() 
    {
        return isOpen;
    }

    /**
     * Returns open helper
     * @return SQLiteOpenHelper
     */
    public SQLiteOpenHelper getSQLiteOpenHelper() 
    {
        return sqLiteOpenHelper;
    }

    /**
     * Close connection source
     */
    @Override
    public void close() 
    {
        super.close();
        isOpen = false;
    }

}
