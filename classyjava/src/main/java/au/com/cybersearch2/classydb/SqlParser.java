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
package au.com.cybersearch2.classydb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.sql.SQLException;

/**
 * SqlParser
 * Parse SQL script using token parser to create a set of semi colon delimited statements
 * @author Andrew Bowley
 * 30/07/2014
 */
public class SqlParser
{
    protected int count;

    /**
     * StatementCallback
     * Interface to call back when a one-line statement has been parsed
     * @author Andrew Bowley
     * 30/07/2014
     */
    public interface StatementCallback
    {
        /**
         * Consume one SQL statement
         *@param statement
         *@throws SQLException
         */
        void onStatement(String statement) throws SQLException;
    }
 
    /**
     * Public API call
     *@param is InputStream object
     *@param callback StatementCallback object
     *@throws IOException for input stream error
     *@throws SQLException for StatementCallback error
     */
    public void parseStream(InputStream is, StatementCallback callback) throws IOException, SQLException
    {
        Reader r = new BufferedReader(new InputStreamReader(is));
        StreamTokenizer st = new StreamTokenizer(r);
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars('0', '9');
        st.wordChars(128 + 32, 255);
        st.whitespaceChars(0, 31); // ' ' is not whitespace so it will be inserted in the text
        st.quoteChar('\'');
        st.eolIsSignificant(false);
        int tok = StreamTokenizer.TT_EOF;
        StringBuffer buff = new StringBuffer();
        do 
        {
            tok = st.nextToken();
            switch (tok)
            {
            case StreamTokenizer.TT_EOF:    // End of input
            case StreamTokenizer.TT_EOL:    // End of line
            case StreamTokenizer.TT_NUMBER: // Not activated
                break;
            case StreamTokenizer.TT_WORD:   // Any printable character sequence terminated by single quote, eol or eof 
                buff.append(st.sval);
                break;
            case '\'':                      // Preserve escaped single quotes
                buff.append('\'');
                buff.append(st.sval);
                buff.append('\'');
                break;
           default:
                buff.append((char)tok);
                if (tok == ';') // Semi colon terminates SQL statement
                {
                    callback.onStatement(buff.toString());
                    ++count;
                    buff.setLength(0);
                }
            }
        } while (tok != StreamTokenizer.TT_EOF);
    }

    /**
     * Returns number of statements parsed
     *@return int
     */
    public int getCount() 
    {
        return count;
    }
}
