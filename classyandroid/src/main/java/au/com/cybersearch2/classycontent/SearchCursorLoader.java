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

import android.content.Context;
import android.support.v4.content.CursorLoader;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classytask.WorkTracker;

/**
 * SearchCursorLoader
 * CursorLoader for Search Suggestions
 * @author Andrew Bowley
 * 25/07/2014
 */
public class SearchCursorLoader extends CursorLoader
{
    /** Work status tracker, notified when load complete */
    protected WorkTracker workTracker;
    /** Search query term */
    protected String searchTerm;
    
    /**
     * Create SearchCursorLoader object
     * @param context Android Application Context
     * @param params LoaderCursor constructor parameters 
     */
    public SearchCursorLoader(Context context, SuggestionCursorParameters params)
    {
        super(context, 
                params.getUri(), 
                params.getProjection(), 
                params.getSelection(), 
                params.getSelectionArgs(), 
                params.getSortOrder());
        workTracker = new WorkTracker();
        searchTerm = params.selectionArgs[0];
    }

    /**
     * Returns work tracker object used to notify loading complete
     * @return WorkTracker
     */
    public WorkTracker getWorkTracker() 
    {
        return workTracker;
    }

    /**
     * Returns search query term
     * @return String
     */
    public String getSearchTerm()
    {
        return searchTerm;
    }
    
    /**
     * Update work status and notify workTracker object if status is completion one
     * @param status WorkStatus
     */
    public void setStatus(WorkStatus status)
    {
        workTracker.setStatus(status);
        if (status != WorkStatus.RUNNING)
            synchronized(workTracker)
            {   // FINISHED or FAILED 
                workTracker.notifyAll();
            }
    }
}
