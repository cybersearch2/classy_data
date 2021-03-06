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
package au.com.cybersearch2.classyjpa.query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * SqlQuery
 * Implements javax.persistence.Query invoked using an Android SQLite interface.
 * The SQL is executed with OrmLite JDBC when not running on Android.
 * @author Andrew Bowley
 * 10/07/2014
 */
public class SqlQuery
{
    public static final String TAG = "SqlQuery";
    protected static Log log = JavaLogger.getLogger(TAG);
    /** Date format to suite SQLite database */
    protected final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /** JPA Support */
    protected PersistenceAdmin persistenceAdmin;
    /** Native query information */
    protected QueryInfo queryInfo;
    /** Selection arguments */
    protected List<String> selectionArgs;

    /**
     * Create SqlQuery object
     * @param persistenceAdmin JPA Support
     * @param queryInfo Native query information
     */
    public SqlQuery(PersistenceAdmin persistenceAdmin, QueryInfo queryInfo)
    {
        this.persistenceAdmin = persistenceAdmin;
        this.queryInfo = queryInfo;
        selectionArgs = new ArrayList<String>();
     }

    /**
     * Execute query and return results as a list of Objects
     * @return Object list
     */
    public List<?> getResultObjectList() 
    {
        return getResultObjectList(0,0);
    }
    
    /**
     * Execute query and return results as a list of Objects
     * @return Object list
     */
    public List<?> getResultObjectList(int startPosition, int maxResults) 
    {
        queryInfo.setSelectionArgs(selectionArgs.toArray(new String[selectionArgs.size()]));
        return persistenceAdmin.getResultList(queryInfo, startPosition, maxResults);
    }

    /**
     * Execute query and return a single Object result
     * @return Object or null if nothing returned by query
     */
    public Object getResultObject() 
    {
        queryInfo.setSelectionArgs(selectionArgs.toArray(new String[selectionArgs.size()]));
        return persistenceAdmin.getSingleResult(queryInfo);
    }

    /**
     * Set parameter value referenced by position
     * @param position Starts at 1
     * @param value Object
     * @return boolean - true if position is valid, otherwise false
     */
    public boolean setParam(int position, Object value)
    {
        if (position > 0)
        {
            if ((queryInfo.getParameterNames() != null) && (position > queryInfo.getParameterNames().length))
                logInvalidIndex(position);
            else
            {
                selectionArgs.add(position - 1, value == null ? null : formatObject(value));
                return true;
            }
        }
        else
            logInvalidIndex(position);
        return false;
    }
    
    /**
     * Set parameter referenced by name
     * @param param
     * @param value Object
     * @return boolean - true if name is valid, otherwise false
     */
    public boolean setParam(String param, Object value)
    {
        if (param == null)
        {
            log.error(TAG, "Null query parameter encountered for named query '" + queryInfo.getSelection() + "'");
            return false;
        }
        if (queryInfo.getParameterNames() == null)
        {
            log.error(TAG, "Query parameters not supported for named query '" + queryInfo.getSelection() + "'");
            return false;
        }
        String[] parameterNames = queryInfo.getParameterNames();
        int index = 0;
        for (; index < parameterNames.length; index++)
            if (param.equals(parameterNames[index]))
                break;
         if (index == parameterNames.length)
         {
            log.error(TAG, "Query parameter '" + param + "' not found for named query '" + queryInfo.getSelection() + "'");
            return false;
        }
        selectionArgs.add(index, value == null ? null : formatObject(value));
        return true;
    }

    /**
     * Returns object value as String
     * @param value Object
     * @return String
     */
    protected String formatObject(Object value) 
    {
        if (value instanceof Date)
        {   // Dates have to be of standard format for SQLite
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            String dateValue = sdf.format((Date)value); 
            // Append ".SSSSSS" part of format as zeros as non-zero values are not converted correctly
            return dateValue + ".000000";
        }
        return value.toString();
    }

    /**
     * Log "position out of range" message
     * @param position Invalid position value
     */
    protected void logInvalidIndex(int position)
    {
        log.error(TAG, "Query parameter " + position + " out of range for " + toString());
        //throw new IllegalArgumentException("Parameter \"" + position + "\" is invalid");
    }

    /**
     * Returns a string representation of the object.
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() 
    {
        return "Named query for '" + queryInfo.getSelection() + "'";
    }
}
