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
- * NodeFinder
 * Abstract persistence work overrides doInBackground() to perform find node by primary key.
 * Sub class to override onPostExecute() and onRollback() 
 * @author Andrew Bowley
 * 05/09/2014
 */
public class NodeFinder implements PersistenceWork
{
    public interface Callback
    {
        /**
         * Handle node found in caller's thread
         * @param node Node returned by search is a graph fragment containing all found node ancestors and immediate children
         */
        void onNodeFound(Node node);
        /**
         * Handle node not found in caller's thread. Check if getRollbackException returns non-null in case of failure.
         * @param nodeId Node identity
         */
        void onNodeNotFound(int nodeId);
        /**
         * Handle rollback
         * @param nodeId Node identity
         * @param rollbackException Exception which caused rollback
         */
        void onRollback(int nodeId, Throwable rollbackException);
    }

    /** Callback to handle completion in caller's thread */
    protected Callback callback;
    
    /** Primary key to search on */
    protected int nodeId;
    /**  Node returned by successful search */
    protected Node node;
    
    /**
     * Create NodeFinder object
     * @param nodeId Primary key to search on
     */
    public NodeFinder(int nodeId, Callback callback)
    {
        this.nodeId = nodeId;
        this.callback = callback;
    }

    /**
     * Find node by primary key on background thread
     * @see au.com.cybersearch2.classyjpa.entity.PersistenceWork#doTask(au.com.cybersearch2.classyjpa.EntityManagerLite)
     */
    @Override
    public void doTask(EntityManagerLite entityManager) 
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

    @Override
    public void onPostExecute(boolean success)
    {
        if (success)
            callback.onNodeFound(node);
        else
            callback.onNodeNotFound(nodeId);
    }

    @Override
    public void onRollback(Throwable rollbackException)
    {
        callback.onRollback(nodeId, rollbackException);
    }
}
