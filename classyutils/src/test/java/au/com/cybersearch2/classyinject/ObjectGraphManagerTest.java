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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;
import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

/**
 * ObjectGraphManagerTest
 * @author Andrew Bowley
 * 05/06/2014
 */
public class ObjectGraphManagerTest
{
    static ObjectGraph objectGraph;
    static ObjectGraph objectGraph2;
    static List<Object> moduleList;
    static DependencyProvider<ObjectGraphManagerTest> dependencyProvider;
 
    @Module
    static class TestModule
    {
        
    }
    @Module(injects = Injectee.class)
    static class TestModule2 implements DependencyProvider<Injectee>
    {
        @Provides
        Object provideObject() 
        {
            return new Object();
        }
    }

    static class Injectee
    {
        @Inject Object object;
        
        Object get()
        {
            return object;
        }
    }
    
    static class TestObjectGraphManager extends ObjectGraphManager
    {
        public TestObjectGraphManager(List<Object> moduleList)
        {
            super(moduleList);
            
        }

        // Override ObjectGraph create method to inject mock
        @Override
        protected ObjectGraph createObjectGraph(List<Object> moduleList)
        {
            ObjectGraphManagerTest.moduleList = moduleList;
            return objectGraph; 
        }
        
    }

    @Before
    public void setUp() throws Exception 
    {
        if (objectGraph == null)
            objectGraph = mock(ObjectGraph.class);
        if (objectGraph2 == null)
            objectGraph2 = mock(ObjectGraph.class);
    }
    
    @Test 
    public void test_constructor()
    {
        TestModule firstModule = new TestModule();
        moduleList = new ArrayList<Object>();
        moduleList.add(firstModule);
        ObjectGraphManager objectGraphManager = new TestObjectGraphManager(moduleList);
        assertThat(objectGraphManager.mainObjectGraph).isEqualTo(objectGraph);
        assertThat(moduleList.size()).isEqualTo(1);
        assertThat(moduleList.get(0)).isEqualTo(firstModule);
    }

    @Test 
    public void test_constructor_extra_module()
    {
        TestModule firstModule = new TestModule();
        TestModule2 testModule2 = new TestModule2();
        List<Object> classList = new ArrayList<Object>();
        classList.add(testModule2 );
        classList.add(firstModule);
       moduleList = null;
        ObjectGraphManager objectGraphManager = new TestObjectGraphManager(classList);
        assertThat(objectGraphManager.mainObjectGraph).isEqualTo(objectGraph);
        assertThat(moduleList.size()).isEqualTo(2);
        assertThat(moduleList.get(0)).isEqualTo(testModule2);
        assertThat(moduleList.get(1)).isEqualTo(firstModule);
    }
    
    @Test 
    public void test_constructor_null_classList()
    {
        try
        {
            new ObjectGraphManager(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"moduleList\" is null");
        }
        
    }

    @Test 
    public void test_constructor_empty_classList()
    {
        try
        {
            new ObjectGraphManager(new ArrayList<Object>());
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"moduleList\" is empty");
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_inject()
    {
        ObjectGraphManager objectGraphManager = createTestObjectGraphManager();
        Injectee injectee = new Injectee();
        objectGraphManager.inject(injectee);
        verify(objectGraphManager.mainObjectGraph).inject(injectee);
    }

    
    @SuppressWarnings("unchecked")
    @Test
    public void test_inject_with_module()
    {
        ObjectGraphManager objectGraphManager = createTestObjectGraphManager();
        TestModule2 testModule2 = new TestModule2();
        when(objectGraphManager.mainObjectGraph.plus(testModule2)).thenReturn(objectGraph2);
        Injectee injectee = new Injectee();
        objectGraphManager.inject(injectee, testModule2);
        verify(objectGraph2).inject(injectee);
    }

    @Test
    public void test_add()
    {
        ObjectGraphManager objectGraphManager = createTestObjectGraphManager();
        TestModule2 testModule2 = new TestModule2();
        when(objectGraphManager.mainObjectGraph.plus(testModule2)).thenReturn(objectGraph2);
        objectGraphManager.add(testModule2);
        assertThat(objectGraphManager.mainObjectGraph).isEqualTo(objectGraph2);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void test_inject__more_than_one_module()
    {
        Injectee injectee = new Injectee();
        TestModule2 module1 = new TestModule2();
        TestModule2 module2 = new TestModule2();
       try
        {
            createTestObjectGraphManager().inject(injectee, module1, module2);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("inject() method only supports one or zero modules");
        }
        
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_inject_null_injectee()
    {
        try
        {
            createTestObjectGraphManager().inject(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter injectee is null");
        }
        
    }

    @Test
    public void test_add_null_modules()
    {
        try
        {
            createTestObjectGraphManager().add((Object[])null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"modules\" is null");
        }
        
    }

    @Test
    public void test_add_empty_modules()
    {
        try
        {
            createTestObjectGraphManager().add(new Object[0]);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch(IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"modules\" is empty");
        }
        
    }

    @Test
    public void test_validate()
    {
        ObjectGraphManager objectGraphManager = createTestObjectGraphManager();
        objectGraphManager.validate();
        verify(objectGraphManager.mainObjectGraph).validate();
    }
  
    private ObjectGraphManager createTestObjectGraphManager()
    {
        TestModule firstModule = new TestModule();
        List<Object> classList = new ArrayList<Object>();
        classList.add(firstModule);
        return new TestObjectGraphManager(classList);
    }
}
