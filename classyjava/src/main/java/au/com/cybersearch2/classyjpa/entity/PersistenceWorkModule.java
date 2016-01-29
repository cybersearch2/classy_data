/**
    Copyright (C) 2015  www.cybersearch2.com.au

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

import javax.inject.Singleton;

import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.JavaThreadMessenger;
import au.com.cybersearch2.classytask.TaskManager;
import au.com.cybersearch2.classytask.TaskMessenger;
import au.com.cybersearch2.classytask.TaskRunner;
import au.com.cybersearch2.classytask.ThreadHelper;
import dagger.Module;
import dagger.Provides;

/**
 * PersistenceWorkModule
 * @author Andrew Bowley
 * 8 Jan 2016
 */
@Module
public class PersistenceWorkModule
{
    private String puName;
    private boolean async;
    private boolean isUserTransactions;
    private PersistenceWork persistenceWork;
    
    public PersistenceWorkModule(String puName, boolean async, PersistenceWork persistenceWork)
    {
        this.puName = puName;
        this.async = async;
        this.persistenceWork = persistenceWork;
    }
    
    @Provides @Singleton PersistenceContainer providePersistenceContainer(PersistenceContext persistenceContext)
    {
        return new PersistenceContainer(persistenceContext, puName, async);
    }
  
    @Provides TaskMessenger provideTaskMessenger()
    {
        return new JavaThreadMessenger();
    }
    
    @Provides Executable provideExecutable(
            PersistenceContainer persistenceContainer,
            ThreadHelper threadHelper,
            TaskManager taskManager, 
            TaskMessenger taskMessenger)
    {
        persistenceContainer.setUserTransactionMode(isUserTransactions);
        JavaPersistenceContext jpaContext = persistenceContainer.getPersistenceTask(persistenceWork);
        if (!async)
            return jpaContext.executeInProcess();
        else
        {
            TaskBase  task = new TaskBase(jpaContext, threadHelper);
            TaskRunner taskRunner = new TaskRunner(taskManager, taskMessenger);
            taskRunner.execute(task);
            return task.getExecutable();
        }
        
    }
    
    /**
     * Set user transaction mode. The transaction is accessed by calling EntityManager getTransaction() method.
     * @param isUserTransactions boolean
     */
    public void setUserTransactions(boolean isUserTransactions)
    {
        this.isUserTransactions = isUserTransactions;
    }

    public PersistenceWork getPersistenceWork()
    {
        return persistenceWork;
    }
}
