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
package au.com.cybersearch2.classyinject;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * TestModule
 * @author Andrew Bowley
 * 1 Jan 2016
 */
@Module
public class TestModule
{
    private final Integer magicNumber;

    public TestModule(Integer magicNumber)
    {
        this.magicNumber = magicNumber;
    }
    
    @Provides @Singleton Integer provideMagicNumber()
    {
        return magicNumber;
    }
}
