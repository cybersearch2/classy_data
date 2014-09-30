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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * PersistenceXmlParserTest
 * @author Andrew Bowley
 * 11/05/2014
 */
public class PersistenceXmlParserTest
{
    static final String PERSISTENCE_XML =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
      "<persistence xmlns=\"http://java.sun.com/xml/ns/persistence\"" +
                   "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                   "version=\"2.0\">" +
        "<persistence-unit name=\"classyfy\">" +
           "<provider>au.com.cybersearch2.ClassyFyProvider</provider>" + 
           "<class>au.com.cybersearch2.data.alfresco.RecordCategory</class>" +
        "</persistence-unit>" +
      "</persistence>";
    
    @Test
    public void test_PersistenceXmlParser() throws Exception
    {
        PersistenceXmlParser parser = new PersistenceXmlParser();
        Map<String, PersistenceUnitInfo> result = parser.parsePersistenceXml(new ByteArrayInputStream(PERSISTENCE_XML.getBytes()));
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        PersistenceUnitInfo info = result.get("classyfy");
        assertThat(info).isNotNull();
        assertThat(info.getPersistenceUnitName()).isEqualTo("classyfy");
        List<String> managed = info.getManagedClassNames();
        assertThat(managed.size()).isEqualTo(1);
        assertThat(managed.get(0)).isEqualTo("au.com.cybersearch2.data.alfresco.RecordCategory");
    }

}
