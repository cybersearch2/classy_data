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
package au.com.cybersearch2.classybean;

/**
 * BeanException
 * A method is not required to declare in its <code>throws</code> 
 * clause any subclasses of <code>BeanException</code> that might 
 * be thrown during the execution of the method but not caught. 
 * @author Andrew Bowley
 * 28/05/2014
 */
public class BeanException extends RuntimeException
{

    private static final long serialVersionUID = 3514326495671985012L;

    /** Constructs a new runtime exception with <code>null</code> as its
     * detail message.  The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public BeanException()
    {
    }

    /** Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param   detailMessage   the detail message. The detail message is saved for 
     *          later retrieval by the {@link #getMessage()} method.
     */
    public BeanException(String detailMessage)
    {
        super(detailMessage);

    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     * @since  1.4
     */
    public BeanException(Throwable cause)
    {
        super(cause);

    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated in
     * this runtime exception's detail message.
     *
     * @param  detailMessage the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public BeanException(String detailMessage, Throwable cause)
    {
        super(detailMessage, cause);

    }

}
