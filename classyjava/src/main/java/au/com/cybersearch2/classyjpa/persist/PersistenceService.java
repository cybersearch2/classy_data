/**
    Copyright (C) 2016  www.cybersearch2.com.au

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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import au.com.cybersearch2.classyjpa.persist.PersistenceContext;

/**
 * PersistenceService
 * @author Andrew Bowley
 * 25 Jan 2016
 */
public abstract class PersistenceService<E> extends PersistenceWorker
{
    public static int MAX_QUEUE_LENGTH = 16;
    
    private BlockingQueue<E> entityQueue;
    private Thread consumeThread;

    /**
     * 
     */
    public PersistenceService(String persistenceUnit, PersistenceContext persistenceContext)
    {
        super(persistenceUnit, persistenceContext);
        entityQueue = new LinkedBlockingQueue<E>(MAX_QUEUE_LENGTH);
        runConsumer();
    }

    public abstract void onEntityReceived(E entity);

    /**
     * Inserts the specified element into the service queue, waiting if necessary
     * for space to become available.
     *
     * @param element the element to add
     * @throws InterruptedException if interrupted while waiting
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    public void put(E element) throws InterruptedException
    {
        entityQueue.put(element);
    }

    public void shutdown()
    {
        consumeThread.interrupt();
    }
    
    private void runConsumer() 
    {
        Runnable comsumeTask = new Runnable()
        {
            @Override
            public void run() 
            {
                while (true)
                {
                    try 
                    {
                        onEntityReceived(entityQueue.take());
                    } 
                    catch (InterruptedException e) 
                    {
                        break;
                    }
                }
            }
        };
        consumeThread = new Thread(comsumeTask, "PersistenceWorker");
        consumeThread.start();
    }

}
