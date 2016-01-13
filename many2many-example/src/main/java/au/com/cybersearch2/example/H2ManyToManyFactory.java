/**
    Copyright (C) 2015  www.cybersearch2.com.au

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
package au.com.cybersearch2.example;

import javax.inject.Singleton;

import au.com.cybersearch2.classyapp.JavaTestResourceEnvironment;
import au.com.cybersearch2.classyinject.ApplicationModule;
import au.com.cybersearch2.classyjpa.entity.PersistenceWork;
import au.com.cybersearch2.classyjpa.entity.PersistenceWorkModule;
import au.com.cybersearch2.classyjpa.persist.PersistenceContext;
import au.com.cybersearch2.classytask.Executable;
import dagger.Component;
import dagger.Subcomponent;

/**
 * H2ManyToManyFactory
 * @author Andrew Bowley
 * 8 Jan 2016
 */
public class H2ManyToManyFactory
{
    @Singleton
    @Component(modules = H2ManyToManyModule.class)  
    static interface ApplicationComponent extends ApplicationModule
    {
        PersistenceContext persistenceContext();
        H2PersistenceWorkSubcontext plus(PersistenceWorkModule persistenceWorkModule);
    }

    @Singleton
    @Subcomponent(modules = PersistenceWorkModule.class)
    static interface H2PersistenceWorkSubcontext
    {
        Executable executable();
    }

    protected ApplicationComponent component;
    protected PersistenceWorkModule persistenceWorkModule;
    
    public H2ManyToManyFactory()
    {
        component = 
                DaggerH2ManyToManyFactory_ApplicationComponent.builder()
                .h2ManyToManyModule(new H2ManyToManyModule(new JavaTestResourceEnvironment("src/main/resources")))
                .build();
    }
    
    public PersistenceContext getPersistenceContext()
    {
        return component.persistenceContext();
    }
    
    public Executable getExecutable(PersistenceWork persistenceWork)
    {
        persistenceWorkModule = new PersistenceWorkModule("manytomany", true, persistenceWork);
        return component.plus(persistenceWorkModule).executable();
    }

}
