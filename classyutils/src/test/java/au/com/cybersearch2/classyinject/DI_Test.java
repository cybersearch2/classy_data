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

import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;

import dagger.Module;
import dagger.Provides;

/**
 * DI_Test
 * @author Andrew Bowley
 * 11/06/2014
 */
public class DI_Test
{
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
    
    
    class TestDI extends DI
    {
        List<Object> moduleList;
        int moduleCount;
        
        public TestDI(ApplicationModule applicationModule, Object... modules)
        {
            super(applicationModule, modules);
        }
   
        @Override
        protected ObjectGraphManager createObjectGraphManager(List<Object> moduleList)
        {
            this.moduleList = moduleList;
            moduleCount = moduleList.size();
            return testOjectGraphManager;
        }
   }

    ApplicationModule applicationModule;
    ObjectGraphManager testOjectGraphManager;
    
 
    @Before
    public void setup()
    {
        applicationModule = mock(ApplicationModule.class);
        testOjectGraphManager = mock(ObjectGraphManager.class);
    }
    
    @Test
    public void test_constructor()
    {
        Object[] testObjects = new Object[2];
        Object object0 = new Object();
        Object object1 = new Object();
        testObjects[0] = object0;
        testObjects[1] = object1;
        TestDI testDI = new TestDI(applicationModule, testObjects);
        assertThat(testDI.moduleCount).isEqualTo(3);
        assertThat(testDI.moduleList.size()).isEqualTo(0);
        assertThat(DI.getInstance()).isEqualTo(testDI);
        DI.singleton = null;
        try
        {
            DI.getInstance();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        }
        catch (IllegalStateException e)
        {
            assertThat(e.getMessage()).isEqualTo("DI called while not initialized");
        }
    }
    
    @Test
    public void test_constructor_null_application_module()
    {
        try
        {
            new DI(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"applicationModule\" is null");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_inject()
    {
        new TestDI(applicationModule);
        Object injectee = new Object();
        DI.inject(injectee);
        verify(testOjectGraphManager).inject(injectee);
    }

    @Test
    public void test_inject_null_injectee()
    {
        try
        {
            DI.inject(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"injectee\" is null");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_inject2()
    {
        new TestDI(applicationModule);
        Injectee injectee = new Injectee();
        TestModule2 module = new TestModule2();
        DI.inject(module, injectee);
        verify(testOjectGraphManager).inject(injectee, module);
    }

    @Test
    public void test_inject2_null_injectee()
    {
        try
        {
            TestModule2 module = new TestModule2();
            DI.inject(module, null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"injectee\" is null");
        }
    }

    @Test
    public void test_inject2_null_module()
    {
        try
        {
            DI.inject(null, new Injectee());
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"module\" is null");
        }
    }

    @Test
    public void test_add()
    {
        Object[] testObjects = new Object[2];
        Object object0 = new Object();
        Object object1 = new Object();
        testObjects[0] = object0;
        testObjects[1] = object1;
        new TestDI(applicationModule);
        DI.add(testObjects);
        verify(testOjectGraphManager).add(testObjects);
    }

    @Test
    public void test_add_null_modules()
    {
        try
        {
            DI.add((Object[])null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"modules\" is null or empty");
        }
    }

    @Test
    public void test_add_empty_modules()
    {
        try
        {
            DI.add(new Object[0]);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"modules\" is null or empty");
        }
    }

    @Test
    public void test_validate()
    {
        DI testDI = new TestDI(applicationModule);
        testDI.validate();
        verify(testOjectGraphManager).validate();
    }

}
