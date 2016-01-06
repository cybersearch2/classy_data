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

import java.util.Date;

import javax.inject.Singleton;

import org.junit.Test;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;

/**
 * ObjectGraphManagerTest
 * 
 * @author Andrew Bowley 05/06/2014
 */
public class ObjectGraphManagerTest
{
    @Module
    public class SubTransientAppModule implements DependencyProvider<ScopedActivity>
    {
        @Provides Date provideDate()
        {
            return new Date();
        }
    }

    @Singleton
    @Component(modules = TestModule.class)  
    public interface TransientAppComponent extends ApplicationModule
    {
        TransAppSubComponent plus(SubTransientAppModule subAppModule);
    }

    @Singleton // <- not @PerActivity
    @Subcomponent(modules = SubTransientAppModule.class)
    public interface TransAppSubComponent
    {
        void inject(ScopedActivity scopedActivity);
    }

    @Test 
    public void test_inject() 
    { 
        int magicValue = 20160101;
        TestModule firstModule = new TestModule(magicValue);
        ApplicationComponent component = DaggerApplicationComponent.builder()
            .testModule(firstModule)
            .build();
        ObjectGraphManager objectGraphManager = new ObjectGraphManager(component);
        Injectee injectee = new Injectee();
        objectGraphManager.inject(injectee);
        assertThat(injectee.getMagicNumber()).isEqualTo(magicValue);
    }
 
    @Test
    public void test_subComponentInject()
    {
        int magicValue = 20160101;
        TestModule firstModule = new TestModule(magicValue);
        Date testDate = new Date();
        SubAppModule subAppModule = new SubAppModule(testDate);
        ApplicationComponent component = DaggerApplicationComponent.builder()
                .testModule(firstModule)
                .build();
        ObjectGraphManager objectGraphManager = new ObjectGraphManager(component);
        ScopedActivity scopedActivity = new ScopedActivity();
        objectGraphManager.inject(subAppModule, scopedActivity);
        assertThat(scopedActivity.getCreated()).isEqualTo(testDate);
        ScopedActivity scopedActivity2 = new ScopedActivity();
        objectGraphManager.inject(subAppModule, scopedActivity2);
        assertThat(scopedActivity2.getCreated()).isEqualTo(testDate);
        
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
        ObjectGraphManager objectGraphManager = new ObjectGraphManager(component, subAppModule);
        ScopedActivity scopedActivity = new ScopedActivity();
        objectGraphManager.inject(scopedActivity);
        assertThat(scopedActivity.getCreated()).isEqualTo(testDate);
        ScopedActivity scopedActivity2 = new ScopedActivity();
        objectGraphManager.inject(scopedActivity2);
        assertThat(scopedActivity2.getCreated()).isEqualTo(testDate);
    }

    @Test
    public void test_transientModuleInject() throws InterruptedException
    {
        int magicValue = 20160101;
        TestModule firstModule = new TestModule(magicValue);
        SubTransientAppModule subAppModule = new SubTransientAppModule();
        TransientAppComponent component = DaggerObjectGraphManagerTest_TransientAppComponent.builder()
                .testModule(firstModule)
                .build();
        ObjectGraphManager objectGraphManager = new ObjectGraphManager(component, subAppModule);
        ScopedActivity scopedActivity = new ScopedActivity();
        objectGraphManager.inject(scopedActivity);
        Date date1 =  scopedActivity.getCreated();
        Thread.sleep(1001);
        ScopedActivity scopedActivity2 = new ScopedActivity();
        objectGraphManager.inject(scopedActivity2);
        Date date2 = scopedActivity2.getCreated();
        assertThat(date1).isNotEqualTo(date2);
    }

}
