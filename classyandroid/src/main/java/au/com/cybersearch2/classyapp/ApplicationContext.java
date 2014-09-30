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
package au.com.cybersearch2.classyapp;

import java.io.IOException;

import javax.inject.Inject;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import au.com.cybersearch2.classyinject.DI;

/**
 * ApplicationContext
 * Provides access to Application Context by dependency injection and adds convenience methods
 * @author Andrew Bowley
 * 20/06/2014
 */
public class ApplicationContext
{
    static final String TAG = "ApplicationContext";
    
    /** Android Application context. Inject into a singleton rather than everywhere this ubiquitous object is required. */
    @Inject Context context;
 
    /**
     * Create ApplicationContext object
     */
    public ApplicationContext()
    {
        DI.inject(this);
    }
    
    /**
     * Returns Android Application context
     * @return Context
     */
    public Context getContext()
    {
        return context;
    }

    /**
     * Returns Android Application context content resolver
     * @return ContentResolver
     */
    public ContentResolver getContentResolver()
    {
        return context.getContentResolver();
    }
    
    /**
     * Returns document element attribute identified by name from XML file identified by resource ID 
     * @param resourceId 
     * @param name
     * @return String value or null if attribute not found or other error occurs
     */
    public String getDocumentAttribute(int resourceId, String name)
    {
        XmlResourceParser parser = null;
        String value = null;
        try
        {
            parser = context.getResources().getXml(resourceId);
            int eventType = parser.getEventType();
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                eventType = parser.next();
                if (eventType == XmlPullParser.START_TAG)
                {
                    value = parser.getAttributeValue("http://schemas.android.com/apk/res/android", name);
                }
            }

        }
        catch (NotFoundException e)
        {
            Log.e(TAG, "Resource attribute \"" + name + "\" not found");
        }
        catch (XmlPullParserException e)
        {
            Log.e(TAG, e.toString());
        }
        catch (IOException e)
        {
            Log.e(TAG, e.toString());
        }
        finally
        {
            if (parser != null)
                parser.close();
        }
        return value;
    }
}
