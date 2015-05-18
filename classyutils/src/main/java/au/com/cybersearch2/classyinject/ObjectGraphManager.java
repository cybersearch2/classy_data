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

import java.util.List;

import dagger.ObjectGraph;

/**
 * ObjectGraphManager
 * Application ObjectGraph owner. 
 * @author Andrew Bowley
 * 15/04/2014
 */
public class ObjectGraphManager
{
    /** ObjectGraph created on application start. This may be superceded if add() is called. */
    protected ObjectGraph mainObjectGraph;

    /**
     * Construct ObjectGraphManager object. Must be a singleton, created on application start.
     * @param moduleList List of @Module annotated objects required to make the resulting graph complete
     * @see DI
     */
    public ObjectGraphManager(List<Object> moduleList)
    {
        if (moduleList == null)
            throw new IllegalArgumentException("Parameter \"moduleList\" is null");
        if (moduleList.isEmpty())
            throw new IllegalArgumentException("Parameter \"moduleList\" is empty");
        mainObjectGraph = createObjectGraph(moduleList);
    }
    
    /**
     * Inject an object into a new graph containing supplied module and based on mainObjectGraph.
     * @param injectee Object to inject 
     * @param module DependencyProvider for object class. Optional. If omitted, the mainObjectGraph will be injected.
     */
    public <T>  void inject(T injectee, DependencyProvider<T>... module)
    {
        if (injectee == null)
            throw new IllegalArgumentException("Parameter injectee is null");
        if (module.length > 1)
            throw new IllegalArgumentException("inject() method only supports one or zero modules");
        ObjectGraph graph = (module.length == 0) ? mainObjectGraph : mainObjectGraph.plus((Object[])module);
        graph.inject(injectee);
    }
 
    /**
     * Add @Module annotated objects to current ObjectGraph using plus() method
     * @param modules Object array
     */
    public void add(Object... modules)
    {
        if (modules == null)
            throw new IllegalArgumentException("Parameter \"modules\" is null");
        if (modules.length == 0)
            throw new IllegalArgumentException("Parameter \"modules\" is empty");
        ObjectGraph newObjectGraph = mainObjectGraph.plus(modules);
        mainObjectGraph = newObjectGraph;
    }
    
    /**
     * Validate the ObjectGraph
     */
    public void validate()
    {
        mainObjectGraph.validate();
    }
    
    /**
     * Returns ObjectGraph created from specified list of @Module annotated objects
     * @param moduleList List of @Module annotated objects required to make the resulting graph complete
     * @return ObjectGraph
     */
    protected ObjectGraph createObjectGraph(List<Object> moduleList)
    {
        return ObjectGraph.create(moduleList.toArray());
    }
    
}