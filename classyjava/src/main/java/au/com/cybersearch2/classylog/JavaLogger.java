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
package au.com.cybersearch2.classylog;

import java.util.logging.*;

/**
 * JavaLogger
 * Logger readily interchangeable with Android android.util.Log, implemented using java.util.logging.Logger
 * Mapping Android levels to Java levels:
 * VERBOSE = FINEST
 * DEBUG = FINE
 * INFO = INFO
 * WARN = WRNING
 * ERROR = SEVERE
 * @author Andrew Bowley
 * 11/06/2014
 */
public class JavaLogger implements Log
{
    /** Use java.util.logging package for actual log implementation */
    private Logger logger;
    /** Tag Used to identify the source of a log message */
    private String name;
    
    /**
     * Create JavaLogger object. Call static getLogger() to invoke constructor.
     * @param name Tag Used to identify the source of a log message
     */
    protected JavaLogger(String name)
    {
        this.name = name;
        logger = Logger.getLogger(name);
    }

    /**
     * JavaLogger class factory
     * @param name Tag Used to identify the source of a log message
     * @return JavaLogger
     */
    public static JavaLogger getLogger(String name)
    {
        return new JavaLogger(name);
    }
    
    /**
     * Send a VERBOSE log message. Level = FINEST.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void verbose(String tag, String msg) 
    {
        logger(tag).logp(Level.FINEST, tag, null, msg);
    }

    /**
     * Send a VERBOSE log message and log the exception. Level = FINEST.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void verbose(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.FINEST, tag, null, msg, tr);
    }

    /**
     * Send a DEBUG log message. Level = FINE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void debug(String tag, String msg) 
    {
        logger(tag).logp(Level.FINE, tag, null, msg);
    }

    /**
     * Send a DEBUG log message and log the exception. Level = FINE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void debug(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.FINE, tag, null, msg, tr);
    }

    /**
     * Send an INFO log message. Level = INFO. Level = INFO.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void info(String tag, String msg) 
    {
        logger(tag).logp(Level.INFO, tag, null, msg);
    }

    /**
     * Send a INFO log message and log the exception. Level = INFO.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void info(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.INFO, tag, null, msg, tr);
    }

    /**
     * Send a WARN log message. Level = WARNING.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void warn(String tag, String msg) 
    {
        logger(tag).logp(Level.WARNING, tag, null, msg);
    }

    /**
     * Send a #WARN log message and log the exception. Level = WARNING.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void warn(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.WARNING, tag, null, msg, tr);
    }

    /**
     * Send an ERROR log message. Level = SEVERE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    @Override
    public void error(String tag, String msg) 
    {
        logger(tag).logp(Level.SEVERE, tag, null, msg);
    }

    /**
     * Send an ERROR log message and log the exception. Level = SEVERE.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    @Override
    public void error(String tag, String msg, Throwable tr) 
    {
        logger(tag).logp(Level.SEVERE, tag, null, msg, tr);
    }

    /**
     * Checks to see whether or not a log for the specified tag is loggable at the specified level.
     * 
     * NOTE IF USING Android Log implementation:    
     * Log.isLoggable() will throw an exception if the length of the tag is greater than
     * 23 characters, so trim it if necessary to avoid the exception.
     *
     * @param tag The tag to check.
     * @param level The level to check.
     * @return Whether or not that this is allowed to be logged.
     */
    @Override
    public boolean isLoggable(String tag, Level level) 
    {
        return logger(tag).isLoggable(level);
    }
    
    /**
     * Set logging level. 
     * NOTE IF USING Android Log implementation, this function is not supported natively by Android.
     */
    @Override
    public void setLevel(Level level) 
    {
        logger.setLevel(level);
    }

    /**
     * Get logger referenced by tag. Handle mismatch of tag to this logger's name gracefully.
     * @param tag Used to identify the source of a log message. 
     * @return Logger This logger if tag matches name or tag is empty, otherwise logger obtained by Logger.getLogger(tag).
     */
    protected Logger logger(String tag)
    {
        if (name.equals(tag) || (tag == null) || (tag.length() == 0))
            return logger;
        return Logger.getLogger(tag);
    }


}
