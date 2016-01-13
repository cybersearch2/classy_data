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
package au.com.cybersearch2.classyinject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * ObjectGraphManager
 * Application ObjectGraph owner. 
 * @author Andrew Bowley
 * 15/04/2014
 */
public class ObjectGraphManager
{
    /** Collection of inject Methods belonging to ApplicationModule (Primary global scope component) */
    protected Map<Class<?>, Method> injectMap;
    /** Collection of subcomponent factory Methods belonging to ApplicationModule */
    protected Map<Class<?>, Method> subComponentMap;
    /** Collection of subcomponents created from additional modules */
    protected Map<Class<?>, Object> moduleMap;
    protected ApplicationModule applicationModule;

    /**
     * Construct ObjectGraphManager object. Must be a singleton, created on application start.
     * @param applicationModule ApplicationModule object for Dagger2 is a @Component annotated
     *                           class with Singleton scope ie. for life of application.
     * @param modules Option additional @Module annotated objects required to make the resulting graph complete.
     * @see DI
     */
    public ObjectGraphManager(ApplicationModule applicationModule, Object... modules)
    {
        this.applicationModule = applicationModule;
        injectMap = new HashMap<Class<?>, Method>();
        subComponentMap = new HashMap<Class<?>, Method>();
        moduleMap = new HashMap<Class<?>, Object>(); 
        if (applicationModule == null)
            throw new IllegalArgumentException("Parameter \"applicationModule\" is null");
        Method[] methods = applicationModule.getClass().getMethods();
        for (Method method: methods)
        {
            //if ("inject".equals(method.getName()))
                //System.out.println(method.getParameterTypes()[0]);
                //injectMap.put(method.getParameterTypes()[0], method);
            if (isSubComponentFactory(method))
            {
                Class<?> methodClass = method.getParameterTypes()[0];
                boolean moduleSupplied = false;
                for (Object module: modules)
                {
                    if (methodClass.equals(module.getClass()))
                    {
                        moduleMap.put(getGenericClass(module), createSubComponent(method, module));
                        moduleSupplied = true;
                        break;
                    }
                }
                if (!moduleSupplied)
                    subComponentMap.put(methodClass, method);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public InjectService<?> createInjectService(Object injectee)
    {
        InjectService<?> injectService = null;
        final Class<?> clazz = injectee.getClass();
        try
        {
            Method method = applicationModule.getClass().getDeclaredMethod("inject", clazz);
            injectMap.put(clazz, method);
            injectService = new InjectService(){

                @Override
                public void inject(Object injectee)
                {
                    Method method = injectMap.get(clazz);
                    try
                    {
                        method.invoke(applicationModule, injectee);
                    }
                    catch (IllegalArgumentException e)
                    {
                        e.printStackTrace();
                    }
                    catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                    catch (InvocationTargetException e)
                    {
                        e.printStackTrace();
                    }
                }};
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        return injectService;
    }
    
    private Class<?> getGenericClass(Object module)
    {
        Type[] types = module.getClass().getGenericInterfaces();
        for (Type type: types)
        {
            ParameterizedType parameterizedType = (ParameterizedType)type;
            String rawType = parameterizedType.getRawType().toString();
            if (rawType.endsWith(DependencyProvider.class.getName()))
            {
                String genericType = parameterizedType.getActualTypeArguments()[0].toString().split(" ")[1];
                //System.out.println(.toString());
                try
                {
                    return Class.forName(genericType);
                }
                catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Object createSubComponent(Method method, Object module)
    {
        Object subComponent = null;
        try
        {
            subComponent = method.invoke(applicationModule, module);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return subComponent;
    }

    private boolean isSubComponentFactory(Method method)
    {
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive() || returnType.equals(Void.class))
            return false;
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1)
            return false;
        Type[] types = parameterTypes[0].getGenericInterfaces();
        for (Type type: types)
        {
            String rawType = ((ParameterizedType)type).getRawType().toString();
            if (rawType.endsWith(DependencyProvider.class.getName()))
                return true;
        }
        return false; 
    }

    /**
     * Inject an object into a new graph containing supplied module and based on mainObjectGraph.
     * @param injectee Object to inject 
     */
    public <T>  void inject(T injectee)
    {
        if (injectee == null)
            throw new IllegalArgumentException("Parameter injectee is null");
        Method method = injectMap.get(injectee.getClass());
        if (method == null)
        {
            Object subComponent = moduleMap.get(injectee.getClass());
            injectSubComponent(subComponent, injectee);
            return;
        }
        // TODO - Log errors
        try
        {
            method.invoke(applicationModule, injectee);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }
 
    private <T> void injectSubComponent(Object subComponent, T injectee)
    {
        try
        {
            Method[] methods = subComponent.getClass().getMethods();
            for (Method subCompMethod: methods)
            {
                if ("inject".equals(subCompMethod.getName()) && injectee.getClass().equals(subCompMethod.getParameterTypes()[0]))
                {
                    subCompMethod.setAccessible(true);
                    subCompMethod.invoke(subComponent, injectee);
                    return;
                }
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    public <T>  void inject(DependencyProvider<T> module, T injectee)
    {
        if (module == null)
            throw new IllegalArgumentException("Parameter \"module\" is null");
        if (injectee == null)
            throw new IllegalArgumentException("Parameter \"injectee\" is null");
        Method method = subComponentMap.get(module.getClass());
        
        try
        {
            Object subComponent = method.invoke(applicationModule, module);
            injectSubComponent(subComponent, injectee);
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Validate the ObjectGraph
     */
    public void validate()
    {
        // TODO - Implement validate()
    }
    
}