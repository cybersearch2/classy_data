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
package au.com.cybersearch2.classycontent;

import android.app.SearchManager;
import android.net.Uri;
import android.os.Bundle;

/**
 * SuggestionCursorParameters
 * Parameters required to create a CursorLoader used for search suggestions. 
 * @author Andrew Bowley
 * 25/07/2014
 */
public class SuggestionCursorParameters
{
    /** Key used for Bundle search query text */
    public final static String QUERY_TEXT_KEY = "QUERY_TEXT_KEY";

    /** The URI, using the content:// scheme, for the content to retrieve. */
    final protected Uri uri;
    /** A list of which columns to return. */
    protected String[] projection;
    /** A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself).*/
    final protected String selection;
    /** Values to use in place of question marks in the order that they appear in the selection.*/
    final protected String[] selectionArgs;
    /** How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).*/
    protected String sortOrder;

    /**
     * Create a SuggestionCursorParameters object
     * @param bundle Bundle created by SearchManager to pass the query text
     * @param uri The URI, using the content:// scheme, for the content to retrieve 
     * @param limit The maximum number of items to retrieve or unlimited if zero
     */
    public SuggestionCursorParameters(Bundle bundle, Uri uri, int limit)
    {
        this.uri = uri;
        String query = "";
        if (bundle != null) // Extract the search query from the arguments
            query = bundle.getString(QUERY_TEXT_KEY);
        // Construct the new query in the form a a Cursor Loader
        selection = "word MATCH ?";
        selectionArgs = new String[] { query };
        // Append limit query parameter
        if (limit > 0)
            uri = uri.buildUpon().appendQueryParameter(
                SearchManager.SUGGEST_PARAMETER_LIMIT, String.valueOf(limit)).build();
    }

    /**
     * Returns a list of which columns to return
     * @return String[]
     */
    public String[] getProjection() {
        return projection;
    }

    /**
     * Set a list of which columns to return
     * @param projection String[]
     */
    public void setProjection(String[] projection) {
        this.projection = projection;
    }

    /**
     * Returns how to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * @return String
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Set how to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * @param sortOrder String
     */
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Returns the URI, using the content:// scheme, for the content to retrieve
     * @return Uri
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * Set A filter declaring which rows to return, formatted as an
     *         SQL WHERE clause (excluding the WHERE itself). Passing null will
     *         return all rows for the given URI.
     * @return String
     */
    public String getSelection() {
        return selection;
    }

    /**
     * Returns a filter declaring which rows to return
     * @return String
     */
    public String[] getSelectionArgs() {
        return selectionArgs;
    }
}
