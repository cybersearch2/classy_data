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
package au.com.cybersearch2.classyjpa.entity;

/**
 * EntityKey
 * Used by Entity Manager to identify entity objects
 * @author Andrew Bowley
 * 05/05/2014
 */
public class EntityKey implements Comparable<EntityKey>
{
    /** Entity class hashcode */
    int entityClassHash;
    /** Primary key hashcode */
    int primaryKeyHash;
    /** Flag set true if updates need to be persisted on the entity identifed by this key */
    boolean dirty;
  
    /**
     * Create EntityKey object
     * @param entityClass Class of entity object
     * @param primaryKey Primary key of object
     */
    public EntityKey(Class<?> entityClass, Object primaryKey)
    {
        entityClassHash = entityClass.hashCode();
        primaryKeyHash = primaryKey.hashCode();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param   another The object to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *      is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(EntityKey another) 
    {
        return (entityClassHash < another.entityClassHash) ? -1 : (primaryKeyHash - another.primaryKeyHash);
    }

    /**
     * Returns true if updates need to be persisted on the entity identifed by this key
     * @return boolean
     */
    public boolean isDirty() 
    {
        return dirty;
    }

    /**
     * Set dirty flag
     * @param dirty boolean
     */
    public void setDirty(boolean dirty) 
    {
        this.dirty = dirty;
    }

    /**
     * Returns a hash code value for the object. This method is 
     * supported for the benefit of hashtables such as those provided by 
     * <code>java.util.Hashtable</code>. 
     *
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    @Override
    public int hashCode()
    {
        return entityClassHash ^ primaryKeyHash;
    }
    
    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param   another   The reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
     */
    @Override
    public boolean equals(Object another)
    {
        if (another instanceof EntityKey)
            return (entityClassHash == ((EntityKey)another).entityClassHash) && (primaryKeyHash == ((EntityKey)another).primaryKeyHash);
        return false;
    }
    
}
