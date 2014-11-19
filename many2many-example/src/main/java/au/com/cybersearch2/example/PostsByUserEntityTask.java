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
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * PostsByUserEntityTask
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class PostsByUserEntityTask implements PersistenceWork
{
    User user1;
    User user2;
    Post post1;
    Post post2;
    UserPost user1Post1;
    UserPost user1Post2;
    UserPost user2Post1;
    List<Post> posts;

    /**
     * Create PostsByUserEntityTask object
     * @param user1_id User 1 primary key
     * @param user2_id User 2 primary key
     * @param post1_id Post 1 primary key
     * @param post2_id Post 2 primary key
     */
    public PostsByUserEntityTask(int user1_id, int user2_id, int post1_id, int post2_id)
    {
        user1 = new User();
        user1.id = user1_id;
        user2 = new User();
        user2.id = user2_id;
        post1 = new Post();
        post1.id = post1_id;
        post2 = new Post();
        post2.id = post2_id;
        posts = new ArrayList<Post>();
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
        /*
         * Perform query to get all posts by user1
         */
        Query query = entityManager.createNamedQuery(ManyToManyMain.POSTS_BY_USER);
        query.setParameter(UserPost.USER_ID_FIELD_NAME, user1.id);
        posts.addAll((Collection<? extends Post>) query.getResultList());
     }

    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onPostExecute(boolean)
     */
    @Override
    public void onPostExecute(boolean success) 
    {
        if (!success)
            throw new IllegalStateException("Query " + ManyToManyMain.POSTS_BY_USER + " failed. Check console for error details.");
    }
    
    /**
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#onRollback(java.lang.Throwable)
     */
    @Override
    public void onRollback(Throwable rollbackException) 
    {
        throw new IllegalStateException("Query " + ManyToManyMain.POSTS_BY_USER + " failed. Check console for stack trace.", rollbackException);
    }

    /**
     * Returns result of "posts_by_user" query
     * @return List&lt;Post&gt;
     */
    public List<Post> getPosts()
    {
        return posts;
    }
    

}
