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

/**
 * TransactionInfo
 * Java bean containing transaction context of persistence container. Essential for managing and reporting rollback. 
 * @author Andrew Bowley
 * 15/05/2014
 */
public class TransactionInfo
{
    /** Exception responsible for rollback if one is thrown while transaction is active */ 
    private Throwable rollbackException;
    /** Flag for user transaction. If true, then entityTransaction and userTransaction will be the same object */ 
    private boolean isUserTransaction;
    /** The enclosing entity transaction */
    private EntityTransaction entityTransaction;
    
    /**
     * Set enclosing entity transaction
     * @param entityTransaction EntityTransaction
     */
    public void setEntityTransaction(EntityTransaction entityTransaction)
    {
        this.entityTransaction = entityTransaction;
    }
    
    /**
     * Returns enclosing entity transaction
     * @return EntityTransaction
     */
    public EntityTransaction getTransaction()
    {
        return entityTransaction;
    }
    
    /**
     * Set Exception responsible for rollback if one is thrown while transaction is active
     * @param rollbackException Throwable
     */
    public void setRollbackException(Throwable rollbackException) 
    {
        this.rollbackException = rollbackException;
    }

    /**
     * Returns Exception responsible for rollback if one is thrown while transaction is active
     * @return Throwable
     */
    public Throwable getRollbackException() 
    {
        return rollbackException;
    }

    /**
     * Returns true if user transaction
     * @return boolean
     */
    public boolean isUserTransaction() 
    {
        return isUserTransaction;
    }

    /**
     * Set user transaction flag
     * @param isUserTransaction boolean
     */
    public void setUserTransaction(boolean isUserTransaction) 
    {
        this.isUserTransaction = isUserTransaction;
    }

}
