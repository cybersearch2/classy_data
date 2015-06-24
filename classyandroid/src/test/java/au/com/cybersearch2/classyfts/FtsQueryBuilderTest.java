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

import static org.fest.assertions.api.Assertions.*;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.robolectric.RobolectricTestRunner;

import android.app.SearchManager;
import android.net.Uri;

/**
 * FtsQueryBuilderTest
 * @author Andrew Bowley
 * 24 Jun 2015
 */
@RunWith(RobolectricTestRunner.class)
public class FtsQueryBuilderTest
{
    public static final String PROVIDER_AUTHORITY = "au.com.cybersearch2.classyfy.ClassyFyProvider";
    // The scheme part for this provider's URI
    private static final String SCHEME = "content://";
    // Column names
    // Android expects RowIDColumn to be "_id", so do not use any other value.
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_TITLE = "title";
    public static final String KEY_MODEL = "model";

    // Defines a selection column for the query. When the selection columns are passed
    // to the query, the selection arguments replace the placeholders.
    public static final String TITLE_SELECTION = "title = " + "?";

    // Defines the selection columns for a query.
    public static final String SELECTION_COLUMNS =
        TITLE_SELECTION;

     // Defines the arguments for the selection columns. Put in sort order for sort check below
    public static final String[] SELECTION_ARGS = { "Policy & Procedures"  };

     // Defines a query sort order
    public static final String SORT_ORDER = "title ASC";
    
    // Defines a projection of column names to return for a query
    static final String[] TEST_PROJECTION = {
        KEY_TITLE,
        KEY_NAME,
        KEY_MODEL
    };

    @Test
    public void testLexicalSearchSuggestion()
    {
        int queryType = SearchEngineBase.LEXICAL_SEARCH_SUGGEST;
        Uri uri = Uri.EMPTY; 
        final String[] projection = TEST_PROJECTION; 
        final String selection = TITLE_SELECTION;
        final String[] selectionArgs = SELECTION_ARGS;
        final String sortOrder = SORT_ORDER;
        // Constructor sets searchTerm and limit
        FtsQueryBuilder qb = new FtsQueryBuilder(
            queryType, 
            uri, 
            projection, 
            selection,
            selectionArgs, 
            sortOrder);
        assertThat(qb.getSearchTerm()).isEqualTo(SELECTION_ARGS[0]);
        assertThat(qb.getLimit()).isEqualTo(0);
        assertThat(qb.getQueryType()).isEqualTo(queryType);
        assertThat(qb.getProjection()).isEqualTo(TEST_PROJECTION);
        assertThat(qb.getSelection()).isEqualTo(TITLE_SELECTION);
        assertThat(qb.getSortOrder()).isEqualTo(SORT_ORDER);
    }

    @Test
    public void testNormalSearchSuggestion()
    {
        doNormalSearchSuggestion(SearchEngineBase.LEXICAL_SEARCH_SUGGEST);
        doNormalSearchSuggestion(SearchEngineBase.SEARCH_SUGGEST);
    }


    void doNormalSearchSuggestion(int queryType)
    {
        Uri suggestUri = Uri.parse(SCHEME + PROVIDER_AUTHORITY + "/" + SearchManager.SUGGEST_URI_PATH_QUERY  + "/information?limit=50");
        final String[] projection = TEST_PROJECTION; 
        final String selection = null;
        final String[] selectionArgs = null;
        final String sortOrder = SORT_ORDER;
        // Constructor sets searchTerm and limit
        FtsQueryBuilder qb = new FtsQueryBuilder(
            queryType, 
            suggestUri, 
            projection, 
            selection,
            selectionArgs, 
            sortOrder);
        assertThat(qb.getSearchTerm()).isEqualTo("information");
        assertThat(qb.getLimit()).isEqualTo(50);
    }
}
