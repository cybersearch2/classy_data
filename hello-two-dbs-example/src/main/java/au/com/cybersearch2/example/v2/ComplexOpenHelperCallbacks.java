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

import javax.persistence.Query;

import com.j256.ormlite.support.ConnectionSource;

import au.com.cybersearch2.classydb.OpenHelperCallbacksImpl;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceTask;
import au.com.cybersearch2.example.QueryForAllGenerator;

/**
 * ComplexOpenHelperCallbacks
 * @author Andrew Bowley
 * 24 Nov 2014
 */
public class ComplexOpenHelperCallbacks extends OpenHelperCallbacksImpl 
{
    /** Named query to find all ComplexData objects */
    static public final String ALL_COMPLEX_DATA = "all_complex_data";

	public ComplexOpenHelperCallbacks() 
	{
		super(HelloTwoDbsMain.PU_NAME2);
		
	}

    /**
     * What to do when your database needs to be created. Usually this entails creating the tables and loading any
     * initial data.
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be created.
     */
    @Override
    public void onCreate(ConnectionSource connectionSource) 
    {
        super.onCreate(connectionSource);
    	doWork(connectionSource, getPopulateTask1());
    }

	protected PersistenceTask getPopulateTask1()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
				long millis = System.currentTimeMillis();
				// create some entries in the onCreate
				ComplexData complex1 = new ComplexData(millis, QuoteSource.getQuote());
				entityManager.persist(complex1);
				ComplexData complex2 = new ComplexData(millis + 1, QuoteSource.getQuote());
				entityManager.persist(complex2);
				//logMessage(puName, "Created 2 new ComplexData entries: " + millis);
			}};
	}

    /**
     * What to do when your database needs to be updated. This could mean careful migration of old data to new data.
     * Maybe adding or deleting database columns, etc..
     * 
     * <p>
     * <b>NOTE:</b> You should use the connectionSource argument that is passed into this method call or the one
     * returned by getConnectionSource(). If you use your own, a recursive call or other unexpected results may result.
     * </p>
     * 
     * @param connectionSource
     *            To use get connections to the database to be updated.
     * @param oldVersion
     *            The version of the current database so we can know what to do to the database.
     * @param newVersion
     *            The version that we are upgrading the database to.
     */
    @Override
    public void onUpgrade(
            ConnectionSource connectionSource, int oldVersion, int newVersion) 
    {
    	super.onUpgrade(connectionSource, oldVersion, newVersion);
        QueryForAllGenerator allComplexDataObjects = 
                new QueryForAllGenerator(persistenceAdmin);
        persistenceAdmin.addNamedQuery(ComplexData.class, ALL_COMPLEX_DATA, allComplexDataObjects);
    	doWork(connectionSource, getUpgradeTask1());
    }
    
    
	@SuppressWarnings("unchecked")
	protected PersistenceTask getUpgradeTask1()
	{
		return new PersistenceTask(){

			@Override
			public void doTask(EntityManagerLite entityManager) 
			{
		    	// Query for all of the data objects in the database
		        Query query = entityManager.createNamedQuery(ALL_COMPLEX_DATA);
		        List<ComplexData> list = (List<ComplexData>) query.getResultList();
		
				// If we already have items in the database
				for (ComplexData complex : list) 
				{
					complex.setQuote(QuoteSource.getQuote());
					entityManager.merge(complex);
				}
		    }
		};
	}

}
