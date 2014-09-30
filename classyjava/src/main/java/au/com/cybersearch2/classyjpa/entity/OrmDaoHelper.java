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
 * OrmDaoHelper
 * JPA adapter for OrmLite
 * @author Andrew Bowley
 * 02/05/2014
 */
public class OrmDaoHelper<T,ID>
{
    protected PersistenceDao<T, ID> entityDao; 
    
    public OrmDaoHelper(PersistenceDao<T, ID> entityDao)
    {
        this.entityDao = entityDao;
    }
    
    /**
     * Persist an object for the first time. Creates entity table if it does not exist.
     * Uses a PersistenceDao object to perform database operations. @see com.j256.ormlite.dao.PersistenceDao.
     * OrmLite comments:
     * Create a new row in the database from an object. If the object being created uses
     * annotation DatabaseField "generatedId" then the data parameter will be modified and set with the corresponding id
     * from the database.
     *
     * @param object - the entity to be persisted
     * @return The number of rows updated in the database. This should be 1.
     */
    public int create(Object object)
    {
        @SuppressWarnings("unchecked")
        T entity = (T)object;
        return entityDao.create(entity);
    }

    /**
     * Retrieves an object associated with a specific ID.
     * 
     * @param primaryKey Identifier that matches a specific row in the database to find and return.
     * @return The object that has the ID field which equals id or null if no matches.
     * @throws RuntimeException on any SQL problems or if more than 1 item with the id are found in the database.
     */
    public T queryForId(Object primaryKey)
    {
        @SuppressWarnings("unchecked")
        ID id = (ID)primaryKey;
        return entityDao.queryForId(id);
    }

    /**
     * Query for a data item in the table that has the same id as the data parameter.
     */
    public T queryForSameId(Object object)
    {
        @SuppressWarnings("unchecked")
        T entity = (T)object;
        return entityDao.queryForSameId(entity);
    }

    /**
     * Returns the ID from the data parameter passed in. This is used by some of the internal queries to be able to
     * search by id.
     */
     public ID extractId(Object object)
     {
         @SuppressWarnings("unchecked")
         T entity = (T)object;
         return entityDao.extractId(entity);
     }

     /**
      * Returns true if an object exists that matches this ID otherwise false.
      */
     public boolean entityExists(Object object)
     {
         @SuppressWarnings("unchecked")
         T entity = (T)object;
         ID id = entityDao.extractId(entity);
         return entityDao.idExists(id);
     }

    /**
     * Store the fields from an object to the database row corresponding to the id from the data parameter. If you have
     * made changes to an object, this is how you persist those changes to the database. You cannot use this method to
     * update the id field.
     * 
     * <p>
     * NOTE: This will not save changes made to foreign objects or to foreign collections.
     * </p>
     * 
     * @param object The data item that we are updating in the database.
     * @return The number of rows updated in the database. This should be 1.
     * @throws RuntimeException on any SQL problems.
     * @throws IllegalArgumentException If there is only an ID field in the object.
     */
    public int update(Object object)
    {
        @SuppressWarnings("unchecked")
        T entity = (T)object;
        return entityDao.update(entity);
    }

    /**
     * Does a query for the data parameter's id and copies in each of the field values from the database to refresh the
     * data parameter. Any local object changes to persisted fields will be overwritten. If the database has been
     * updated this brings your local object up to date.
     * 
     * @param object The data item that we are refreshing with fields from the database.
     * @return The number of rows found in the database that correspond to the data id. This should be 1.
     * @throws RuntimeException on any SQL problems or if the data item is not found in the table 
     * or if more than 1 item is found with data's id.
     */
    @SuppressWarnings("unchecked")
    public int refresh(Object object)
    {
        return entityDao.refresh((T) object);
    }


    /**
     * Delete the database row corresponding to the id from the data parameter.
     * 
     * @param object The data item that we are deleting from the database.
     * @return The number of rows updated in the database. This should be 1.
     * @throws RuntimeException on any SQL problems.
     */
    public int delete(Object object)
    {
        @SuppressWarnings("unchecked")
        T entity = (T)object;
        return entityDao.delete(entity);
    }


}
