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
// Derived from OrmLite com.j256.ormlite.misc.JavaxPersistence and 
// com.j256.ormlite.android.apptools.OrmLiteConfigUtil
// Original copyright license:
/*
Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
granted, provided that this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING
ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,
WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE
USE OR PERFORMANCE OF THIS SOFTWARE.

The author may be contacted via http://ormlite.com/ 
*/


package au.com.cybersearch2.classyjpa.persist;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.PersistenceUnitInfo;

import au.com.cybersearch2.classyjpa.entity.OrmDaoHelperFactory;
import au.com.cybersearch2.classyjpa.persist.ClassAnalyser.ClassRegistry;
import au.com.cybersearch2.classyjpa.query.DaoQueryFactory;
import au.com.cybersearch2.classyjpa.query.NamedDaoQuery;
import au.com.cybersearch2.classyjpa.query.NamedSqlQuery;
import au.com.cybersearch2.classyjpa.query.QueryInfo;
import au.com.cybersearch2.classyjpa.query.SqlQueryFactory;
import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;

import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * PersistenceConfig
 * Configuration information for one Persistence Unit
 * @author Andrew Bowley
 * 05/05/2014
 */
public class PersistenceConfig
{
    public static final String TAG = "PersistenceConfig";
    protected static Log log = JavaLogger.getLogger(TAG);

    private static final String NAME_EXISTS_MESSAGE  = "Query name already exists: ";
    
    /** Maps ORM DAO helper factory object to entity class name */
    protected final Map<String,OrmDaoHelperFactory<?,?>> helperFactoryMap;
    /** Maps ORM query to name of query */ 
    protected Map<String,NamedDaoQuery> namedQueryMap;
    /** Maps native query to name of query */
    protected Map<String,NamedSqlQuery> nativeQueryMap;
    /** PU info from persistence.xml */
    protected PersistenceUnitInfo puInfo;
    /** Database type */
    protected DatabaseType databaseType;

    /**
     * Construct a PersistenceConfig instance
     * @param databaseType Database type
     */
    public PersistenceConfig(DatabaseType databaseType)
    {
        this.databaseType = databaseType;
        namedQueryMap = new HashMap<String,NamedDaoQuery>();
        nativeQueryMap = new HashMap<String,NamedSqlQuery>();
        helperFactoryMap = new HashMap<String,OrmDaoHelperFactory<?,?>>();
    }

    /**
     * Create a named query and store it in namedQueryMap. The query is implemented using OrmLite rather than JPA query language.
     * @param clazz Class&lt;?&gt; class of entity to which the query applies. This must be included in persistence.xml Persistence Unit class list.
     * @param name Query name
     * @param daoQueryFactory Query generator which uses supplied DAO for entity class
     */
    public void addNamedQuery(Class<?> clazz, String name, DaoQueryFactory daoQueryFactory)
    {
        if (existsName(name))
            log.warn(TAG, NAME_EXISTS_MESSAGE + name);
        else
            namedQueryMap.put(name, new NamedDaoQuery(clazz, name, daoQueryFactory));
    }

    /**
     * Add native named query to persistence unit context
     * @param name Query name
     * @param queryInfo Native query information
     * @param queryGenerator Native query generator
     */
    public void addNamedQuery(String name, QueryInfo queryInfo, SqlQueryFactory queryGenerator)
    {
        if (existsName(name))
            log.warn(TAG, NAME_EXISTS_MESSAGE + name);
        else
            nativeQueryMap.put(name, new NamedSqlQuery(name, queryInfo, queryGenerator));
    }

    /**
     * Returns true in query of specified name exists
     * @param name
     * @return boolean
     */
    protected boolean existsName(String name)
    {
        return namedQueryMap.containsKey(name) || nativeQueryMap.containsKey(name);
    }
    
    /**
     * Returns NamedDaoQuery objects mapped by name
     * @return Map&lt;String, NamedDaoQuery&gt;
     */
    public Map<String, NamedDaoQuery> getNamedQueryMap() 
    {
        return Collections.unmodifiableMap(namedQueryMap);
    }

    /**
     * Returns NamedSqlQuery objects mapped by name
     * @return Map&lt;String, NamedSqlQuery&gt;
     */
    public Map<String, NamedSqlQuery> getNativeQueryMap() 
    {
        return Collections.unmodifiableMap(nativeQueryMap);
    }

    /**
     * Returns OrmDaoHelperFactory objects mapped by entity class name
     * @return Map&lt;String, OrmDaoHelperFactory&gt;
     */
    public Map<String, OrmDaoHelperFactory<?, ?>> getHelperFactoryMap() 
    {
        return Collections.unmodifiableMap(helperFactoryMap);
    }

    /**
     * Returns PersistenceUnitInfo object unmarshalled from persistence.xml
     * @return PersistenceUnitInfo
     */
    public PersistenceUnitInfo getPuInfo() 
    {
        return puInfo;
    }

    /**
     * Set PersistenceUnitInfo object unmarshalled from persistence.xml and prepare entity class DAOs
     * @param puInfo PersistenceUnitInfo object
     */
    public void setPuInfo(PersistenceUnitInfo puInfo) 
    {
        this.puInfo = puInfo;
        List<String> managedClassNames = puInfo.getManagedClassNames();
        if (!managedClassNames.isEmpty())
        {
            ClassRegistry classRegistry = new ClassRegistry(){

                @Override
                public <T, ID> void registerEntityClass(Class<T> entityClass,
                        Class<ID> primaryKeyClass) 
                {
                    String key = entityClass.getName();
                    helperFactoryMap.put(key, new OrmDaoHelperFactory<T,ID>(entityClass));
               }};
            ClassAnalyser classAnlyser = new ClassAnalyser(databaseType, classRegistry);
            List<DatabaseTableConfig<?>> configs = classAnlyser.getDatabaseTableConfigList(managedClassNames);
            if (!configs.isEmpty())
                DaoManager.addCachedDatabaseConfigs(configs);
        }
    }
    
}
