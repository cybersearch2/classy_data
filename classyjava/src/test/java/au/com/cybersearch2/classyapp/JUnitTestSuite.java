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
package au.com.cybersearch2.classyapp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import au.com.cybersearch2.classybean.BeanMapTest;
import au.com.cybersearch2.classybean.BeanUtilTest;
import au.com.cybersearch2.classydb.DatabaseAdminImplTest;
import au.com.cybersearch2.classydb.NativeScriptDatabaseWorkTest;
import au.com.cybersearch2.classydb.SQLiteDatabaseSupportTest;
import au.com.cybersearch2.classydb.SQLiteSupportTest;
import au.com.cybersearch2.classyinject.DI_Test;
import au.com.cybersearch2.classyinject.ObjectGraphManagerTest;
import au.com.cybersearch2.classyjpa.JpaIntegrationTest;
import au.com.cybersearch2.classyjpa.entity.EntityManagerImplTest;
import au.com.cybersearch2.classyjpa.entity.ObjectMonitorTest;
import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperFactoryTest;
import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperTest;
import au.com.cybersearch2.classyjpa.entity.PersistenceContainerTest;
import au.com.cybersearch2.classyjpa.entity.PersistenceDaoTest;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyserTest;
import au.com.cybersearch2.classyjpa.persist.PersistenceConfigTest;
import au.com.cybersearch2.classyjpa.persist.PersistenceXmlParserTest;
import au.com.cybersearch2.classyjpa.query.DaoQueryTest;
import au.com.cybersearch2.classyjpa.query.EntityQueryTest;
import au.com.cybersearch2.classyjpa.query.NativeQueryTest;
import au.com.cybersearch2.classyjpa.query.SqlQueryTest;
import au.com.cybersearch2.classyjpa.transaction.ClassyEntityTransactionTest;
import au.com.cybersearch2.classyjpa.transaction.TransactionStateTest;
import au.com.cybersearch2.classynode.NodeTest;

/**
 * JUnitTestSuite
 * @author Andrew Bowley
 * 19/06/2014
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BeanMapTest.class,
    BeanUtilTest.class,
    SQLiteSupportTest.class,
    DI_Test.class,
    ObjectGraphManagerTest.class,
    JpaIntegrationTest.class,
    OrmDaoHelperTest.class,
    OrmDaoHelperFactoryTest.class,
    ObjectMonitorTest.class,
    EntityManagerImplTest.class,
    PersistenceConfigTest.class,
    PersistenceXmlParserTest.class,
    TransactionStateTest.class,
    DatabaseAdminImplTest.class,
    NativeScriptDatabaseWorkTest.class,
    PersistenceDaoTest.class,
    PersistenceContainerTest.class,
    SQLiteDatabaseSupportTest.class,
    DaoQueryTest.class,
    EntityQueryTest.class,
    NativeQueryTest.class,
    SqlQueryTest.class,
    ClassyEntityTransactionTest.class,
    ClassAnalyserTest.class,
    NodeTest.class
})
public class JUnitTestSuite 
{   
}


