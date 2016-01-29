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
package au.com.cybersearch2.example.v2;

import java.util.List;
import java.util.Random;

import javax.persistence.Query;

//import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
//import au.com.cybersearch2.classyjpa.entity.PersistenceDao;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;

/**
 * UsersByPostTask
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class SimpleTask implements PersistenceTask
{
	public static String NAMES[] = 
	{
		"Henry",
		"Sally",
		"Tim"
	};
	
    protected String context;
    protected StringBuilder sb;
    
    /**
     * Create SimpleTask object
     */
    public SimpleTask(String context)
    {
    	this.context = context;
        sb = new StringBuilder(); 
    }

    public String getMessage()
    {
    	return sb.toString();
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doTask(EntityManagerLite entityManager) 
    {
    	sb.setLength(0);
		// Query for all of the data objects in the database
    	/* Comments starting with "///" are alternate EntityManagerDelegate approach, 
    	 * but you will need to handle lots of places where checked SQLException is thrown. */
    	/// EntityManagerDelegate delegate = (EntityManagerDelegate) entityManager.getDelegate();
    	/// PersistenceDao<SimpleData, Integer> simpleDao = (PersistenceDao<SimpleData, Integer>) delegate.getDaoForClass(SimpleData.class);
		/// List<SimpleData> list = simpleDao.queryForAll();
        Query query = entityManager.createNamedQuery(HelloTwoDbsMain.ALL_SIMPLE_DATA2);
        List<SimpleData> list = (List<SimpleData>) query.getResultList();

		sb.append("Got ").append(list.size()).append(" SimpleData entries in ").append(context).append("\n");
		sb.append("------------------------------------------\n");

		// If we already have items in the database
		int objC = 0;
		for (SimpleData simple : list) 
		{
			sb.append("[").append(objC).append("] = ").append(simple).append("\n");
			objC++;
		}
		sb.append("------------------------------------------\n");
		for (SimpleData simple : list) 
		{
			/// simpleDao.delete(simple);
			// Note objects returned from queries are not managed, so need to call merge() on them
	        entityManager.merge(simple);
	        entityManager.remove(simple);
			sb.append("Deleted SimpleData id ").append(simple.id).append("\n");
			HelloTwoDbsMain.logInfo(HelloTwoDbsMain.PU_NAME1, "Deleting SimpleData(" + simple.id + ")");
		}

		int createNum;
		do 
		{
			createNum = new Random().nextInt(3) + 1;
		} while (createNum == list.size());
		for (int i = 0; i < createNum; i++) 
		{
			// Create a new simple object
			long millis = System.currentTimeMillis();
			SimpleData simple = new SimpleData(NAMES[i], millis, QuoteSource.getQuote());
			// store it in the database
			/// simpleDao.create(simple);
			entityManager.persist(simple);
			HelloTwoDbsMain.logInfo(HelloTwoDbsMain.PU_NAME1, "Created SimpleData(" + millis + ")");
			// output it
			sb.append("------------------------------------------\n");
			sb.append("Created SimpleData entry #").append(i + 1).append(":\n");
			sb.append(simple).append("\n");
			// Introduce a delay of more than 1 millisecond to get new "millis" value
			try 
			{
				Thread.sleep(5);
			} 
			catch (InterruptedException e) 
			{
				break;
			}
		}
    }
}
