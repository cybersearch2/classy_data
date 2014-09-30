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
// Original copyright - code mostly unchanged except for promotion to separate class
/*
 * Copyright (C) 2008 The Android Open Source Project, Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.com.cybersearch2.classytask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import au.com.cybersearch2.classyinject.DI;

/**
 * WorkerRunnable
 * Worker task implementing java.util.concurrent.Callable
 * @author Romain Guy, Andrew Bowley
 * 25/04/2014
 */
public class WorkerRunnable<Result> implements Callable<Result>
{
    /** The task to be invoked by executing call() method */
    protected Callable<Result> task;
    /** Exception thrown when the worker task fails due to a uncaught RuntimeException */
    protected ExecutionException executionException;
    /** Flag set when task invoked. Used in logic to handle cancel request */
    private final AtomicBoolean taskInvoked = new AtomicBoolean();
    /** System adapter object */
    @Inject ThreadHelper threadHelper;

    /**
     * Create a WorkerRunnable object
     * @param task Invoked by executing call() method
     */
    public WorkerRunnable(Callable<Result> task)
    {
        this.task = task;
        DI.inject(this);
    }

    /**
     * Execute task
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Result call() throws Exception 
    {
        taskInvoked.set(true); // Set flag so FutureTask done() knows whether to it has to post results
        threadHelper.setBackgroundPriority();
        return task.call();
    }

    /**
     * Returns flag set true when task starts execution
     * @return boolean
     */
    public boolean wasTaskInvoked() 
    {
        final boolean wasTaskInvoked = taskInvoked.get();
        return wasTaskInvoked;
    }
 
    /**
     * Set Exception thrown when the worker task fails due to a uncaught RuntimeException  
     * @param executionException ExecutionException
     */
    public void setExecutionException(ExecutionException executionException)
    {
        this.executionException = executionException;
    }

    /**
     * Returns Exception thrown when the worker task fails due to a uncaught RuntimeException
     * @return ExecutionException
     */
    public ExecutionException getExecutionException()
    {
        return executionException;
    }
}
