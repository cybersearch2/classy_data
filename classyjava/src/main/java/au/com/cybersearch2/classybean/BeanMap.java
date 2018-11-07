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
// Original code provided on Stack Overflow site by Mike Slattery @ cirriapp.com
/*
 * OpenBeans is simply a redistribution of the java.beans package from the Apache Harmony project, which is an 
 * open source implementation of Java SE. The only modification to the Harmony code is that the package name 
 * has been changed from java.beans to com.googlecode.openbeans. This was done to support the Android 
 * environment which does not include java.beans in it's core libraries. 
 */

package au.com.cybersearch2.classybean;

import com.googlecode.openbeans.*;

import java.lang.reflect.Method;
import java.util.*;

/**
 * BeanMap
 * Creates a Map by wrapping an object.
 * @author Andrew Bowley
 * 28/05/2014
 */
public class BeanMap extends AbstractMap<String, Object>
{
    // Maps PropertyDescriptors to property names
    private HashMap<String, PropertyDescriptor> properties;
    // The object being wrapped
    private Object bean;

    /**
     * Construct a BeanMap instance by wrapping an Object which follows Java Bean spec.
     * @param bean Object with setters and getters to access properties
     */
    public BeanMap(Object bean)  
    {
        this.bean = bean;
        properties = new HashMap<String, PropertyDescriptor>();
        BeanInfo info = BeanUtil.getBeanInfo(bean);
        for (PropertyDescriptor property : info.getPropertyDescriptors()) 
            properties.put(property.getName(), property);
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>If this map permits null values, then a return value of
     * {@code null} does not <i>necessarily</i> indicate that the map
     * contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@link #containsKey
     * containsKey} operation may be used to distinguish these two cases.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional)
     * @throws NullPointerException if the specified key is null and this map
     *         does not permit null keys (optional)
     */
    @Override public Object get(Object key) 
    {
        PropertyDescriptor property = properties.get(key);
        return (property == null) ? null : invoke(property); 
    }

    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A map
     * <tt>m</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * <tt>true</tt>.)
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>,
     *         if the implementation supports <tt>null</tt> values.)
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *         is not supported by this map
     * @throws ClassCastException if the class of the specified key or value
     *         prevents it from being stored in this map
     * @throws NullPointerException if the specified key or value is null
     *         and this map does not permit null keys or values
     * @throws IllegalArgumentException if some property of the specified key
     *         or value prevents it from being stored in this map
     */
    @Override public Object put(String key, Object value) 
    {
        PropertyDescriptor property = properties.get(key);
        Method method = property.getWriteMethod();
        return BeanUtil.invoke(method, bean, new Object[] {value});
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    @Override public Set<Map.Entry<String, Object>> entrySet() 
    {
        HashSet<Map.Entry<String, Object>> result = 
            new HashSet<Map.Entry<String, Object>>(properties.size() * 2);
        for (PropertyDescriptor property : properties.values()) 
        {
            String key = property.getName();
            Object value = null;
            value = invoke(property); 
            result.add(new PropertyEntry(key, value));
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * Invoke getter
     *@param property PropertyDescriptor
     *@return Object returned by getter
     */
    protected Object invoke(PropertyDescriptor property) 
    {
        Method method = property.getReadMethod();
        if (method == null)
            return null; // No getter defined if method == null
        return BeanUtil.invoke(method, bean, BeanUtil.NO_ARGS);
    }

    
    /**
     * Returns the number of key-value mappings in this map.  
     *
     * @return int
     */
    @Override public int size() 
    { 
        return properties.size(); 
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the specified
     * key.  More formally, returns <tt>true</tt> if and only if
     * this map contains a mapping for a key <tt>k</tt> such that
     * <tt>(key==null ? k==null : key.equals(k))</tt>.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     *         key
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional)
     * @throws NullPointerException if the specified key is null and this map
     *         does not permit null keys (optional)
     */
    @Override public boolean containsKey(Object key) 
    { 
        return properties.containsKey(key);
    }

    /**
     * 
     * PropertyEntry
     * An Entry maintaining a key and a value.  The value may be
     * changed using the <tt>setValue</tt> method.  This class
     * facilitates the process of building custom map
     * implementations. For example, it may be convenient to return
     * arrays of <tt>SimpleEntry</tt> instances in method
     * <tt>Map.entrySet().toArray</tt>.
     */
    class PropertyEntry extends AbstractMap.SimpleEntry<String, Object> 
    {
        private static final long serialVersionUID = -6784633780309646695L;

        /**
         * Construct PropertyEntry instance
         * @param key 
         * @param value
         */
        PropertyEntry(String key, Object value) 
        {
            super(key, value);
        }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).  (Writes through to the map.)  The
     * behavior of this call is undefined if the mapping has already been
     * removed from the map (by the iterator's <tt>remove</tt> operation).
     *
     * @param value new value to be stored in this entry
     * @return old value corresponding to the entry
     */
        @Override public Object setValue(Object value) 
        {
            super.setValue(value);
            return BeanMap.this.put(getKey(), value);
        }
    }
    
 
}
