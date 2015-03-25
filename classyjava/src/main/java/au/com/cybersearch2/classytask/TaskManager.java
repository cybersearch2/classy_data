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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TaskManager
 * Manages the background task thread pool
 * @author Romain Guy
 * 25/04/2014
 */
public class TaskManager 
{
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 10;
    private static final int KEEP_ALIVE = 10;

    private final ThreadPoolExecutor executor;

    /**
     * Create TaskManager object
     */
    public TaskManager()
    {
        executor = 
                new ThreadPoolExecutor(CORE_POOL_SIZE,
                                       MAXIMUM_POOL_SIZE, 
                                       KEEP_ALIVE, 
                                       TimeUnit.SECONDS, 
                                       new LinkedBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE), 
                                       new ThreadFactory() 
                                       {
                                           private final AtomicInteger count = new AtomicInteger(1);

                                           public Thread newThread(Runnable r) 
                                           {
                                               return new Thread(r, "Worker #" + count.getAndIncrement());
                                           }
                                       },
                                       new ThreadPoolExecutor.CallerRunsPolicy());
    }
    
    /**
     * Returns thread pool executor. Pool parameters statically defined in this class - CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE
     * @return ThreadPoolExecutor
     */
    public ThreadPoolExecutor getExecutor() 
    {
        return executor;
    }
}
