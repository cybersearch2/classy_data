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
import android.database.Cursor;

/**
 * CursorAdapaterParameters
 * Encapsulates parameters to pass to CursorAdaptor to populate a standard 2-item list
 * @author Andrew Bowley
 * 25/07/2014
 */
public class CursorAdapaterParameters
{
    final int layout;
    final String[]  uiBindFrom;
    final int[]  uiBindTo;
    Cursor cursor;
    int flags;

    /**
     * Create CursorAdapaterParameters object
     */
    public CursorAdapaterParameters()
    {
        // Use Android 2 column simple list layout
        layout = android.R.layout.simple_list_item_2;
        // Map SearchManager column names to items
        uiBindFrom = new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2 };
        // Map Android resource IDs to items
        uiBindTo = new int[] { android.R.id.text1, android.R.id.text2 };
        // Flags default = 0
        flags = 0;
    }

    /**
     * Returns cursor which iterates over database rows to provide list item values
     * @return Cursor
     */
    public Cursor getCursor() {
        return cursor;
    }

    /**
     * Set cursor which iterates over database rows to provide list item values
     * @param cursor Cursor
     */
    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    /**
     * Returns flags
     * @return int value, default = 0
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Set flags
     * @param flags int value
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * Returns list layout resource ID
     * @return int value
     */
    public int getLayout() {
        return layout;
    }

    /**
     * Returns array which maps column names to items
     * @return String[]
     */
    public String[] getUiBindFrom() {
        return uiBindFrom;
    }

    /**
     * Returns array which maps resource IDs to items
     * @return int[]
     */
    public int[] getUiBindTo() {
        return uiBindTo;
    }

}
