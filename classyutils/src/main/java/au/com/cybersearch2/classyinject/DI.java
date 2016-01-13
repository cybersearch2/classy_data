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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import au.com.cybersearch2.classylog.*;

/**
 * DI
 * Utility class for global dependency injection management
 * @author Andrew Bowley
 * 28/04/2014
 */
public class DI
{
    public static final String TAG = "DependencyInjection";
    private final static Log log = JavaLogger.getLogger(TAG);
    /* Only a single instance manages dependency injection */ 
    protected static DI singleton;
    /** Implementation delegated to an ObjectGraphManager instance */ 
    protected ObjectGraphManager objectGraphManager; 
    protected Map<Class<?>, InjectService<?>> injectServiceMap;

    public static <T> void register(Class<?> targetClass, InjectService<T> injectService)
    {
        if (singleton == null)
            synchronized(DI.class)
            {
                if (singleton == null)
                    singleton = new DI();
            }
        singleton.injectServiceMap.put(targetClass, injectService);
    }
 
    public static boolean isRegistered(Class<?> targetClass)
    {
        return singleton != null && singleton.injectServiceMap.containsKey(targetClass);
    }
        
    /** 
     * Returns DI singleton instance
     * @return DI
     * @throws IllegalStateException if called before singleton instantiated
     */
    public static DI getInstance()
    {
        if (singleton == null)
            throw new IllegalStateException("DI called while not initialized");
        return singleton;
    }

    /**
     * Construct DI singleton object. 
     * If Android, then an Application object should be created to call this constructor when onCreate() is called.
     * @param applicationModule The @Module annotated object defining all permanent bindings
     * @param modules Option additional @Module annotated objects required to make the resulting graph complete.
     */
    public static DI getInstance(ApplicationModule applicationModule, Object... modules)
    {
        synchronized(DI.class)
        {
            if (singleton == null)
                singleton = new DI();
            singleton.createObjectGraphManager(applicationModule, modules);
        }
        return singleton;
    }

    /**
     * Inject an object from the Object Graph
     * @param injectee Object containing one or more @Inject annotated class members
     */
    @SuppressWarnings("unchecked")
    public static <T> void inject(T injectee)
    {
        if (injectee == null)
            throw new IllegalArgumentException("Parameter \"injectee\" is null");
        Class<T> clazz = (Class<T>) injectee.getClass();
        DI dependencyInjection = getInstance();
        InjectService<T> injectService = (InjectService<T>) dependencyInjection.injectServiceMap.get(clazz);
        if (injectService == null)
        {
            if (dependencyInjection.objectGraphManager != null)
                injectService = (InjectService<T>) dependencyInjection.objectGraphManager.createInjectService(injectee);
            if (injectService != null)
                register(clazz, injectService);
            else
                throw new IllegalArgumentException("Class \"" + clazz.getName() + "\" is not configured for Dependency Injection");
       }
       injectService.inject(injectee);
    } 

    /**
     * Inject an object from the Object Graph and transient plus ObjectGraph 
     * @param module DependencyProvider of inject generic type - an @Module annotated object which defines the plus ObjectGraph
     * @param injectee Object containing one or more @Inject annotated class members
     */
    public static <T>  void inject(DependencyProvider<T> module, T injectee)
    {
        if (module == null)
            throw new IllegalArgumentException("Parameter \"module\" is null");
        if (injectee == null)
            throw new IllegalArgumentException("Parameter \"injectee\" is null");
        getInstance().objectGraphManager.inject(module, injectee);
    }
    
    /**
     * Validate the ObjectGraph
     */
    public void validate()
    {
        objectGraphManager.validate();
    }

    private DI()
    {
        objectGraphManager = new ObjectGraphManager(new ApplicationModule(){});
        injectServiceMap = new HashMap<Class<?>, InjectService<?>>();
    }

    /**
     * Construct DI singleton object. 
     * If Android, then an Application object should be created to call this constructor when onCreate() is called.
     * @param applicationModule The @Module annotated object defining all permanent bindings
     * @param modules Option additional @Module annotated objects required to make the resulting graph complete.
     */
    private void createObjectGraphManager(ApplicationModule applicationModule, Object... modules)
    {
        if (applicationModule == null)
            throw new IllegalArgumentException("Parameter \"applicationModule\" is null");
        objectGraphManager = new ObjectGraphManager(applicationModule, modules);
        if (log.isLoggable(TAG, Level.INFO))
           log.info(TAG, "Application object graph created.");
    }
}
