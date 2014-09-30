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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.PersistenceException;
import javax.persistence.Query;

import au.com.cybersearch2.classybean.BeanException;
import au.com.cybersearch2.classybean.BeanUtil;
import au.com.cybersearch2.classybean.BeanUtil.DataPair;
import au.com.cybersearch2.classyjpa.EntityManagerLite;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;

/**
 * NodeFinder
 * Abstract persistence work overrides doInBackground() to perform find node by primary key.
 * Sub class to override onPostExecute() and onRollback() 
 * @author Andrew Bowley
 * 05/09/2014
 */
public abstract class NodeFinder implements PersistenceWork
{
    /** Primary key to search on */
    protected int nodeId;
    /** Node returned by search is a graph fragment containing all found node ancestors and immediate children */
    protected Node node;
    
    /**
     * Create NodeFinder object
     * @param nodeId Primary key to search on
     */
    public NodeFinder(int nodeId)
    {
        this.nodeId = nodeId;
    }

    /**
     * Returns graph fragment containing all found node ancestors and immediate children
     * @return Node
     */
    Node getNode()
    {
        return node;
    }
    
    /**
     * Find node by primary key on background thread
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doInBackground(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @Override
    public void doInBackground(EntityManagerLite entityManager) 
    {
        NodeEntity nodeEntity = entityManager.find(NodeEntity.class, nodeId);

        node = Node.marshall(nodeEntity);
        // Now get properties of requested node
        Query query = entityManager.createNamedQuery(Node.NODE_BY_PRIMARY_KEY_QUERY + node.getModel()); //
        query.setParameter("node_id", nodeId);
        try
        {
            Object result = query.getSingleResult();
            Set<DataPair> dataSet = BeanUtil.getDataPairSet(result);
            Map<String,Object> propertiesMap = new HashMap<String,Object>(dataSet.size());
            for (DataPair dataPair: dataSet)
                propertiesMap.put(dataPair.getKey(), dataPair.getValue());
            node.setProperties((propertiesMap));
        }
        catch (BeanException e)
        {
            throw new PersistenceException(e.getMessage(), e);
        }
    }
}
