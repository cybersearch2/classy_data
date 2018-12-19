/**
    Copyright (C) 2018  www.cybersearch2.com.au

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
package au.com.cybersearch2.node;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * Join table which links nodes to their parents.
 * 
 * <p>
 * For more information about foreign objects, see the <a href="http://ormlite.com/docs/foreign" >online docs</a>
 * </p>
 */
@Entity(name="child_nodes")
public class ParentChild {
    /** Column name in join table for user foreign key */
	public final static String CHILD_ID_FIELD_NAME = "_child_id";
    /** Column name in join table for post foreign key */
	public final static String PARENT_ID_FIELD_NAME = "_parent_id";

	/**
	 * This id is generated by the database and set on the object when it is passed to the create method. An id is
	 * needed in case we need to update or delete this object in the future.
	 */
    @Id @GeneratedValue
	int id;

	/** This is a foreign object which just stores the id from the User object in this table. */
    @OneToOne
    @JoinColumn(name=CHILD_ID_FIELD_NAME, referencedColumnName="_id")
    NodeBean child;

	/** This is a foreign object which just stores the id from the Post object in this table. */
    @OneToOne
    @JoinColumn(name=PARENT_ID_FIELD_NAME, referencedColumnName="_id")
    NodeBean parent;

    /**
     * NodeParent default constructor for ormlite
     */
    ParentChild()
    {
    }
    
    public ParentChild(NodeBean parent, NodeBean child)
    {
    	this.child = child;
    	this.parent = parent;
    }
}
