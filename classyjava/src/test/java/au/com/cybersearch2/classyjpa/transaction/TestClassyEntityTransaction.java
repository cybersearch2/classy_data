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

import java.sql.SQLException;
import java.util.concurrent.Callable;
import static org.mockito.Mockito.*;

import com.j256.ormlite.support.ConnectionSource;

/**
 * TestClassyEntityTransaction
 * @author Andrew Bowley
 * 18/07/2014
 */
public class TestClassyEntityTransaction extends EntityTransactionImpl
{
    TransactionState mockTransactionState = mock(TransactionState.class);
    SQLException sqlException;
    
    public TestClassyEntityTransaction(ConnectionSource connectionSource)
    {
        super(connectionSource);

    }

    public TestClassyEntityTransaction(ConnectionSource connectionSource,
            TransactionCallable onPreCommit)
    {
        super(connectionSource, onPreCommit);

    }

    public TestClassyEntityTransaction(ConnectionSource connectionSource,
            TransactionCallable onPreCommit, Callable<Boolean> onPostCommit)
    {
        super(connectionSource, onPreCommit, onPostCommit);

    }

    protected TransactionState getTransactionState() throws SQLException 
    {
        if (sqlException != null)
            throw sqlException;
        return mockTransactionState;
    }

}
