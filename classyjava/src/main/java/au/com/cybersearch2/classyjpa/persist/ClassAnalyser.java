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
package au.com.cybersearch2.classyjpa.persist;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.PersistenceException;

import au.com.cybersearch2.classylog.JavaLogger;
import au.com.cybersearch2.classylog.Log;
import au.com.cybersearch2.classybean.BeanException;
import au.com.cybersearch2.classybean.BeanUtil;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataPersisterManager;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.misc.JavaxPersistenceImpl;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * ClassAnalyser
 * Adds to com.j256.ormlite.misc.JavaxPersistence to support OneToMany and ManyToOne annotations.
 * Duplicates some DatabaseFieldConfig code for processing @ForeignCollection annotation.
 * @author Andrew Bowley
 * 18/07/2014
 */
public class ClassAnalyser
{
    /**
     * The implementation of this class creates a Map entry which assigns a OrmDaoHelper<T,ID> to the specified entity class.
     * ClassRegistry
     * @author Andrew Bowley
     * 21/07/2014
     */
    interface ClassRegistry
    {
        /**
         * Register a helper of compound generic type <T,ID>.
         * @param entityClass
         * @param primaryKeyClass
         */
        <T,ID> void registerEntityClass(Class<T> entityClass, Class<ID> primaryKeyClass);
    }

    /**
     * ForeignFieldData
     * Contains foreignFieldMap and foreignCollectionMap used to map foreign field column name to field name and 
     * assign foreignTableConfig to foreignCollection fields
     * @author Andrew Bowley
     * 25/05/2014
     */
    public static class ForeignFieldData
    {   // FieldKey is a composite of field class (or generic class for collections) and column name.
        // JPA specifies mappedBy column name, but OrmLite uses field name for one to many associatiions.
        Map<FieldKey, DatabaseFieldConfig> foreignFieldMap;
        Map<FieldKey, DatabaseFieldConfig> foreignCollectionMap;
        
        public ForeignFieldData()
        {
            foreignFieldMap = new HashMap<FieldKey, DatabaseFieldConfig>();
            foreignCollectionMap = new HashMap<FieldKey, DatabaseFieldConfig>();
        }
    }
    

    public static final String TAG = "ClassAnalyser";
    protected static Log log = JavaLogger.getLogger(TAG);
    protected DatabaseType databaseType;
    protected ClassRegistry classRegistry;

    /**
     * Construct a ClassAnalyser instance
     * @param databaseType DatabaseType which specifies database feature set
     * @param classRegistry ClassRegistry implementation
     */
    public ClassAnalyser(DatabaseType databaseType, ClassRegistry classRegistry)
    {
        this.databaseType = databaseType;
        this.classRegistry = classRegistry;
    }

    /**
     * Returns a list of database table configurations for specified list of class names
     * @param managedClassNames List of class names representing all entities within a single Persistence Unit
     * @return List of DatabaseTableConfig
     */
    protected List<DatabaseTableConfig<?>> getDatabaseTableConfigList(List<String> managedClassNames)
    {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        // Report list of failed classes only after consuming managedClassNames list
        List<String> failedList = null;
        // Record data during analysis to resolve foreign field references
        ForeignFieldData foreignFieldData = new ForeignFieldData();
        // Obtain classes from class names
        for (String className: managedClassNames)
        {
            boolean success = false;
            try
            {
                classList.add(Class.forName(className));
                success = true;
            }
            catch (ClassNotFoundException e)
            {
                log.error(TAG, "Class not found: " + className, e);
            }
            if (!success)
            {
                if (failedList == null)
                    failedList = new ArrayList<String>();
                failedList.add(className);
            }
        }
        if (failedList != null)
            throw new PersistenceException("Failed to load following entity classes: " + failedList.toString());
        // Put database table configurations into a Map instead of a list to support set ForeignTableConfig below
        Map<String, DatabaseTableConfig<?>> tableConfigMap = new HashMap<String, DatabaseTableConfig<?>>();
        for(Class<?> clazz: classList)
        {
            DatabaseTableConfig<?> config = getTableConfiguration(clazz, foreignFieldData);
            if (config != null)
                tableConfigMap.put(clazz.getName(), config);
            else
            {
                if (failedList == null)
                    failedList = new ArrayList<String>();
                failedList.add(clazz.getName());
            }
        }
        if (failedList != null)
            throw new PersistenceException("Failed to extract persistence config following entity classes: " + failedList.toString());
        // Resolve foreign field mapping and foreign table configs
        // Set ForeignCollectionForeignFieldName for foreign collections (OneToMany) 
        for (Entry<FieldKey, DatabaseFieldConfig> entry: foreignFieldData.foreignCollectionMap.entrySet())
        {   // Get associated field and assign it's field name to ForeignCollectionColumnName
            DatabaseFieldConfig fieldConfig = foreignFieldData.foreignFieldMap.get(entry.getKey());
            if (fieldConfig == null)
                throw new PersistenceException("Field of type " + entry.getKey().getEntityClass().getName() + 
                        " could not be found with column name '" + entry.getKey().getColumnName() + "'");
            entry.getValue().setForeignCollectionForeignFieldName(fieldConfig.getFieldName());
        }
        // Set ForeignTableConfig for foreign fields (ManyToOne) 
        for (Entry<FieldKey, DatabaseFieldConfig> entry: foreignFieldData.foreignFieldMap.entrySet())
        {
            Class<?> fieldType = entry.getKey().getEntityClass();
            DatabaseTableConfig<?> foreignTableConfig = tableConfigMap.get(fieldType.getName());
            if (foreignTableConfig == null)
                throw new PersistenceException("Table of type " + fieldType.getName() + " not found");
            entry.getValue().setForeignTableConfig(foreignTableConfig);
        }
        // Return tableConfigMap values converted to a list
        return new ArrayList<DatabaseTableConfig<?>>(tableConfigMap.values());
    }

