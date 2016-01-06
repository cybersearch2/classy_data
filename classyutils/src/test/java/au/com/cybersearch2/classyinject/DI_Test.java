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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.util.Date;

import org.junit.Test;

/**
 * DI_Test
 * @author Andrew Bowley
 * 11/06/2014
 */
public class DI_Test
{
  
    static class DummyComponent implements ApplicationModule
    {
        
    }
    
    @Test
    public void test_constructor_null_application_module()
    {
        try
        {
            DI.getInstance(null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"applicationModule\" is null");
        }
    }

    @Test
    public void test_inject()
    {
        int magicValue = 20160101;
        TestModule firstModule = new TestModule(magicValue);
        ApplicationComponent component = DaggerApplicationComponent.builder()
            .testModule(firstModule)
            .build();
        DI.getInstance(component);
        Injectee injectee = new Injectee();
        DI.inject(injectee);
        assertThat(injectee.getMagicNumber()).isEqualTo(magicValue);
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

    @Test
    public void test_moduleInject()
    {
        int magicValue = 20160101;
        TestModule firstModule = new TestModule(magicValue);
        Date testDate = new Date();
        SubAppModule subAppModule = new SubAppModule(testDate);
        ApplicationComponent component = DaggerApplicationComponent.builder()
                .testModule(firstModule)
                .build();
        DI.getInstance(component, subAppModule);
        ScopedActivity scopedActivity = new ScopedActivity();
        DI.inject(scopedActivity);
        assertThat(scopedActivity.getCreated()).isEqualTo(testDate);
        Injectee injectee = new Injectee();
        DI.inject(injectee);
        assertThat(injectee.getMagicNumber()).isEqualTo(magicValue);
    }

    @Test
    public void test_inject2_null_injectee()
    {
        DI.getInstance(new DummyComponent()); 
        try
        {
            DI.inject(new DependencyProvider<Injectee>(){}, null);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        }
        catch (IllegalArgumentException e)
        {
            assertThat(e.getMessage()).isEqualTo("Parameter \"injectee\" is null");
        }
    }

}
