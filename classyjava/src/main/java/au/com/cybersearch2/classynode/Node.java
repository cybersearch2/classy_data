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
package au.com.cybersearch2.classynode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Node
 * Anchor for a component of a graph. 
 * Each node has a model which identifies what the node contains.
 * The top node of a graph has a special model called "root".
 * @author Andrew Bowley
 * 05/09/2014
 */
public class Node extends NodeEntity
{
    private static final long serialVersionUID = -611139781193264363L;
    /** Prefix for name of query to fetch node by primary key */
    public static final String NODE_BY_PRIMARY_KEY_QUERY = "NodeByPrimaryKey";
    /** Node properties defined by model */
    Map<String,Object> properties;
    /** Child nodes list. When this node is fetched from a database, the list may contain place holders with only primary key set */
    List<Node> children;
    
    /**
     * Create root node. Private default constructor prevents creation of node orphans.
     * To begin constructing a graph, create a top node by calling 
     * static method rootNodeNewInstance(). The graph then grows
     * by using the Node(model, parent) constructor.
     */
    private Node()
    {
        model = NodeType.ROOT;
        parent = this;
        level = 0;
    }
 
    /**
     * Construct a Node from its persisted state. 
     * @param nodeEntity The persisted object
     * @param parent The parent on the graph under construction or null if this is the first node of the graph
     */
    public Node(NodeEntity nodeEntity, Node parent)
    {
        if (nodeEntity  == null)
            throw new IllegalArgumentException("Parameter nodeEntity is null");
        _id = nodeEntity._id;
        model = nodeEntity.model;
        name = nodeEntity.name;
        title = nodeEntity.title;
        if (parent != null)
        {
            this.parent = parent;
            //_parent_id = nodeEntity._parent_id;
            _parent_id = parent.get_id();
            // Check if a Node with same id already in children list
            // Replace it, if found, as it only a placeholder
            Node existingNode = null;
            for (Node childNode: parent.getChildren())
            {   
                if (childNode.get_id() == nodeEntity.get_id())
                {
                    existingNode = childNode;
                    break;
                }
            }
            if (existingNode != null)
                parent.getChildren().remove(existingNode);
            // Now add this node to the parent's children list
            parent.getChildren().add(this);
            // Set level to one more than parent's
            level = parent.getLevel() + 1;
        }
        else
        {   // No parent specified, so create a root node to be parent
            Node rootNode = new Node();
            rootNode.set_id(nodeEntity._parent_id);
            rootNode.getChildren().add(this);
            rootNode.setLevel(nodeEntity.level - 1);
            this.parent = rootNode;
            level = nodeEntity.level;
        }
        // Transfer nodeEntity's chidren to this Node 
        if ((nodeEntity._children != null) && (nodeEntity._children.size() > 0))
        {
            for (NodeEntity childEntity: nodeEntity._children)
            {
                if (childEntity._id != childEntity._parent_id) // Never add top node as a child
                    new Node(childEntity, this);
            }
        }
    }
    
    /**
     * Create an empty Node object and attach to an existing graph
     * @param model Ordinal of model enum type
     * @param parent Parent node - use Node.rootNodeNewInstance() for first Node in graph
     */
    public Node(int model, Node parent)
    {
        this.model = model;
        if (parent == null)
            throw new IllegalArgumentException("Parameter parent is null");
        this.parent = parent;
        parent.getChildren().add(this);
        level = parent.getLevel() + 1;
    }
    
    /**
     * Set properties
     * @param properties Map&lt;String, Object*gt;
     */
    public void setProperties(Map<String, Object> properties) 
    {
        this.properties = properties;
    }
   
    /** 
     * Returns children list
     * @return List&lt;Node&gt;
     */
    public List<Node> getChildren()
    {
        if (children == null)
            children = new ArrayList<Node>();
        return children;
    }
    
    /**
     * Returns properties
     * @return Map&lt;String,Object&gt;
     */
    public Map<String,Object> getProperties()
    {
        if (properties == null)
            properties = new HashMap<String,Object>();
        return properties;
    }

    /**
     * Returns root Node object
     * @return Node
     */
    public static Node rootNodeNewInstance()
    {
        return new Node();
    }
   
    /**
     * Returns property value as quote-delimited text for logging perposes 
     * @param node Node from which to extract property
     * @param key Property name
     * @param defaultValue Value to return if property not found 
     * @return String
     */
    public static String getProperty(Node node, String key, String defaultValue)
    {
        Object object = (node.properties == null ? null : node.properties.get(key));
        if (object == null)
        {
            if (defaultValue == null)
                return "null";
            else
                return "'" + defaultValue + "'";
        }
        return "'" + object.toString() + "'";
    }
    
    /**
     * Marshall a nodeEntity object into a graph fragment containing all ancestors and immediate children.
     * Deletes children of other Nodes in graph to prevent triggering lazy fetches and thus potentially fetching the entire graph
     * @param nodeEntity The object to marshall
     * @return Root node of graph
     */
    public static Node marshall(NodeEntity nodeEntity)
    {
        Deque<NodeEntity> nodeEntityDeque = new ArrayDeque<NodeEntity>();
        // Walk up to top node
        while (nodeEntity != null)
        {
             nodeEntityDeque.add(nodeEntity);
             if  (nodeEntity.get_id() == nodeEntity.get_parent_id())// Top of tree indicated by self parent
                break;
             nodeEntity = nodeEntity.getParent();
        }
        // Now build graph fragment
        Node node = Node.rootNodeNewInstance();
        Iterator<NodeEntity> nodeEntityIterator = nodeEntityDeque.descendingIterator();
        while (nodeEntityIterator.hasNext())
        {
            nodeEntity = nodeEntityIterator.next();
            if (nodeEntity.get_children() != null)
                // Children of children must be deleted as they are lazy collections.
                // Access to lazy collections triggers database fetch operations
                for (NodeEntity childEntity: nodeEntity.get_children())
                    childEntity.set_children(null);
            node = new Node(nodeEntity, node);
        }
        return node;
    }
}
