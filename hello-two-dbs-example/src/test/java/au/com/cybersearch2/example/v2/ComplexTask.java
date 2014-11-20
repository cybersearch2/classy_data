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
import au.com.cybersearch2.example.PersistenceTask;

/**
 * ComplexTask
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class ComplexTask implements PersistenceTask
{
    protected String context;
    protected StringBuilder sb;
    
    /**
     * Create ComplexTask object
     */
    public ComplexTask(String context)
    {
    	this.context = context;
        sb = new StringBuilder(); 
    }

    public String getMessage()
    {
    	return sb.toString();
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doInBackground(au.com.cybersearch2.classyjpa.EntityManagerLite)
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
    	/// PersistenceDao<ComplexData, Integer> complexDao = (PersistenceDao<ComplexData, Integer>) delegate.getDaoForClass(ComplexData.class);
		/// List<ComplexData> list = complexDao.queryForAll();
        Query query = entityManager.createNamedQuery(HelloTwoDbsMain.ALL_COMPLEX_DATA);
        List<ComplexData> list = (List<ComplexData>) query.getResultList();

		sb.append("Got ").append(list.size()).append(" ComplexData entries in ").append(context).append("\n");
		sb.append("------------------------------------------\n");

		// If we already have items in the database
		int objC = 0;
		for (ComplexData complex : list) 
		{
			sb.append("[").append(objC).append("] = ").append(complex).append("\n");
			objC++;
		}
		sb.append("------------------------------------------\n");
		for (ComplexData complex : list) 
		{
			/// complexDao.delete(complex);
			// Note objects returned from queries are not managed, so need to call merge() on them
	        entityManager.merge(complex);
	        entityManager.remove(complex);
			sb.append("Deleted ComplexData id ").append(complex.id).append("\n");
			HelloTwoDbsMain.logInfo(HelloTwoDbsMain.PU_NAME2, "Deleting ComplexData(" + complex.id + ")");
		}

		int createNum;
		do 
		{
			createNum = new Random().nextInt(3) + 1;
		} while (createNum == list.size());
		for (int i = 0; i < createNum; i++) 
		{
			// Create a new complex object
			long millis = System.currentTimeMillis();
			ComplexData complex = new ComplexData(millis, QuoteSource.getQuote());
			// store it in the database
			/// complexDao.create(complex);
			entityManager.persist(complex);
			HelloTwoDbsMain.logInfo(HelloTwoDbsMain.PU_NAME2, "Created ComplexData(" + millis + ")");
			// output it
			sb.append("------------------------------------------\n");
			sb.append("Created ComplexData entry #").append(i + 1).append(":\n");
			sb.append(complex).append("\n");
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
