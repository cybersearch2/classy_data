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

/**
 * FieldKey
 * Used by ClassAnalyser to configure foreign collections and foreign fields
 * @author Andrew Bowley
 * 25/05/2014
 */
public class FieldKey implements Comparable<FieldKey>
{
    /** Class of entity with one or more OneToMany or ManyToOne annotations */
    protected Class<?> entityClass;
    /** Name of "mappedBy" field */
    protected String columnName;
 
    /**
     * Construct a FieldKey Instance
     * @param entityClass Class of entity with one or more OneToMany or ManyToOne annotations
     * @param columnName Name of "mappedBy" field 
     */
    public FieldKey(Class<?> entityClass, String columnName)
    {
        this.entityClass = entityClass;
        this.columnName = columnName;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param   another The object to be compared.
     * @return  A negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this object.
     */
    @Override
    public int compareTo(FieldKey another) 
    {
        int compareClasses = entityClass.getName().compareTo(another.entityClass.getName());
        return (compareClasses != 0) ? compareClasses : (columnName.compareTo(another.columnName));
    }

    /**
     * Returns Class of entity with one or more OneToMany or ManyToOne annotations
     * @return Class
     */
    public Class<?> getEntityClass() 
    {
        return entityClass;
    }

    /**
     * Set class of entity with one or more OneToMany or ManyToOne annotations 
     * @param entityClass Class
     */
    public void setEntityClass(Class<?> entityClass) 
    {
        this.entityClass = entityClass;
    }

    /**
     * Returns name of "mappedBy" field
     * @return String
     */
    public String getColumnName() 
    {
        return columnName;
    }

    /**
     * Set name of "mappedBy" field
     * @param columnName String
     */
    public void setColumnName(String columnName) 
    {
        this.columnName = columnName;
    }

    /**
     * Returns a hash code value for the object. This method is 
     * supported for the benefit of hashtables such as those provided by 
     * <code>java.util.Hashtable</code>. 
     * @see java.lang.Object#hashCode()
     */ 
    @Override
    public int hashCode()
    {
        return entityClass.hashCode() ^ columnName.hashCode();
    }
  
    /**
     * Indicates whether some other object is "equal to" this one.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object another)
    {
        if (another instanceof FieldKey)
            return entityClass.equals(((FieldKey) another).entityClass) && 
                    columnName.equals(((FieldKey) another).columnName);
        return false;
    }
}
