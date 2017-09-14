# classy_data

Classy Data is a Lightweight Java Persistence package which builds on the [OrmLite](http://ormlite.com) lightweight Object Relational Mapping (ORM) Java package. 
It is a client-side persistence framework which adheres to a familiar industry specification and is suitable for resource-constrained platforms such as Android. 
Classy Data implements version 1 of the <a href="http://docs.oracle.com/javaee/6/api/javax/persistence/package-summary.html">javax.persistence</a> 
package which delivers a lean package, but sufficient for client-side persistence. JPA features include persistence.xml configuration, an entity manager factory, 
named queries and transactions which automatically roll back if an exception occurs. For more details, refer to [Lightweight JPA in a nutshell](http://cybersearch2.com.au/develop/jpa_intro.html)></a>

   
Classy Data is potentially portable to any database supported by OrmLite - refer [Database-Notes for Supported Databases](http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html) with
builtin support currently provided for SQLite and H2.

Classy Data binds classes using Dagger dependency injection which enhances ease of configuration, flexibility and testability.

## Getting Started

The instructions for getting started can be found at [Classy Data Home Page](http://cybersearch2.com.au/develop/classydata.html).
You will need to clone this project from the [Github site](https://github.com/cybersearch2/classy_data) and then use
Maven to install it. Both Java SE version 7 and above and Maven need to be installed in order to proceed. Once
the project is installed you can progress through the eamples starting with [many2many example](http://cybersearch2.com.au/develop/many2many.html).


## classy_apps

A separate [Android](https://github.com/cybersearch2/classy_apps) library provides an Android SQLite adapter and other features including an Android-specific Persistence Container 
and a Fast Text Search engine to support automatic search suggestions.  
   

