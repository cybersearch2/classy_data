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
package au.com.cybersearch2.classyjpa;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import au.com.cybersearch2.classyapp.TestClassyApplication;
import au.com.cybersearch2.classyapp.TestClassyApplicationModule;
import au.com.cybersearch2.classyfy.data.Model;
import au.com.cybersearch2.classynode.Node;
import au.com.cybersearch2.classynode.NodeEntity;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyinject.DI;
import au.com.cybersearch2.classyjpa.entity.EntityManagerDelegate;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainer;
import au.com.cybersearch2.classyjpa.entity.PersistenceDao;
import au.com.cybersearch2.classyjpa.entity.TestPersistenceWork;
import au.com.cybersearch2.classyjpa.entity.TestPersistenceWork.Callable;
import au.com.cybersearch2.classyjpa.persist.PersistenceAdmin;
import au.com.cybersearch2.classyjpa.persist.Persistence;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classyjpa.persist.TestPersistenceFactory;
import au.com.cybersearch2.classytask.Executable;
import au.com.cybersearch2.classytask.WorkStatus;
import au.com.cybersearch2.classyutil.Transcript;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import dagger.Module;

/**
 * JpaIntegrationTest
 * @author Andrew Bowley
 * 13/06/2014
 */
public class JpaIntegrationTest
{
    @Module(includes = TestClassyApplicationModule.class)
    static class JpaIntegrationTestModule implements ApplicationModule
    {
    }
    
    private static final String TOP_TITLE = "Cybersearch2 Records";
    protected PersistenceContainer testContainer;
    protected Transcript transcript;
    protected TestPersistenceFactory testPersistenceFactory; 
    
    protected PersistenceContext persistenceContext;
    
    @Before
    public void setup() throws Exception
    {
	    createObjectGraph();
        persistenceContext = new PersistenceContext();
	    persistenceContext.initializeAllDatabases();
        Persistence persistence = persistenceContext.getPersistenceUnit(TestClassyApplication.PU_NAME);
        testPersistenceFactory = new TestPersistenceFactory(persistence);
        transcript = new Transcript();
        testContainer = new PersistenceContainer(TestClassyApplication.PU_NAME);
    }

    @After
    public void shutdown()
    {
        testPersistenceFactory.onShutdown();
    }
 
	/**
	 * Set up dependency injection, which creates an ObjectGraph from a HelloTwoDbsModule configuration object.
	 * Override to run with different database and/or platform. 
	 */
	protected void createObjectGraph()
	{
        DI dependencyInjection = new DI(new JpaIntegrationTestModule());
        dependencyInjection.validate();
	}

    @Test
    public void test_PersistenceEnvironment()
    {
        assertThat(testPersistenceFactory).isNotNull();
        Persistence persistence = testPersistenceFactory.getPersistenceEnvironment();
        assertThat(persistence).isNotNull();
        PersistenceAdmin persistenceAdmin = persistence.getPersistenceAdmin();
        assertThat(persistenceAdmin).isNotNull();
        EntityManagerLiteFactory entityManagerFactory = persistenceAdmin.getEntityManagerFactory();
        assertThat(entityManagerFactory).isNotNull();
        EntityManagerLite em = entityManagerFactory.createEntityManager();
        assertThat(em).isNotNull();
        EntityTransaction transaction = em.getTransaction();
        assertThat(transaction).isNotNull();
        transaction.begin();
        em.close();
        
    }

