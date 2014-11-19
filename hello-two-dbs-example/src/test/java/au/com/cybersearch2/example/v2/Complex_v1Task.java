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

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.example.ComplexData;

/**
 * Simple_v1Task
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class Complex_v1Task implements PersistenceWork
{
    protected StringBuilder sb;
    List<ComplexData> list;
    
    /**
     * Create SimpleTask object
     */
    public Complex_v1Task()
    {
        sb = new StringBuilder(); 
    }

    public String getMessage()
    {
    	return sb.toString();
    }
    
    public List<ComplexData> getList()
    {
        return list;
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doInBackground(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doInBackground(EntityManagerLite entityManager) 
    {
    	sb.setLength(0);
		// Query for all of the data objects in the database
        Query query = entityManager.createNamedQuery(HelloTwoDbsMain.ALL_COMPLEX_DATA_v1);
        list = (List<ComplexData>) query.getResultList();

		sb.append("Got ").append(list.size()).append(" ComplexData entries in ").append("\n");
		sb.append("------------------------------------------\n");

		// If we already have items in the database
		int objC = 0;
		for (ComplexData complex : list) 
		{
			sb.append("[").append(objC).append("] = ").append(complex).append("\n");
			objC++;
		}
		sb.append("------------------------------------------\n");
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
     */
    @Override
    public void onPostExecute(boolean success) 
    {
        if (!success)
            throw new IllegalStateException(HelloTwoDbsMain.PU_NAME2_v1 + " task failed. Check console for error details.");
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
     */
    @Override
    public void onRollback(Throwable rollbackException) 
    {
        throw new IllegalStateException(HelloTwoDbsMain.PU_NAME2_v1 + " task failed. Check console for stack trace.", rollbackException);
   }
}
