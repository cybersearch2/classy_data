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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import au.com.cybersearch2.classyfy.data.Model;
import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

/**
 * NodeTest
 * @author Andrew Bowley
 * 10/09/2014
 */
public class NodeTest
{
    final int NODE_ID = 123;
    final int CHILD_ID = 124;
    final int PARENT_NODE_ID = 97;
    final int NODE_LEVEL = 1;
    final String NODE_NAME = "Information_Technology";
    final String NODE_TITLE = "Information Technology";
    final String CHILD_NAME = "Mobile_Plans";
    final String CHILD_TITLE = "Mobile Plans";
    
    @Test
    public void test_rootNodeNewInstance()
    {
        Node node = Node.rootNodeNewInstance();
        assertThat(Model.values()[node.getModel()]).isEqualTo(Model.root);
        assertThat(node.getParent()).isEqualTo((NodeEntity)node);
        assertThat(node.getLevel()).isEqualTo(0);
        assertThat(node.get_id()).isEqualTo(0);
        assertThat(node.get_parent_id()).isEqualTo(0);
    }
    
    @Test
    public void test_NodeEntity_constructor_null_parent()
    {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordCategory.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity parent = new NodeEntity();
        parent.setModel(Model.recordCategory.ordinal());
        parent.set_id(PARENT_NODE_ID);
        nodeEntity.setParent(parent);
        NodeEntity child = new NodeEntity();
        child.setModel(Model.recordFolder.ordinal());
        child.set_id(CHILD_ID);
        child.setName(CHILD_NAME);
        child.setTitle(CHILD_TITLE);
        List<NodeEntity> childList = new ArrayList<NodeEntity>();
        childList.add(child);
        childList.add(Node.rootNodeNewInstance()); // This one should be ignored
        nodeEntity.set_children(childList);
        Node node = new Node(nodeEntity, null);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(node.getName()).isEqualTo(NODE_NAME);
        assertThat(node.getTitle()).isEqualTo(NODE_TITLE);
        assertThat(node.get_id()).isEqualTo(NODE_ID);
        assertThat(node.get_parent_id()).isEqualTo(0);
        assertThat(node.getParent().getModel()).isEqualTo(Model.root.ordinal());
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().size()).isEqualTo(0);
        assertThat(node.getParent().getLevel()).isEqualTo(NODE_LEVEL - 1);
        List<Node> children = node.getChildren();
        assertThat(children.size()).isEqualTo(1);
        Node childNode = children.get(0);
        assertThat(childNode.get_id()).isEqualTo(CHILD_ID);
        assertThat(childNode.getModel()).isEqualTo(Model.recordFolder.ordinal());
        assertThat(childNode.getName()).isEqualTo(CHILD_NAME);
        assertThat(childNode.getTitle()).isEqualTo(CHILD_TITLE);
        assertThat(childNode.getParent()).isEqualTo(node);
    }
    
    @Test
    public void test_NodeEntity_constructor()
    {
        Node parent = mock(Node.class);
        when(parent.get_id()).thenReturn(PARENT_NODE_ID);
        List<Node> childList = new ArrayList<Node>();
        when(parent.getChildren()).thenReturn(childList);
        when(parent.getLevel()).thenReturn(NODE_LEVEL - 1);
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordCategory.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity entityParent = new NodeEntity();
        entityParent.setModel(Model.recordCategory.ordinal());
        entityParent.set_id(PARENT_NODE_ID);
        nodeEntity.setParent(parent);
        Node node = new Node(nodeEntity, parent);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(node.getName()).isEqualTo(NODE_NAME);
        assertThat(node.getTitle()).isEqualTo(NODE_TITLE);
        assertThat(node.get_id()).isEqualTo(NODE_ID);
        assertThat(node.get_parent_id()).isEqualTo(PARENT_NODE_ID);
        assertThat(node.getParent()).isEqualTo(parent);
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().size()).isEqualTo(0);
        assertThat(childList.get(0)).isEqualTo(node);
    }

    @Test
    public void test_NodeEntity_constructor_existing_child()
    {
        Node parent = mock(Node.class);
        when(parent.get_id()).thenReturn(PARENT_NODE_ID);
        List<Node> childList = new ArrayList<Node>();
        Node childNode = mock(Node.class);
        when(childNode.get_id()).thenReturn(NODE_ID);
        childList.add(childNode);
        when(parent.getChildren()).thenReturn(childList);
        when(parent.getLevel()).thenReturn(NODE_LEVEL - 1);
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordCategory.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity entityParent = new NodeEntity();
        entityParent.setModel(Model.recordCategory.ordinal());
        entityParent.set_id(PARENT_NODE_ID);
        nodeEntity.setParent(parent);
        Node node = new Node(nodeEntity, parent);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(node.getName()).isEqualTo(NODE_NAME);
        assertThat(node.getTitle()).isEqualTo(NODE_TITLE);
        assertThat(node.get_id()).isEqualTo(NODE_ID);
        assertThat(node.get_parent_id()).isEqualTo(PARENT_NODE_ID);
        assertThat(node.getParent()).isEqualTo(parent);
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getProperties()).isNotNull();
        assertThat(node.getProperties().size()).isEqualTo(0);
        assertThat(childList.get(0)).isEqualTo(node);
    }
    
    @Test
    public void test_NodeEntity_constructor_null_entity()
    {
        try
        {
            new Node(null, null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter nodeEntity is null");
        }
    }
    
    @Test
    public void test_model_constructor()
    {
        Node parent = mock(Node.class);
        List<Node> childList = new ArrayList<Node>();
        when(parent.getChildren()).thenReturn(childList);
        when(parent.getLevel()).thenReturn(NODE_LEVEL - 1);
        Node node = new Node(Model.recordCategory.ordinal(), parent);
        assertThat(childList.size()).isEqualTo(1);
        assertThat(childList.get(0)).isEqualTo(node);
        assertThat(node.getLevel()).isEqualTo(NODE_LEVEL);
        assertThat(node.getModel()).isEqualTo(Model.recordCategory.ordinal());
    }
    
    @Test
    public void test_get_property()
    {
        Node node = Node.rootNodeNewInstance();
        assertThat(Node.getProperty(node, "key", null)).isEqualTo("null");
        assertThat(Node.getProperty(node, "key", "value")).isEqualTo("'value'");
        node.setProperties(new HashMap<String,Object>());
        assertThat(Node.getProperty(node, "key", null)).isEqualTo("null");
        assertThat(Node.getProperty(node, "key", "value")).isEqualTo("'value'");
        node.getProperties().put("key", "superlative");
        assertThat(Node.getProperty(node, "key", "value")).isEqualTo("'superlative'");
        assertThat(Node.getProperty(node, "key", null)).isEqualTo("'superlative'");
        node.getProperties().put("level", Integer.valueOf(NODE_LEVEL));
        assertThat(Node.getProperty(node, "level", null)).isEqualTo("'" + NODE_LEVEL + "'");
    }
    
    @Test
    public void test_marshall()
    {
        NodeEntity nodeEntity = new NodeEntity();
        nodeEntity.setModel(Model.recordFolder.ordinal());
        nodeEntity.set_id(NODE_ID);
        nodeEntity.set_parent_id(PARENT_NODE_ID);
        nodeEntity.setName(NODE_NAME);
        nodeEntity.setTitle(NODE_TITLE);
        nodeEntity.setLevel(NODE_LEVEL);
        NodeEntity parent = new NodeEntity();
        parent.setModel(Model.recordCategory.ordinal());
        parent.set_id(PARENT_NODE_ID);
        parent.setParent(Node.rootNodeNewInstance());
        NodeEntity child1 = new NodeEntity();
        child1.setModel(Model.recordFolder.ordinal());
        child1.set_id(CHILD_ID + 1);
        child1.setName(CHILD_NAME);
        child1.setTitle(CHILD_TITLE);
        List<NodeEntity> childList1 = new ArrayList<NodeEntity>();
        childList1.add(child1);
        parent.set_children(childList1);
        nodeEntity.setParent(parent);
        NodeEntity child2 = new NodeEntity();
        child2.setModel(Model.recordFolder.ordinal());
        child2.set_id(CHILD_ID + 2);
        child2.setName(CHILD_NAME);
        child2.setTitle(CHILD_TITLE);
        List<NodeEntity> childList2 = new ArrayList<NodeEntity>();
        childList2.add(child2);
        child1.set_children(childList2);
        nodeEntity.setParent(parent);
        NodeEntity child = new NodeEntity();
        child.setModel(Model.recordFolder.ordinal());
        child.set_id(CHILD_ID);
        child.setName(CHILD_NAME);
        child.setTitle(CHILD_TITLE);
        List<NodeEntity> childList = new ArrayList<NodeEntity>();
        childList.add(child);
        nodeEntity.set_children(childList);
        Node node = Node.marshall(nodeEntity);
        assertThat(node.get_id()).isEqualTo(NODE_ID);
        assertThat(node.getModel()).isEqualTo(Model.recordFolder.ordinal());
        assertThat(node.getChildren().get(0).get_id()).isEqualTo(CHILD_ID);
        Node parentNode = (Node)node.getParent();
        assertThat(parentNode.get_id()).isEqualTo(PARENT_NODE_ID);
        assertThat(parentNode.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(parentNode.getChildren().get(0).get_id()).isEqualTo(CHILD_ID + 1);
        assertThat(parentNode.getChildren().get(0).getChildren().size()).isEqualTo(0);
        Node root = (Node)parentNode.getParent();
        assertThat(root.getModel()).isEqualTo(Model.root.ordinal());
    }
}
