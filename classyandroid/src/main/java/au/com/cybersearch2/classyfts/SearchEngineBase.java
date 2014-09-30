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
package au.com.cybersearch2.classyfts;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import au.com.cybersearch2.classyapp.ApplicationContext;
import au.com.cybersearch2.classyapp.ApplicationLocale;
import au.com.cybersearch2.classyfts.FtsEngine;
import au.com.cybersearch2.classyfts.FtsQuery;
import au.com.cybersearch2.classyfts.FtsSearch;
import au.com.cybersearch2.classyfts.FtsEngine.FtsStatus;
import au.com.cybersearch2.classytask.UserTask;

/**
 * SearchEngineBase
 * Abstract class implementing a search suggestion dictionary. Sub-class to override onCreate() and make a concrete class.
 * @author Andrew Bowley
 * 12/07/2014
 */
public abstract class SearchEngineBase
{
    public static final String TAG = "SearchEngine";
    /** Keyword in search URI indicates use FTS for search suggestions */
    public static final String LEX = "lex";
    
    /** UriMatcher ID for Slow text search - for backup only */
    protected static final int SEARCH_SUGGEST = 1;
    /** UriMatcher ID for when using {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table */
    protected static final int REFRESH_SHORTCUT = 2;
    /** UriMatcher ID for Fast text search */
    protected static final int LEXICAL_SEARCH_SUGGEST = 3;
    /** UriMatcher ID for when using {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our dictionary table */
    protected static final int LEXICAL_REFRESH_SHORTCUT = 4;
    /** UriMatcher ID for content provider query */
    protected static final int PROVIDER_TYPE = 5;
    
    /** The fast text search query engine */
    protected FtsQuery ftsQuery;
    /** Flag to indicate FTS is supported and available */
    protected boolean isFtsAvailable;
    /** Android Application Context */
    protected ApplicationContext applicationContext;
    /** Android Application Locale */
    protected ApplicationLocale  applicationLocale;

    /**
     * Create SearchEngineBase object
     */
    public SearchEngineBase()
    {
        applicationContext = new ApplicationContext();
        applicationLocale = new ApplicationLocale();
        // Install FtsQuery place holder until flag isFtsAvailable is set true
        ftsQuery = new FtsQuery(){

            @Override
            public Cursor query(String selection, String[] argselectionArgs, String[] columns, int limit) {
                throw new IllegalStateException("FTS query invoked while FTS Engine not available");
            }};
    }

    /**
     * Handle dictionary creation event
     * @param sqLiteOpenHelper The open helper for the source database which is to be accessed using FTS
     */
    public abstract void onCreate(SQLiteOpenHelper sqLiteOpenHelper);

    /**
     * Returns MIME type for suggestions data  
     * @param queryType UriMatcher value
     * @param uri URI matched by UriMatcher
     * @return "vnd.android.cursor.dir/vnd.android.search.suggest"
     * @throws IllegalArgumentException if UriMatcher value does not match one for a suggestion type URI
     */
    public String getType(int queryType, Uri uri)
    {
        switch (queryType)
        {
        // Search Suggestions support
        case SEARCH_SUGGEST: 
        case LEXICAL_SEARCH_SUGGEST:
            return SearchManager.SUGGEST_MIME_TYPE;
        case REFRESH_SHORTCUT: 
        case LEXICAL_REFRESH_SHORTCUT: 
            return SearchManager.SUGGEST_MIME_TYPE;
        default: throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
    
    /**
     * Perform a search suggestion query using FTS if supported and available
     * @param qb Populated QueryBuilder object
     * @param sqLiteOpenHelper Open helper for dictionary database
     * @return A cursor over the result set
     */
    protected Cursor query(FtsQueryBuilder qb, SQLiteOpenHelper sqLiteOpenHelper)
    {
        if ((qb.queryType == LEXICAL_SEARCH_SUGGEST) && isFtsAvailable)
        {    
            Cursor cursor = doLexicalSearch(qb.searchTerm, qb.uri);
            if (cursor != null)
                return cursor;
            // Fall back to slow text search
        }
        if ((qb.queryType == LEXICAL_SEARCH_SUGGEST) || 
            (qb.queryType == SEARCH_SUGGEST))          
        {
            // Set passed selection and selectionArgs parameters to null to match search by Uri
            qb.selection = null;
            qb.selectionArgs = null;
        }
        Cursor cursor = qb.doQuery(sqLiteOpenHelper.getWritableDatabase());
        if (cursor != null)
            cursor.setNotificationUri(applicationContext.getContentResolver(), qb.uri);
        return cursor;
    }

    /**
     * Notify registered observers that a row was updated and attempt to sync changes
     * to the network.
     * @param uri The uri of the content that was changed.
     */
    protected void notifyChange(Uri uri)
    {
        applicationContext.getContentResolver().notifyChange(uri, null);
    }

    protected Cursor doLexicalSearch(String searchTerm, Uri uri) 
    {
        Cursor cursor = getSuggestions(searchTerm, getLimitFromUri(uri));
        if (cursor != null)
            // Register the context ContentResolver to be notified if the cursor result set changes
            cursor.setNotificationUri(applicationContext.getContentResolver(), uri);
        return cursor;
    }

    protected Cursor getSuggestions(String searchTerm, int limit) 
    {
        searchTerm = searchTerm.toLowerCase(applicationLocale.getLocale());
        String[] columns = new String[] 
        {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
         /* SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                          (only if you want to refresh shortcuts) */
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
        FtsSearch ftsSearch = new FtsSearch(ftsQuery);
        return ftsSearch.getWordMatches(searchTerm, columns, limit);
    }
    
    protected String getSearchSuggestPath(int searchableResourceId)
    {
        return applicationContext.getDocumentAttribute(searchableResourceId, "searchSuggestPath");
    }

    protected void startFtsEngine(final FtsEngine ftsEngine)
    {
        UserTask<Void,FtsStatus> initTask = new UserTask<Void,FtsStatus>()
        {
            @Override
            public FtsStatus doInBackground()
            {    
                return ftsEngine.initialize();
            }
            
            @Override
            public void onPostExecute(FtsStatus status)
            {
                if (status == FtsStatus.Loaded)
                {
                    isFtsAvailable = true;
                    ftsQuery = ftsEngine;
                }
            }
        };
        initTask.execute();
    }

    public static int getLimitFromUri(Uri uri)
    {
        String limitText = uri.getQueryParameter(SearchManager.SUGGEST_PARAMETER_LIMIT);
        if (limitText != null)
        {
            try
            {
                return Integer.parseInt(limitText);
            }
            catch (NumberFormatException e)
            {
                Log.e(TAG, "Invalid \"" + SearchManager.SUGGEST_PARAMETER_LIMIT + "\" value of " + limitText);
            }
        }
        return 0;
    }
}
