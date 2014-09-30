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
package au.com.cybersearch2.example;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * UsersByPostTask
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class UsersByPostTask implements PersistenceWork
{
    User user1;
    User user2;
    Post post1;
    Post post2;
    List<List<User>> resultsList;

    /**
     * Create UsersByPostTask object
     * @param user1_id User 1 primary key
     * @param user2_id User 2 primary key
     * @param post1_id Post 1 primary key
     * @param post2_id Post 2 primary key
     */
    public UsersByPostTask(int user1_id, int user2_id, int post1_id, int post2_id)
    {
        user1 = new User();
        user1.id = user1_id;
        user2 = new User();
        user2.id = user2_id;
        post1 = new Post();
        post1.id = post1_id;
        post2 = new Post();
        post2.id = post2_id;
        resultsList = new ArrayList<List<User>>(2);
    }

    /**
     * Returns result of "users_by_post" query for post 1
     * @return List&lt;User&gt;
     */
    public List<User> getUsersByPost1()
    {
        return resultsList.size() == 2 ? resultsList.get(0) : new ArrayList<User>();
    }
    
    /**
     * Returns result of "users_by_post" query for post 2
     * @return List&lt;User&gt;
     */
    public List<User> getUsersByPost2()
    {
        return resultsList.size() == 2 ? resultsList.get(1) : new ArrayList<User>();
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doInBackground(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doInBackground(EntityManagerLite entityManager) 
    {
        entityManager.merge(user1);
        entityManager.merge(user2);
        entityManager.merge(post1);
        entityManager.merge(post2);
        entityManager.refresh(user1);
        entityManager.refresh(user2);
        entityManager.refresh(post1);
        entityManager.refresh(post2);
        Query query = entityManager.createNamedQuery(ManyToManyMain.USERS_BY_POST);
        query.setParameter(UserPost.POST_ID_FIELD_NAME, post1.id);
        resultsList.add((List<User>) query.getResultList());
        query = entityManager.createNamedQuery(ManyToManyMain.USERS_BY_POST);
        query.setParameter(UserPost.POST_ID_FIELD_NAME, post2.id);
        resultsList.add((List<User>) query.getResultList());
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
     */
    @Override
    public void onPostExecute(boolean success) 
    {
        if (!success)
            throw new IllegalStateException("Query " + ManyToManyMain.USERS_BY_POST + " failed. Check console for error details.");
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
     */
    @Override
    public void onRollback(Throwable rollbackException) 
    {
        throw new IllegalStateException("Query " + ManyToManyMain.USERS_BY_POST + " failed. Check console for stack trace.");
   }
}
