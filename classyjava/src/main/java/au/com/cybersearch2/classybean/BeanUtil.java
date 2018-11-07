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
/*
 * OpenBeans is simply a redistribution of the java.beans package from the Apache Harmony project, which is an 
 * open source implementation of Java SE. The only modification to the Harmony code is that the package name 
 * has been changed from java.beans to com.googlecode.openbeans. This was done to support the Android 
 * environment which does not include java.beans in it's core libraries. 
 */
package au.com.cybersearch2.classybean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.openbeans.BeanInfo;
import com.googlecode.openbeans.IntrospectionException;
import com.googlecode.openbeans.Introspector;
import com.googlecode.openbeans.PropertyDescriptor;

/**
 * BeanUtil
 * @author Andrew Bowley
 * 30/05/2014
 */
public class BeanUtil
{
    /**
     * DataPair
     * Entry for Map&lt:String, Object&gt;
     * @author Andrew Bowley
     * 28/07/2014
     */
    public static class DataPair extends AbstractMap.SimpleImmutableEntry<String, Object>
    {
        private static final long serialVersionUID = 6959237568599112142L;
        
        public DataPair(String key, Object value) 
        {
            super(key, value);
        }

    }

    /**
     * Static empty Object array to represent no parameters in reflection method call
     */
    public static final Object[] NO_ARGS = new Object[] {};

    /**
     * Returns the result of dynamically invoking this method. Equivalent to
     * {@code receiver.methodName(arg1, arg2, ... , argN)}.
     *
     * <p>If the method is static, the receiver argument is ignored (and may be null).
     *
     * <p>If the method takes no arguments, you can pass {@code (Object[]) null} instead of
     * allocating an empty array.
     *
     * <p>If you're calling a varargs method, you need to pass an {@code Object[]} for the
     * varargs parameter: that conversion is usually done in {@code javac}, not the VM, and
     * the reflection machinery does not do this for you. (It couldn't, because it would be
     * ambiguous.)
     *
     * <p>Reflective method invocation follows the usual process for method lookup.
     *
     * <p>If an exception is thrown during the invocation it is caught and
     * wrapped in an InvocationTargetException. This exception is then thrown.
     *
     * <p>If the invocation completes normally, the return value itself is
     * returned. If the method is declared to return a primitive type, the
     * return value is boxed. If the return type is void, null is returned.
     *
     * @param method
     *            the method to invoke
     * @param receiver
     *            the object on which to call this method (or null for static methods)
     * @param args
     *            the arguments to the method
     * @return the result
     *
     * @throws BeanException
     *             if this method is not accessible
     *             if the number of arguments doesn't match the number of parameters, the receiver
     *                is incompatible with the declaring class, or an argument could not be unboxed
     *                or converted by a widening conversion to the corresponding parameter type
     *             if an exception was thrown by the invoked method
     */
    public static Object invoke(Method method, Object receiver, Object... args)
            throws BeanException
    {
        try
        {
            return method.invoke(receiver, args);
        }
        catch (IllegalArgumentException e)
        {
            throw new BeanException("Invoke failed for method " + method.getName(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new BeanException("Invoke failed for method " + method.getName(), e);
        }
        catch (InvocationTargetException e)
        {
            throw new BeanException("Invoke failed for method " + method.getName(), e.getCause() == null ? e : e.getCause());
        }
    }

    /**
    * Gets the <code>BeanInfo</code> object which contains the information of
    * the properties, events and methods of the specified bean class.
    *
    * <p>
    * The <code>Introspector</code> will cache the <code>BeanInfo</code>
    * object. Subsequent calls to this method will be answered with the cached
    * data.
    * </p>
    *
    * @param bean The specified bean class.
    * @return the <code>BeanInfo</code> of the bean class.
    * @throws BeanException
    */ 
    public static BeanInfo getBeanInfo(Object bean)
    {
        BeanInfo info = null;
        try
        {
            info = Introspector.getBeanInfo(bean.getClass());
        }
        catch (IntrospectionException e)
        {
            throw new BeanException("Bean introspection failed for class " + bean.getClass().getName(), e);
        }
        return info;
    }
 
    /**
     * Returns bean properties as an Entry Set
     *@param bean The specified bean class.
     *@return Set&lt;DataPair&gt;
     */
    public static Set<DataPair> getDataPairSet(Object bean)
    {
        BeanInfo info = getBeanInfo(bean);
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        HashSet<DataPair> result = new HashSet<DataPair>(descriptors.length * 2);
        for (PropertyDescriptor property : descriptors) 
        {
            Method method = property.getReadMethod();
            if (method != null) // No getter defined if method == null
                result.add(new DataPair(property.getName(), invoke(method, bean, NO_ARGS)));
        }
        return result;
    }

    /**
     * Returns Object of specified class name
     *@param className
     *@return Object
     *@throws BeanException if class not found, failed to instantiate or security violated
     */
    public static Object newClassInstance(String className)
    {
        if (className == null)
            throw new IllegalArgumentException("Parameter className is null");
        try
        {
            Class<?> newClass = Class.forName(className);
            Object newInstance = newClass.newInstance();
            return newInstance;
        }
        catch (ClassNotFoundException e)
        {
            throw new BeanException("Class " + className + " not found", e);
        }
        catch (InstantiationException e)
        {
            throw new BeanException("Failed to instantiate class " + className, e.getCause() == null ? e : e.getCause());
        }
        catch (IllegalAccessException e)
        {
            throw new BeanException("Security prevented creation of class " + className, e);
        }
    }
}