    /**
     * Returns DatabaseTableConfig for specified class
     * @param clazz Entity class
     * @param foreignFieldData ForeignFieldData to collect foreign field and foreign collection data
     * @return DatabaseTableConfig of generic type matching entity class or null if error occurs
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected DatabaseTableConfig<?> getTableConfiguration(Class<?> clazz, ForeignFieldData foreignFieldData)
    {
        List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
        // Obtain table name from @Entity annotation if available, otherwise use default name
        String tableName = new JavaxPersistenceImpl().getEntityName(clazz);
        // Allow name to be omitted
        if (tableName == null)
            tableName = "table" + clazz.getSimpleName();
        Class<?> idClass = null;
        // Ascend super class chain to find all relevant annotations
        for (Class<?> working = clazz; working != null; working = working.getSuperclass()) 
        {
            for (Field field : working.getDeclaredFields()) 
            {
                DatabaseFieldConfig fieldConfig = null;
                try
                {   // Try extract field configuration using supplied OrmLite library function
                    fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, field);
                }
                catch (SQLException e)
                {   // This exception is not thrown by the OrmLite code, just declared
                }
                if (fieldConfig == null) 
                    // In case nothing found, check for unsupported OneToMany annotation
                    fieldConfig = createOneToManyConfig(databaseType, field);
                if (fieldConfig != null) 
                {
                    fieldConfigs.add(fieldConfig);
                    // Perform further analysis to fill in gaps in OrmLite implementation
                    analyseFieldConfig(fieldConfig, field, foreignFieldData, clazz);
                    // Single out ID field so it's type can be used for helper registration
                    if ((fieldConfig.isId() || fieldConfig.isGeneratedId()) && (idClass == null)) 
                        // Expect only one id field. Catch first if more than one.
                        idClass = field.getType();
                }
            }
        }
        if (fieldConfigs.isEmpty()) 
            log.error(TAG, "Skipping " + clazz + " because no annotated fields found");
        else if (idClass == null)
            log.error(TAG, "Skipping " + clazz + " because no id field found");
        else
        {   // Perform helper registration before returning database table configuration
            classRegistry.registerEntityClass(clazz, idClass);
            return new DatabaseTableConfig(clazz, tableName, fieldConfigs);
        }
        return null;
    }

    /**
     * Perform additional annotation checks
     * @param fieldConfig populated DatabaseFieldConfig object for current field
     * @param field Field object
     * @param foreignFieldData ForeignFieldData to collect foreignCollection data 
     * @param clazz Entity class
     */
    private void analyseFieldConfig(
            DatabaseFieldConfig fieldConfig, 
            Field field, 
            ForeignFieldData foreignFieldData, 
            Class<?> clazz) 
    {
        // For foreign field, set ForeignColumnName and prepare to resolve foreignTableConfig
        // For foreign collection, prepare to resolve ForeignCollectionForeignFieldName
        if (fieldConfig.isForeign() || fieldConfig.isForeignCollection())
        {
            for (Annotation annotation : field.getAnnotations()) 
            {
                Class<?> annotationClass = annotation.annotationType();
                if (annotationClass.getName().equals("javax.persistence.JoinColumn")) 
                {
                    String referencedColumnName = getStringByInvocation(annotation, "referencedColumnName");
                    if ((referencedColumnName.length() > 0) && (fieldConfig.getColumnName() != null))
                    {
                        fieldConfig.setForeignColumnName(referencedColumnName);
                        FieldKey key = new FieldKey(field.getType(), fieldConfig.getColumnName());
                        foreignFieldData.foreignFieldMap.put(key, fieldConfig);
                    }
                }
                else if (annotationClass.getName().equals("javax.persistence.OneToMany")) 
                {
                    if (!Collection.class.isAssignableFrom(field.getType()))
                        throw new PersistenceException(
                                "@OneToMany annotation not applied to Collection type for field " + field);
                    String mappedBy = extractOneToManyField(fieldConfig, annotation, field);
                    if (mappedBy.length() > 0)
                    {
                        FieldKey key = new FieldKey(clazz, mappedBy);
                        foreignFieldData.foreignCollectionMap.put(key, fieldConfig);
                    }
                }

            }
        }
    }

