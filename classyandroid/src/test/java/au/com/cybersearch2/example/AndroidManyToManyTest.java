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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import au.com.cybersearch2.classytask.WorkStatus;

/**
 * AndroidManyToManyTest
 * @author Andrew Bowley
 * 23 Sep 2014
 */
@RunWith(RobolectricTestRunner.class)
public class AndroidManyToManyTest
{
    private static AndroidManyToMany androidManyToMany;

    @Before
    public void setUp() 
    {
        if (androidManyToMany == null)
            androidManyToMany = new AndroidManyToMany();
    } 
    
    @Test 
    public void test_many_to_many_jpa() throws Exception
    {
        androidManyToMany.setUp();
        PostsByUserEntityTask postsByUserEntityTask = new PostsByUserEntityTask(
                androidManyToMany.getUser1().id,
                androidManyToMany.getUser2().id,
                androidManyToMany.getPost1().id,
                androidManyToMany.getPost2().id);

        assertEquals(androidManyToMany.execute(postsByUserEntityTask), WorkStatus.FINISHED);
        androidManyToMany.verifyPostsByUser(postsByUserEntityTask.getPosts());
        UsersByPostTask usersByPostTask= new UsersByPostTask(
                androidManyToMany.getUser1().id,
                androidManyToMany.getUser2().id,
                androidManyToMany.getPost1().id,
                androidManyToMany.getPost2().id);
        assertEquals(androidManyToMany.execute(usersByPostTask), WorkStatus.FINISHED);
        androidManyToMany.verifyUsersByPost(usersByPostTask.getUsersByPost1(), usersByPostTask.getUsersByPost2());
    }


}
