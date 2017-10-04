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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.CancellationSignal;
import android.provider.BaseColumns;
import au.com.cybersearch2.classyapp.TestResourceEnvironment;
import au.com.cybersearch2.classyapp.TestRoboApplication;

/**
 * SearchEngineBaseTest
 * @author Andrew Bowley
 * 22 Jun 2015
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 25, application = TestRoboApplication.class)
public class SearchEngineBaseTest
{
    static class TestSearchEngine extends SearchEngineBase
    {
        // Create the constants used to differntiate between the different URI requests
        // Note values 1 - 4 are reserved for 
        // SEARCH_SUGGEST, REFRESH_SHORTCUT, LEXICAL_SEARCH_SUGGEST and LEXICAL_REFRESH_SHORTCUT
        protected static final int ALL_NODES_TYPES = PROVIDER_TYPE;
        protected static final int ALL_NODES_TYPE_ID = ALL_NODES_TYPES + 1;
        public static final String PROVIDER_AUTHORITY = 
                "au.com.cybersearch2.classyfy.ClassyFyProvider";
        
        public TestSearchEngine()
        {
            super(PROVIDER_AUTHORITY, TestRoboApplication.getTestInstance(), new TestResourceEnvironment().getLocale());
        }
        
        @Override
        public String getType(Uri uri)
        {
            return super.getType(createUriMatcher().match(uri), uri);
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                String[] selectionArgs, String sortOrder)
        {
            return null;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
                String[] selectionArgs, String sortOrder,
                CancellationSignal cancellationSignal)
        {
            return null;
        }
        
        @Override
        public Uri insert(Uri uri, ContentValues values)
        {
            return null;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs)
        {
            return 0;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection,
                String[] selectionArgs)
        {
            return 0;
        }
        
        /**
         * Returns container to map Uri to type
         * @return UriMatcher object
         */
        protected UriMatcher createUriMatcher() 
        {
            // Allocate the UriMatcher object where a URI ending is 'all_nodes' will correspond to a request for all nodes, 
            // and 'all_nodes' with a trailing '/[rowID]' will represent a single all_nodes row

            UriMatcher newUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, "all_nodes", ALL_NODES_TYPES);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, "all_nodes/#", ALL_NODES_TYPE_ID);
            // to get suggestions...
            newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY, LEXICAL_SEARCH_SUGGEST);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", LEXICAL_SEARCH_SUGGEST);
            /* The following are unused in this implementation, but if we include
             * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions table, we
             * could expect to receive refresh queries when a shortcutted suggestion is displayed in
             * Quick Search Box, in which case, the following Uris would be provided and we
             * would return a cursor with a single item representing the refreshed suggestion data.
             */
            newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT, LEXICAL_REFRESH_SHORTCUT);
            newUriMatcher.addURI(PROVIDER_AUTHORITY, LEX + "/" + SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", LEXICAL_REFRESH_SHORTCUT);
            return newUriMatcher;
        }

    }

    public static final String PROVIDER_AUTHORITY = "au.com.cybersearch2.classyfy.ClassyFyProvider";
    // The scheme part for this provider's URI
    private static final String SCHEME = "content://";
    private static final String PATH_ALL_NODES = "/all_nodes";
    private static final String PATH_SEGMENT_LEX = "/lex/";
    // Column names
    // Android expects RowIDColumn to be "_id", so do not use any other value.
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MODEL = "model";
 
    @Before
    public void setUp()
    {
    }
    
    @Test 
    public void test_constructor()
    {
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        assertThat(testSearchEngine.context).isNotNull();
        assertThat(testSearchEngine.locale).isNotNull();
        String selection = "";
        String[] selectionArgs = new String[]{};
        String[] columns = new String[]{};
        int limit = 0;
        try
        {
            testSearchEngine.ftsQuery.query(selection, selectionArgs, columns, limit);
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch(IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("FTS query invoked while FTS Engine not available");
        }
        UriMatcher uriMatcher = testSearchEngine.createUriMatcher();
        Uri searchQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*");
        assertThat(uriMatcher.match(searchQueryUri)).isEqualTo(1);
        Uri searchShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        assertThat(uriMatcher.match(searchShortcutUri)).isEqualTo(2);
        searchShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*");
        assertThat(uriMatcher.match(searchShortcutUri)).isEqualTo(2);
        Uri searchLexQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_QUERY);
        assertThat(uriMatcher.match(searchLexQueryUri)).isEqualTo(3);
        searchLexQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX + SearchManager.SUGGEST_URI_PATH_QUERY + "/*");
        assertThat(uriMatcher.match(searchLexQueryUri)).isEqualTo(3);
        Uri searchLexShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        assertThat(uriMatcher.match(searchLexShortcutUri)).isEqualTo(4);
        searchLexShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*");
        assertThat(uriMatcher.match(searchLexShortcutUri)).isEqualTo(4);
        assertThat(uriMatcher.match(Uri.EMPTY)).isEqualTo(UriMatcher.NO_MATCH);
    }
    
    @Test
    public void testGetType() throws Exception
    {
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        Uri searchQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*");
        assertThat(testSearchEngine.getType(searchQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri searchShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        assertThat(testSearchEngine.getType(searchShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" +SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*");
        assertThat(testSearchEngine.getType(searchShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri searchLexQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_QUERY);
        assertThat(testSearchEngine.getType(searchLexQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchLexQueryUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX + SearchManager.SUGGEST_URI_PATH_QUERY + "/*");
        assertThat(testSearchEngine.getType(searchLexQueryUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri searchLexShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_SHORTCUT);
        assertThat(testSearchEngine.getType(searchLexShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        searchLexShortcutUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_SEGMENT_LEX +SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*");
        assertThat(testSearchEngine.getType(searchLexShortcutUri)).isEqualTo(SearchManager.SUGGEST_MIME_TYPE);
        Uri invalidUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES);
        try
        {
            testSearchEngine.getType(invalidUri);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage().contains(invalidUri.toString())).isTrue();
        }
    }
   
    @Test
    public void test_FTS_Query() throws Exception
    {
        FtsQueryBuilder qb = mock(FtsQueryBuilder.class);
        SQLiteOpenHelper sqLiteOpenHelper = mock(SQLiteOpenHelper.class);
        String[] columns = getTestColumns();
        FtsQuery ftsQuery = mock(FtsQuery.class);
        Cursor cursor = mock(Cursor.class);
        when(ftsQuery.query(SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?", 
                            new String[] {"inf*"}, 
                            columns, 
                            25)).thenReturn(cursor);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        testSearchEngine.setFtsQuery(ftsQuery);
        Uri uri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=25");
        qb.queryType = SearchEngineBase.LEXICAL_SEARCH_SUGGEST;
        qb.uri = uri;
        qb.searchTerm = "INF";
        assertThat(testSearchEngine.query(qb, sqLiteOpenHelper)).isEqualTo(cursor);
        verify(cursor).setNotificationUri(TestRoboApplication.getTestInstance().getContentResolver(), uri);
    }
    
    @Test
    public void test_FTS_QueryFail() throws Exception
    {
        FtsQueryBuilder qb = mock(FtsQueryBuilder.class);
        SQLiteOpenHelper sqLiteOpenHelper = mock(SQLiteOpenHelper.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(sqLiteOpenHelper.getWritableDatabase()).thenReturn(db);
        String[] columns = getTestColumns();
        FtsQuery ftsQuery = mock(FtsQuery.class);
        Cursor cursor = mock(Cursor.class);
        when(ftsQuery.query(SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?", 
                            new String[] {"inf*"}, 
                            columns, 
                            25)).thenReturn(null);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        testSearchEngine.setFtsQuery(ftsQuery);
        Uri uri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=25");
        qb.queryType = SearchEngineBase.LEXICAL_SEARCH_SUGGEST;
        qb.uri = uri;
        qb.searchTerm = "INF";
        when(qb.doQuery(db)).thenReturn(cursor);
        assertThat(testSearchEngine.query(qb, sqLiteOpenHelper)).isEqualTo(cursor);
        verify(cursor).setNotificationUri(TestRoboApplication.getTestInstance().getContentResolver(), uri);
    }
    
    @Test
    public void test_FTS_QueryFallback() throws Exception
    {
        FtsQueryBuilder qb = mock(FtsQueryBuilder.class);
        SQLiteOpenHelper sqLiteOpenHelper = mock(SQLiteOpenHelper.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(sqLiteOpenHelper.getWritableDatabase()).thenReturn(db);
        Cursor cursor = mock(Cursor.class);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        Uri uri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=25");
        qb.queryType = SearchEngineBase.LEXICAL_SEARCH_SUGGEST;
        qb.uri = uri;
        qb.searchTerm = "INF";
        qb.selection = "*";
        qb.selectionArgs = new String[]{};
        when(qb.doQuery(db)).thenReturn(cursor);
        assertThat(testSearchEngine.query(qb, sqLiteOpenHelper)).isEqualTo(cursor);
        assertThat(qb.selection).isNull();
        assertThat(qb.selectionArgs).isNull();
        verify(cursor).setNotificationUri(TestRoboApplication.getTestInstance().getContentResolver(), uri);
    }
    
    @Test
    public void test_searchSuggestionQuery() throws Exception
    {
        FtsQueryBuilder qb = mock(FtsQueryBuilder.class);
        SQLiteOpenHelper sqLiteOpenHelper = mock(SQLiteOpenHelper.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(sqLiteOpenHelper.getWritableDatabase()).thenReturn(db);
        Cursor cursor = mock(Cursor.class);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        Uri suggestUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY  + "/information?limit=50");
        qb.queryType = SearchEngineBase.SEARCH_SUGGEST;
        qb.uri = suggestUri;
        qb.searchTerm = "information";
        qb.selection = "*";
        qb.selectionArgs = new String[]{};
        when(qb.doQuery(db)).thenReturn(cursor);
        assertThat(testSearchEngine.query(qb, sqLiteOpenHelper)).isEqualTo(cursor);
        assertThat(qb.selection).isNull();
        assertThat(qb.selectionArgs).isNull();
        verify(cursor).setNotificationUri(TestRoboApplication.getTestInstance().getContentResolver(), suggestUri);
    }
    
    @Test
    public void test_searchSuggestionQueryFail() throws Exception
    {
        FtsQueryBuilder qb = mock(FtsQueryBuilder.class);
        SQLiteOpenHelper sqLiteOpenHelper = mock(SQLiteOpenHelper.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(sqLiteOpenHelper.getWritableDatabase()).thenReturn(db);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        Uri suggestUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY  + "/information?limit=50");
        qb.queryType = SearchEngineBase.SEARCH_SUGGEST;
        qb.uri = suggestUri;
        qb.searchTerm = "information";
        qb.selection = "*";
        qb.selectionArgs = new String[]{};
        when(qb.doQuery(db)).thenReturn(null);
        assertThat(testSearchEngine.query(qb, sqLiteOpenHelper)).isNull();
        assertThat(qb.selection).isNull();
        assertThat(qb.selectionArgs).isNull();
    }
    
    @Test
    public void test_nodeQuery() throws Exception
    {
        FtsQueryBuilder qb = mock(FtsQueryBuilder.class);
        SQLiteOpenHelper sqLiteOpenHelper = mock(SQLiteOpenHelper.class);
        SQLiteDatabase db = mock(SQLiteDatabase.class);
        when(sqLiteOpenHelper.getWritableDatabase()).thenReturn(db);
        Cursor cursor = mock(Cursor.class);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        Uri nodeItemUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES + "/1");
        qb.queryType = TestSearchEngine.ALL_NODES_TYPE_ID;
        qb.uri = nodeItemUri;
        when(qb.doQuery(db)).thenReturn(cursor);
        assertThat(testSearchEngine.query(qb, sqLiteOpenHelper)).isEqualTo(cursor);
    }
    
    @Test 
    public void test_getSuggestions()
    {
        String[] columns = getTestColumns();
        FtsQuery ftsQuery = mock(FtsQuery.class);
        Cursor cursor = mock(Cursor.class);
        when(ftsQuery.query(SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?", 
                            new String[] {"inf*"}, 
                            columns, 
                            25)).thenReturn(cursor);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        testSearchEngine.setFtsQuery(ftsQuery);
        assertThat(testSearchEngine.getSuggestions("INF", 25)).isEqualTo(cursor);
    }
    
    @Test 
    public void test_doLexicalSearch()
    {
        String[] columns = getTestColumns();
        FtsQuery ftsQuery = mock(FtsQuery.class);
        Cursor cursor = mock(Cursor.class);
        when(ftsQuery.query(SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?", 
                            new String[] {"inf*"}, 
                            columns, 
                            25)).thenReturn(cursor);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        testSearchEngine.setFtsQuery(ftsQuery);
        Uri uri =Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=25");
        assertThat(testSearchEngine.doLexicalSearch("INF", uri)).isEqualTo(cursor);
        verify(cursor).setNotificationUri(TestRoboApplication.getTestInstance().getContentResolver(), uri);
    }
    
    @Test 
    public void test_doLexicalSearchFail()
    {
        String[] columns = getTestColumns();
        FtsQuery ftsQuery = mock(FtsQuery.class);
        when(ftsQuery.query(SearchManager.SUGGEST_COLUMN_TEXT_1 + " MATCH ?", 
                            new String[] {"inf*"}, 
                            columns, 
                            25)).thenReturn(null);
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        testSearchEngine.setFtsQuery(ftsQuery);
        Uri uri =Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=25");
        assertThat(testSearchEngine.doLexicalSearch("INF", uri)).isNull();
    }
    
    @Test
    public void test_getLimitFromUri()
    {
        Uri allNodesUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES);
        
        assertThat(TestSearchEngine.getLimitFromUri(allNodesUri)).isEqualTo(0);
        allNodesUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=25");
        assertThat(TestSearchEngine.getLimitFromUri(allNodesUri)).isEqualTo(25);
        allNodesUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + PATH_ALL_NODES +
                "?" + SearchManager.SUGGEST_PARAMETER_LIMIT + "=!");
        assertThat(TestSearchEngine.getLimitFromUri(allNodesUri)).isEqualTo(0);
    }
 
    @Test 
    public void test_notifyChange()
    {
        TestSearchEngine testSearchEngine = new TestSearchEngine();
        testSearchEngine.context = mock(Context.class);
        ContentResolver contentResolver = mock(ContentResolver.class);
        when(testSearchEngine.context.getContentResolver()).thenReturn(contentResolver);
        Uri uri = mock(Uri.class);
        testSearchEngine.notifyChange(uri);
        verify(contentResolver).notifyChange(uri, null);
    }
    
    protected String[] getTestColumns()
    {
        return new String[] 
        {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
         /* SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
                          (only if you want to refresh shortcuts) */
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
        };
    }
}
