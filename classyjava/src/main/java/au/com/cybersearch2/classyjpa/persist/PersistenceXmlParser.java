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
package au.com.cybersearch2.classyjpa.persist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

/**
 * PersistenceXmlParser
 * Parses persistence.xml to create object which maps each peristence unit data to it's name.
 * NOTE: Only data used by ClassyTools is extracted
 * @author Andrew Bowley
 * 11/05/2014
 */
public class PersistenceXmlParser
{
    public static final String TAG = "PersistenceXmlParser";
    private Log log = JavaLogger.getLogger(TAG);
    private XmlPullParser xpp;

    /**
     * Create PersistenceXmlParser object
     * @throws XmlPullParserException - unexpected error creating parser instance
     */
    public PersistenceXmlParser() throws XmlPullParserException
    {
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        xpp = factory.newPullParser();
    }

    /**
     * Returns object to which input stream for persistence.xml is unmarshalled
     * @param stream InputStream
     * @return Map&lt;String, PersistenceUnitInfo&gt; - maps each peristence unit data to it's name
     */
    public Map<String, PersistenceUnitInfo> parsePersistenceXml(InputStream stream)
    {
        Map<String, PersistenceUnitInfo> result = new HashMap<String, PersistenceUnitInfo>();
        Reader reader = new BufferedReader(new InputStreamReader(stream));
        try
        {
            xpp.setInput(reader);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) 
            {
                if (eventType == XmlPullParser.START_DOCUMENT) 
                {
                    //System.out.println("Start document");
                } 
                else if (eventType == XmlPullParser.START_TAG) 
                {
                    if ("persistence-unit".equals(xpp.getName()))
                    {   // PersistenceUnitAdmin unit element
                        String name = getAttribute("name");
                        if (name != null)
                            result.put(name, parsePersistenceUnit(name));
                    }
                } 
                /*
                else if (eventType == XmlPullParser.END_TAG) 
                {
                    System.out.println("End tag "+xpp.getName());
                } 
                else if (eventType == XmlPullParser.TEXT) 
                {
                    System.out.println("Text "+xpp.getText());
                }
                */
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException e)
        {
            log.error(TAG, "Error parsing persistence.xml", e);
        }
        catch (IOException e)
        {
            log.error(TAG, "Error reading persistence.xml", e);
        }
        return result;
    }

    /**
     * Returns persistence unit data
     * @param puName PersistenceUnitAdmin unit name
     * @return PersistenceUnitInfo
     * @throws XmlPullParserException
     * @throws IOException
     */
    private PersistenceUnitInfo parsePersistenceUnit(String puName) throws XmlPullParserException, IOException 
    {
        PersistenceUnitInfoImpl pu = new PersistenceUnitInfoImpl(puName);
        int eventType = xpp.next();
        while (eventType != XmlPullParser.END_DOCUMENT) 
        {
            if (eventType == XmlPullParser.END_TAG) 
            {   // PersistenceUnitAdmin unit end element 
                if ("persistence-unit".equals(xpp.getName()))
                    return pu;
            } 
            else if (eventType == XmlPullParser.START_TAG) 
            {   
                if ("provider".equals(xpp.getName()))
                {   // Provider class name
                    pu.persistenceProviderClassName = getText(); 
                }
                else if ("class".equals(xpp.getName()))
                {   // Entity class name
                    String className = getText();
                    if (className.length() > 0)
                        pu.managedClassNames.add(className);
                }
                else if ("property".equals(xpp.getName()))
                {   // Property
                    String name = getAttribute("name");
                    if ((name != null) && (name.length() > 0))
                        pu.getProperties().setProperty(name, getAttribute("value"));
                }
            } 
            eventType = xpp.next();
        }
        return pu;
    }

    /**
     * Returns text inside current element
     * @return String
     * @throws XmlPullParserException
     * @throws IOException
     */
    private String getText() throws XmlPullParserException, IOException 
    {
        if (xpp.next() ==  XmlPullParser.TEXT)
            return xpp.getText();
        return "";
    }

    /**
     * Returns attribute value for specified attribute in current element
     * @param name Attribute name
     * @return String
     */
    String getAttribute(String name)
    {
        for (int i = 0; i < xpp.getAttributeCount(); i++)
            if (xpp.getAttributeName(i).equals(name)) 
                return xpp.getAttributeValue(i);
        return null;
    }

}
