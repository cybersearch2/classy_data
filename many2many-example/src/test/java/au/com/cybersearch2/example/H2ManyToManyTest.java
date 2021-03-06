package au.com.cybersearch2.example;
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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.junit.After;

import au.com.cybersearch2.classytask.WorkStatus;


/**
 * H2ManyToManyTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
public class H2ManyToManyTest
{
    private H2ManyToManyMain manyToManyMain;

    @Before
    public void setUp() 
    {
         manyToManyMain = new H2ManyToManyMain();
    } 

    @After
    public void tearDown()
    {
        manyToManyMain.close();
    }
    
    @Test 
    public void test_many_to_many_jpa() throws Exception
    {
        manyToManyMain.setUp();
        PostsByUserEntityTask postsByUserEntityTask = new PostsByUserEntityTask(
                manyToManyMain.getUser1().id,
                manyToManyMain.getUser2().id,
                manyToManyMain.getPost1().id,
                manyToManyMain.getPost2().id);

        assertEquals(manyToManyMain.execute(postsByUserEntityTask), WorkStatus.FINISHED);
        manyToManyMain.verifyPostsByUser(postsByUserEntityTask.getPosts());
        UsersByPostTask usersByPostTask= new UsersByPostTask(
                manyToManyMain.getUser1().id,
                manyToManyMain.getUser2().id,
                manyToManyMain.getPost1().id,
                manyToManyMain.getPost2().id);
        assertEquals(manyToManyMain.execute(usersByPostTask), WorkStatus.FINISHED);
        manyToManyMain.verifyUsersByPost(usersByPostTask.getUsersByPost1(), usersByPostTask.getUsersByPost2());
    }

}

