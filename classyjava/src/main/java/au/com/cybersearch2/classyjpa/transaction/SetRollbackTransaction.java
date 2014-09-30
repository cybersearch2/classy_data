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
package au.com.cybersearch2.classyjpa.transaction;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

/**
 * SetRollbackTransaction
 * Transaction to allow user to rollback when not in User Transaction mode
 * @author Andrew Bowley
 * 10/06/2014
 */
public class SetRollbackTransaction implements EntityTransaction
{
    /** Wrapped transaction */
    protected EntityTransaction entityTransaction;
   
    /**
     * Create SetRollbackTransaction object
     * @param entityTransaction Wrapped transaction
     */
    public SetRollbackTransaction(EntityTransaction entityTransaction)
    {
        this.entityTransaction = entityTransaction;
    }

    /**
     * Ignore any call to start the resource transaction.
     */
    @Override
    public void begin() 
    {
    }
    
    /**
     * Ignore any call to commit the current transaction
     */
    @Override
    public void commit() 
    {
    }
    
    /**
     * Determine whether the current transaction has been marked
     * for rollback.
     * @throws IllegalStateException if {@link #isActive()} is false.
     */
    @Override
    public boolean getRollbackOnly() 
    {
        return entityTransaction.getRollbackOnly();
    }
    
    /**
     * Indicate whether a transaction is in progress.
     * @throws PersistenceException if an unexpected error condition is encountered.
     */
    @Override
    public boolean isActive() 
    {
        return entityTransaction.isActive();
    }
    
    /**
     * Ignore any call to roll back the current transaction
     */
    @Override
    public void rollback() 
    {
    }
    
    /**
     * Mark the current transaction so that the only possible
     * outcome of the transaction is for the transaction to be
     * rolled back.
     * @throws IllegalStateException if {@link #isActive()} is false.
     */
    @Override
    public void setRollbackOnly() 
    {
        entityTransaction.setRollbackOnly();;
    }
}
