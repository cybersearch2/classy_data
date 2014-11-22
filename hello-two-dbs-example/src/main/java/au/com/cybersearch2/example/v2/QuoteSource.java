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
package au.com.cybersearch2.example.v2;

import java.util.Random;

/**
 * QuoteSource
 * @author Andrew Bowley
 * 19 Nov 2014
 */
public class QuoteSource 
{

	static String[] QUOTES =
	{
		"To be or not to be",
		"I come to bury Caesar",
		"Beware the ides of March",
		"A rose by any other name",
		"Once more into the breech",
		"Romeo, Romeo where for art thou",
		"Write once, run everywhere",
		"A standard is the starting point for doing things differently"
	};
	
	static String getQuote()
	{
		Random rand = new Random();
		return QUOTES[rand.nextInt(QUOTES.length)];
	}
}
