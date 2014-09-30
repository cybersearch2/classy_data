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

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

import au.com.cybersearch2.classyjpa.transaction.TransactionCallable;

import com.j256.ormlite.support.ConnectionSource;


/**
 * TestDatabaseWorkerTask
 * @author Andrew Bowley
 * 24/06/2014
 */
public abstract class TestDatabaseWork extends DatabaseWork
{
    class TestTransaction implements EntityTransaction
    {
        TransactionCallable callable;
        RuntimeException runtimeExceptionThrown;
        Exception exceptionThrown;
        boolean rollbackOnly;
        
   
        public TestTransaction(TransactionCallable callable)
        {
            this.callable = callable;
        }
        
        @Override
        public void begin() {
            mockTransaction.begin();
        }

        @Override
        public void commit() {
            try
            {
                result = callable.call(null);
            }
            catch (RuntimeException e)
            {
                runtimeExceptionThrown = e;
            }
            catch (Exception e)
            {
                exceptionThrown = e;
            }
            if ((result == null) || (!result))
                mockTransaction.rollback();
            else
                mockTransaction.commit();
            if (runtimeExceptionThrown != null)
                throw new PersistenceException("Pre commit operation failed", runtimeExceptionThrown);
            if (exceptionThrown != null)
                throw new PersistenceException("Pre commit operation failed", exceptionThrown);

            
        }

        @Override
        public boolean getRollbackOnly() {
            return rollbackOnly;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public void rollback() {
            mockTransaction.rollback();
        }

        @Override
        public void setRollbackOnly() {
            rollbackOnly = true;
        }
        
    }
    
    TransactionCallable processFilesCallable;
    EntityTransaction mockTransaction;
    
    public TestDatabaseWork(ConnectionSource connectionSource)
    {
        super(connectionSource);
    }

    public void setMockTransaction(EntityTransaction mockTransaction)
    {
        this.mockTransaction = mockTransaction;
    }
    
    public EntityTransaction getMockTransaction()
    {
        return mockTransaction;
    }
    
    public TransactionCallable getProcessFilesCallable()
    {
        return processFilesCallable;
    }
    
    @Override
    protected EntityTransaction getTransaction(ConnectionSource connectionSource, TransactionCallable processFilesCallable)
    {
        this.processFilesCallable = processFilesCallable;
        return new TestTransaction(processFilesCallable);
    }
    

}