    /**
     * Process OneToMany annotation missing from com.j256.ormlite.misc.JavaxPersistence probably because
     * ForeignCollection annotation serves the same purpose
     * 
     * @param databaseType DatabaseType object
     * @param field Field object
     * @return DatabaseFieldConfig 
     */
    private DatabaseFieldConfig createOneToManyConfig(DatabaseType databaseType, Field field) 
    {
        Annotation oneToManyAnnotation = null;
        for (Annotation annotation : field.getAnnotations()) 
        {
            Class<?> annotationClass = annotation.annotationType();
            if (annotationClass.getName().equals("javax.persistence.OneToMany")) 
            {
                if (!Collection.class.isAssignableFrom(field.getType()))
                    throw new PersistenceException(
                            "@OneToMany annotation not applied to Collection type for field " + field);
                oneToManyAnnotation = annotation;
                break; 
            }
        }
        if (oneToManyAnnotation == null)
            return null;
        DatabaseFieldConfig config = new DatabaseFieldConfig();
        String fieldName = field.getName();
        if (databaseType.isEntityNamesMustBeUpCase())
        {
            fieldName = fieldName.toUpperCase(Locale.US);
        }
        config.setFieldName(fieldName);
        if (config.getDataPersister() == null) 
            config.setDataPersister(DataPersisterManager.lookupForField(field));
        config.setUseGetSet((DatabaseFieldConfig.findGetMethod(field, false) != null) &&
                            (DatabaseFieldConfig.findSetMethod(field, false) != null));
        // Defaults from ForeignCollectionField
        config.setForeignCollection(true);
        config.setForeignCollectionMaxEagerLevel(1);
        config.setForeignCollectionOrderAscending(true);
        return config;
    }
 
    /**
     * Returns "mappedBy" attribute for OneToMany annotation. Also handles "fetch" value of "EAGER".
     * @param fieldConfig DatabaseFieldConfig object of current field
     * @param annotation Annotation object of current field
     * @param field Field  object
     * @return "mappedBy" value or empty String if not found or empty
     */
    protected String extractOneToManyField(DatabaseFieldConfig fieldConfig, Annotation annotation, Field field)
    {
       // The field that owns the relationship. Required unless the relationship is unidirectional.
        String mappedBy = getStringByInvocation(annotation, "mappedBy");
        String fetchType = getStringByInvocation(annotation, "fetch");
        if (fetchType.toString().equals("EAGER")) 
            fieldConfig.setForeignCollectionEager(true);
        if (mappedBy.length() > 0) 
        {
            fieldConfig.setForeignCollectionForeignFieldName(mappedBy);
            fieldConfig.setForeignCollectionColumnName(mappedBy);
        }
        // With OrmLite foreign collections, column name is expected to match field name 
        fieldConfig.setColumnName(field.getName());
        return mappedBy;
    }
 
    /**
     * Utility method to return unitName of class with PersistenceUnit annotation
     * @param clazz Class which is expect to have PersistenceUnit annotation
     * @return unitName
     * @throws PersistenceException if unitName not specified or empty
     */
    public static String getUnitName(Class<?> clazz) 
    {
        String unitName = null;
        for (Annotation annotation : clazz.getAnnotations())
        {
            Class<?> annotationClass = annotation.annotationType();
            if (annotationClass.getName().equals("javax.persistence.PersistenceUnit"))
            {
                unitName = getStringByInvocation(annotation, "unitName");
                break;
            }
        }
        if (unitName.length() == 0)
            throw new PersistenceException("Unit name not defined in @PersistenceUnit annotation for class " + clazz.getName()); 
        return unitName;
    }

    /**
     * Returns String from object obtained from annotation method invocation
     * @param annotation Annotation object
     * @param methodName
     * @return text or empty string if object returned is null 
     */
    protected static String getStringByInvocation(Annotation annotation, String methodName)
    {
        try
        {
            Object value = BeanUtil.invoke(annotation.getClass().getMethod(methodName), annotation);
            return (value != null) ? value.toString() : "";
        }
        catch (SecurityException e)
        {
            throw new BeanException(e);
        }
        catch (NoSuchMethodException e)
        {
            throw new BeanException(e);
        }
    }
    
}
