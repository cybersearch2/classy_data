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
package au.com.cybersearch2.classyjpa.entity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;
import au.com.cybersearch2.classyfy.data.alfresco.RecordCategory;

/**
 * ObjectMonitorTest
 * @author Andrew Bowley
 * 07/05/2014
 */
public class ObjectMonitorTest
{
    private static RecordCategory[] RECORD_CATEGORY_ARRAY2 = new RecordCategory[2];
    private static EntityKey[] ENTITY_KEY_ARRAY2 = new EntityKey[2];
    private RecordCategory entity1;
    private RecordCategory entity2;
    private Date created;
    private Date modified;
    
    @Before
    public void setUp() throws Exception 
    {
        entity1 = new RecordCategory();
        entity2 = new RecordCategory();
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        created = cal.getTime();
        modified = new Date();

    }
    
    @Test 
    public void test_start_managing_persist()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = new Integer(1);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.persist)).isNull();
        verifyEntity1(monitor, id, false);
        assertThat(monitor.removedObjects).isNull();
    }

    @Test 
    public void test_start_managing_merge()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = new Integer(1);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.merge)).isNull();
        verifyEntity1(monitor, id, true);
        assertThat(monitor.removedObjects).isNull();
    }

    @Test 
    public void test_start_managing_refresh()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = new Integer(1);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.refresh)).isNull();
        assertThat(monitor.managedObjects.isEmpty()).isTrue();
        assertThat(monitor.removedObjects).isNull();
    }

    @Test 
    public void test_start_managing_contains()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = new Integer(1);
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.contains)).isNull();
        assertThat(monitor.removedObjects).isNull();
    }


    @Test 
    public void test_start_managing_persist_null_primary_key()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = null;
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.persist)).isNull();
    }
    
    @Test 
    public void test_start_managing_merge_null_primary_key()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = null;
        try
        {
            monitor.startManagingEntity(entity1, id, PersistOp.merge);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(PersistOp.merge.toString());
            assertThat(e.getMessage()).contains("null primary key");
        }
   }

    @Test 
    public void test_start_managing_refresh_null_primary_key()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = null;
        try
        {
            monitor.startManagingEntity(entity1, id, PersistOp.refresh);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(PersistOp.refresh.toString());
            assertThat(e.getMessage()).contains("null primary key");
        }
   }

    @Test 
    public void test_start_managing_contains_null_primary_key()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id = null;
        assertThat(monitor.startManagingEntity(entity1, id, PersistOp.contains)).isNull();
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_persist_already_managed()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.managedObjects = mock(HashMap.class);
        when(monitor.managedObjects.containsKey(anyObject())).thenReturn(true);
        when(monitor.managedObjects.get(any(EntityKey.class))).thenReturn(entity2);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.persist)).isEqualTo(entity2);
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument.capture());
        verify(monitor.managedObjects).get(keyArgument.getValue());
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(keyArgument.getValue().isDirty()).isFalse();
        assertThat(monitor.removedObjects).isNull();
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_merge_already_managed()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        prepareMerge();
        monitor.managedObjects = mock(HashMap.class);
        when(monitor.managedObjects.containsKey(anyObject())).thenReturn(true);
        when(monitor.managedObjects.get(any(EntityKey.class))).thenReturn(entity1);
        assertThat(monitor.startManagingEntity(entity2, id1, PersistOp.merge)).isEqualTo(entity2);
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument.capture());
        verify(monitor.managedObjects).get(keyArgument.getValue());
        verify(monitor.managedObjects).remove(keyArgument.getValue());
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(keyArgument.getValue().isDirty()).isTrue();
        verify(monitor.managedObjects).put(keyArgument.getValue(), entity2);
        verifyMerge();
        assertThat(monitor.removedObjects).isNull();
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_refresh_already_managed()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.managedObjects = mock(HashMap.class);
        when(monitor.managedObjects.containsKey(anyObject())).thenReturn(true);
        when(monitor.managedObjects.get(any(EntityKey.class))).thenReturn(entity1);
        assertThat(monitor.startManagingEntity(entity2, id1, PersistOp.refresh)).isEqualTo(entity2);
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument.capture());
        verify(monitor.managedObjects).remove(keyArgument.getValue());
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(keyArgument.getValue().isDirty()).isFalse();
        verify(monitor.managedObjects).put(keyArgument.getValue(), entity2);
        assertThat(monitor.removedObjects).isNull();
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_contains_already_managed()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.managedObjects = mock(HashMap.class);
        when(monitor.managedObjects.containsKey(anyObject())).thenReturn(true);
        when(monitor.managedObjects.get(any(EntityKey.class))).thenReturn(entity2);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.contains)).isEqualTo(entity2);
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument.capture());
        verify(monitor.managedObjects).get(keyArgument.getValue());
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(keyArgument.getValue().isDirty()).isFalse();
        assertThat(monitor.removedObjects).isNull();
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_persist_removed_objects_populated()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(false);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.persist)).isNull();
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.removedObjects).containsKey(keyArgument.capture());
        verifyEntity1(monitor, id1, false);
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_merge_removed_objects_populated()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(false);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.merge)).isNull();
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.removedObjects).containsKey(keyArgument.capture());
        verifyEntity1(monitor, id1, true);
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_refesh_removed_objects_populated()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(false);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.refresh)).isNull();
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.removedObjects).containsKey(keyArgument.capture());
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_persist_removed_object_match()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(true);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.persist)).isNull();
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.removedObjects).containsKey(keyArgument.capture());
        verify(monitor.removedObjects).remove(keyArgument.getValue());
        verifyEntity1(monitor, id1, false);
        assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }

    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_merge_removed_object_match()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(true);
        try
        {
            monitor.startManagingEntity(entity1, id1, PersistOp.merge);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id1.toString());
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_refresh_removed_object_match()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(true);
        try
        {
            monitor.startManagingEntity(entity1, id1, PersistOp.refresh);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id1.toString());
        }
    }
 
    @SuppressWarnings("unchecked")
    @Test 
    public void test_start_managing_consists_removed_object_match()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        monitor.removedObjects = mock(HashMap.class);
        when(monitor.removedObjects.containsKey(anyObject())).thenReturn(true);
        when(monitor.removedObjects.get(any(EntityKey.class))).thenReturn(entity2);
        assertThat(monitor.startManagingEntity(entity1, id1, PersistOp.contains)).isEqualTo(entity2);
        ArgumentCaptor<EntityKey> keyArgument = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.removedObjects).containsKey(keyArgument.capture());
        verify(monitor.removedObjects).get(keyArgument.getValue());
         assertThat(keyArgument.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }

    @Test
    public void test_monitor_new_entity()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        assertThat(monitor.monitorNewEntity(entity1, id1, id1)).isTrue();
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_monitor_new_entity_primary_key_different()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        Integer id2 = new Integer(2);
        monitor.managedObjects = mock(HashMap.class);
        when(monitor.managedObjects.containsKey(isA(EntityKey.class))).thenReturn(false);
        assertThat(monitor.monitorNewEntity(entity2, id1, id2)).isTrue();
        ArgumentCaptor<EntityKey> keyArgument1 = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).remove(keyArgument1.capture());
        ArgumentCaptor<EntityKey> keyArgument2 = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).put(keyArgument2.capture(), isA(RecordCategory.class));
        assertThat(keyArgument1.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument1.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(keyArgument2.getValue().primaryKeyHash).isEqualTo(id2.hashCode());
   }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_monitor_new_entity_primary_key_already_managed()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        Integer id2 = new Integer(2);
        monitor.managedObjects = mock(HashMap.class);
        when(monitor.managedObjects.containsKey(isA(EntityKey.class))).thenReturn(true);
        when(monitor.managedObjects.get(isA(EntityKey.class))).thenReturn(new RecordCategory());
        assertThat(monitor.monitorNewEntity(entity2, id1, id2)).isFalse();
        ArgumentCaptor<EntityKey> keyArgument1 = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).remove(keyArgument1.capture());
        ArgumentCaptor<EntityKey> keyArgument2 = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument2.capture());
        assertThat(keyArgument1.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument1.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
        assertThat(keyArgument2.getValue().primaryKeyHash).isEqualTo(id2.hashCode());
   }
    
    @Test
    public void test_monitor_new_entity_primary_key1_Null()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = new Integer(1);
        assertThat(monitor.monitorNewEntity(entity1, null, id1)).isTrue();
        verifyEntity1(monitor, id1, false);
   }
    
    @Test
    public void test_monitor_new_entity_no_primary_key()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        Integer id1 = null;
        assertThat(monitor.monitorNewEntity(entity1, id1, id1)).isFalse();
    }
 
    @SuppressWarnings("unchecked")
    @Test 
    public void test_mark_for_removal()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        monitor.managedObjects = mock(HashMap.class);
        monitor.removedObjects = mock(HashMap.class);
        Integer id1 = new Integer(1);
        when(monitor.managedObjects.containsKey(isA(EntityKey.class))).thenReturn(true);
        when(monitor.managedObjects.get(isA(EntityKey.class))).thenReturn(entity1);
        monitor.markForRemoval(RecordCategory.class, id1);
        ArgumentCaptor<EntityKey> keyArgument1 = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument1.capture());
        verify(monitor.managedObjects).remove(keyArgument1.getValue());
        verify(monitor.removedObjects).put(keyArgument1.getValue(), entity1);
    }
    
    @SuppressWarnings("unchecked")
    @Test 
    public void test_mark_for_removal_unmanaged()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        monitor.managedObjects = mock(HashMap.class);
        monitor.removedObjects = mock(HashMap.class);
        Integer id1 = new Integer(1);
        when(monitor.managedObjects.containsKey(isA(EntityKey.class))).thenReturn(false);
        try
        {
            monitor.markForRemoval(RecordCategory.class, id1);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).contains(RecordCategory.class.getName());
            assertThat(e.getMessage()).contains(id1.toString());
        }
        ArgumentCaptor<EntityKey> keyArgument1 = ArgumentCaptor.forClass(EntityKey.class);
        verify(monitor.managedObjects).containsKey(keyArgument1.capture());
        assertThat(keyArgument1.getValue().entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(keyArgument1.getValue().primaryKeyHash).isEqualTo(id1.hashCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_release()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        monitor.managedObjects = mock(HashMap.class);
        monitor.removedObjects = mock(HashMap.class);
        monitor.release();
        verify(monitor.managedObjects).clear();
        verify(monitor.removedObjects).clear();
    }

    @Test
    public void test_get_objects_to_update()
    {
        ObjectMonitor monitor = new ObjectMonitor();
        monitor.managedObjects = new  HashMap<EntityKey, Object>();
        Integer id1 = new Integer(1);
        Integer id2 = new Integer(2);
        EntityKey key1 = new EntityKey(RecordCategory.class, id1);
        EntityKey key2 = new EntityKey(RecordCategory.class, id2);
        key2.setDirty(true);
        monitor.managedObjects.put(key1, entity1);
        monitor.managedObjects.put(key2, entity2);
        List<Object> list = monitor.getObjectsToUpdate();
        assertThat(list).isNotNull();
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo(entity2);
        assertThat(key2.isDirty()).isFalse();
    }
    
    private void verifyEntity1(ObjectMonitor monitor, Integer id, boolean expectedDirtyFlag)
    {
        assertThat(monitor.managedObjects).isNotNull();
        assertThat(monitor.managedObjects.size()).isEqualTo(1);
        assertThat(monitor.managedObjects.values().toArray(RECORD_CATEGORY_ARRAY2)[0]).isEqualTo(entity1);
        EntityKey key = monitor.managedObjects.keySet().toArray(ENTITY_KEY_ARRAY2)[0];
        assertThat(key.entityClassHash).isEqualTo(RecordCategory.class.hashCode());
        assertThat(key.primaryKeyHash).isEqualTo(id.hashCode());
        assertThat(key.isDirty()).isEqualTo(expectedDirtyFlag);
    }
    
    private void prepareMerge()
    {
        entity1.set_id(1);
        entity1.setCreated(created);
        entity1.setCreator("George");
        entity1.setDescription("original description");
        entity2.set_id(1);
        entity2.setCreated(created);
        entity2.setCreator("George");
        entity2.setDescription("new description");
        entity2.setModified(modified);
        entity2.setModifier("Harry");
    }
    
    private void verifyMerge()
    {
        assertThat(entity1.get_id()).isEqualTo(1);
        assertThat(entity1.getCreated()).isEqualTo(created);
        assertThat(entity1.getCreator()).isEqualTo("George");
        assertThat(entity1.getDescription()).isEqualTo("new description");
        assertThat(entity1.getModified()).isEqualTo(modified);
        assertThat(entity1.getModifier()).isEqualTo("Harry");
    }
}
