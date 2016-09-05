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

import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;

import android.database.sqlite.SQLiteDatabase;

/**
 * OpenEventHandler - SQLiteOpenHelper for AndroidConnectionSource
 * The onCreate() and onUpgrade() methods are provided by an OpenHelperCallbacks implementation.
 * A custom OpenHelperCallbacks implementation is specified by perisistence.xml property 
 * "open-helper-callbacks-classname". If not specified, the default OpenHelperCallbacksImpl
 * class is used which calls DatabaseAdmin onCreate() and onUpgrade() methods. 
 * @author Andrew Bowley
 * 26 Nov 2014
 * @see android.database.sqlite.SQLiteOpenHelper
 * @see com.j256.ormlite.android.AndroidConnectionSource
 */
public class OpenEventHandler extends OrmLiteSqliteOpenHelper
{
    /** Implementation of onCreate() and onUpdate() SQLiteOpenHelper abstract methods */
    protected OpenHelperCallbacks openHelperCallbacks;
    /** Support for optional android.os.CancellationSignal beginning with Jelly Bean onwards */
    protected boolean cancelQueriesEnabled;

    /**
     * Create OpenEventHandler object
     * @param androidSqliteParams AndroidSqliteParams adapter
     */
	public OpenEventHandler(AndroidSqliteParams androidSqliteParams)
	{
		super(androidSqliteParams.getContext(),
		       androidSqliteParams.getName(),
		       androidSqliteParams.getFactory(),
		       androidSqliteParams.getVersion());
        this.openHelperCallbacks = androidSqliteParams.getOpenHelperCallbacks();
	}

    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param database
     *            Database being created.
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource)
    {
        openHelperCallbacks.onCreate(connectionSource);
    }

    /**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param database
     *            Database being upgraded.
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion,
            int newVersion)
    {
        // TODO: Why use new connection source rather than follow Gary's advice?
        openHelperCallbacks.onUpgrade(new AndroidConnectionSource(database), oldVersion, newVersion);
    }
    
    /**
     * Called when the database needs to be downgraded. This is strictly similar to
     * {@link #onUpgrade} method, but is called whenever current version is newer than requested one.
     * Supported to allow database version change during testing. A script is required to support the
     * downgrade, so still under customer control as intended by design.
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
	@Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) 
    {
    	onUpgrade(db, oldVersion, newVersion);
    }


    /**
     * Returns flag for support of optional android.os.CancellationSignal
     * @return boolean
     */
	public boolean isCancelQueriesEnabled() 
	{
		return cancelQueriesEnabled;
	}

    /**
     * Sets flag for support of optional android.os.CancellationSignal
     * @param cancelQueriesEnabled boolean
     */
	public void setCancelQueriesEnabled(boolean cancelQueriesEnabled) 
	{
		this.cancelQueriesEnabled = cancelQueriesEnabled;
	}
    
}
