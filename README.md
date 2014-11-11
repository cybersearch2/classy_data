classy_tools
============

JPA lite for Android - uses OrmLite and Dagger

For information on Classy Tools and install instructions, go to our support site at www.cybersearch2.com.au/develop/classytools.html.

Classy Tools is a Lightweight Java Persistence package which builds on the OrmLite - Lightweight Object Relational Mapping Java Package 
so server-side standards for persistence are reflected on the client-side. Classy Tools incorporates the javax.persistence package and 
applies familiar JPA features such as persistence.xml for configuration, an Entity Manager and a Persistence Container, however
it is important to note that the design assumes only one application is accessing any database at any time, and only in a single session.
To keep things lightweight, database queries are performed using the Ormlite Custom Statement Builder rather than by using the 
Java Persistence Query Language (JQL)
   
The word "classy" means "elegant, smart, sophisticated..." which reflects the Classy Tools philosophy. The aim is to promote sound
development practices to achieve in a timely manner, reponsive, reliable applications which are readily maintained and improved.
Classy Tools comes with a pure Java base library, so is potentially portable to any database and operating system combination supported 
by OrmLite - refer http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_2.html#Database-Notes for Supported Databases.

A separate Android library provides an Android SQLite adapter and other features including an Android-specific Persistence Container 
and a Fast Text Search engine to support automatic search suggestions. Classy Tools binds classes using Dagger dependency injection
which enhances ease of configuration and flexibility. 

The Android "Classyfy" sample application can be cloned from https://github.com/andrew-bowley/classy_apps.git.    