    @Test 
    public void test_find_node() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);

        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) 
            {
                entityHolder[0] = entityManager.find(RecordCategory.class, new Integer(1));
                transcript.add("entityManager.find() completed");
                return true;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "entityManager.find() completed", "onPostExecute true");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }
   
    @Test 
    public void test_find_entity() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);

        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) 
            {
                entityHolder[0] = entityManager.find(RecordCategory.class, new Integer(1));
                transcript.add("entityManager.find() completed");
                return true;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "entityManager.find() completed", "onPostExecute true");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
     }
         
    @Test 
    public void test_find_entity_by_query() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);

        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
                EntityManagerDelegate delegate = (EntityManagerDelegate)entityManager.getDelegate();
                @SuppressWarnings("unchecked")
                PersistenceDao<RecordCategory, Integer> recordCategoryDao = 
                        (PersistenceDao<RecordCategory, Integer>) delegate.getDaoForClass(RecordCategory.class);
                QueryBuilder<RecordCategory, Integer> statementBuilder = recordCategoryDao.queryBuilder();
                SelectArg selectArg = new SelectArg();
                // build a query with the WHERE clause set to 'name = ?'
                statementBuilder.where().eq("node_id", selectArg);
                PreparedQuery<RecordCategory> preparedQuery = statementBuilder.prepare();
                // now we can set the select arg (?) and run the query
                selectArg.setValue(new Integer(2));
                List<RecordCategory> results = recordCategoryDao.query(preparedQuery);
                entityHolder[0] = results.get(0);
                transcript.add("entityManager.find() completed");
                return true;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "entityManager.find() completed", "onPostExecute true");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }
       
    @Test 
    public void test_find_entity_by_named_query() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        final RecordCategory[] entityHolder = new RecordCategory[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
                Query query = entityManager.createNamedQuery(TestClassyApplication.CATEGORY_BY_NODE_ID);
                query.setParameter("node_id", new Integer(2));
                entityHolder[0] = (RecordCategory) query.getSingleResult();
                transcript.add("entityManager.query() completed");
                return entityHolder[0].get_node_id() == 2;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        transcript.assertEventsSoFar("background task", "entityManager.query() completed", "onPostExecute true");
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
    }

    @Test 
    public void test_findNodeById() throws InterruptedException
    {
        TestPersistenceWork persistenceWork = new TestPersistenceWork(transcript);
        final NodeEntity[] entityHolder = new NodeEntity[1];
        Callable doInBackgroundCallback = new Callable(){

            @Override
            public Boolean call(EntityManagerLite entityManager) throws Exception
            {
                entityHolder[0] = entityManager.find(NodeEntity.class, 34);
                transcript.add("entityManager.find() completed");
                return entityHolder[0].get_id() == 34;
            }};
        persistenceWork.setCallable(doInBackgroundCallback);
        Executable exe = testContainer.executeTask(persistenceWork);
        exe.waitForTask();
        assertThat(exe.getStatus()).isEqualTo(WorkStatus.FINISHED);
        Node node = Node.marshall(entityHolder[0]);
        assertThat(node).isNotNull();
        assertThat(node.get_id()).isEqualTo(34);
        assertThat(node.get_parent_id()).isEqualTo(1);
        assertThat(node.getChildren()).isNotNull();
        assertThat(node.getChildren().size()).isEqualTo(8);
        assertThat(node.getParent()).isNotNull();
        assertThat(node.getParent() instanceof Node).isTrue();
        assertThat(node.getLevel()).isEqualTo(2);
        Node parent = (Node)node.getParent();
        assertThat(parent.get_id()).isEqualTo(1);
        assertThat(parent.get_parent_id()).isEqualTo(0);
        assertThat(parent.getChildren().size()).isEqualTo(7);
        assertThat(parent.getChildren().contains(node)).isTrue();
        assertThat(parent.getTitle()).isEqualTo(TOP_TITLE);
        assertThat(parent.getModel()).isEqualTo(Model.recordCategory.ordinal());
        assertThat(parent.getLevel()).isEqualTo(1);
        Node root = (Node)parent.getParent();
        assertThat(root.getChildren().size()).isEqualTo(1);
        assertThat(root.getChildren().contains(parent)).isTrue();
        assertThat(root.getModel()).isEqualTo(Model.root.ordinal());
        assertThat(root.get_id()).isEqualTo(0);
        assertThat(root.get_parent_id()).isEqualTo(0);
        assertThat(root.getLevel()).isEqualTo(0);
    }
}
