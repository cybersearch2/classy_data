#!/bin/sh
#
# This script installs jar files in your local Maven repository.
#
# Usage:
#   install-java.sh
#
#  Installing Maven (prerequisite)
#  ----------------
#
#  1) Unpack the archive where you would like to store the binaries, eg:
#
#    Unix-based operating systems (Linux, Solaris and Mac OS X)
#      tar zxvf apache-maven-3.x.y.tar.gz
#
#  2) A directory called "apache-maven-3.x.y" will be created.
#
#  3) Add the bin directory to your PATH, for example:
#
#    Unix-based operating systems (Linux, Solaris and Mac OS X)
#      export PATH=/usr/local/apache-maven-3.x.y/bin:$PATH
#
#  4) Make sure JAVA_HOME is set to the location of your JDK
#
#  5) Run "mvn --version" to verify that it is correctly installed.
#
#  For complete documentation, see http://maven.apache.org/download.html#Installation

echo ** Install persistence-api-2.0.jar to local Maven repository
mvn -q install:install-file -Dfile=persistence-api-2.0.jar -Dpackaging=jar -DgeneratePom=true \
  -DgroupId=javax.persistence -DartifactId=persistence-api -Dversion=2.0 
echo ** Install openbeans-1.0.jar to local Maven repository
mvn -q install:install-file -Dfile=openbeans-1.0.jar -Dpackaging=jar -DgeneratePom=true \
  -DgroupId=com.googlecode.openbeans -DartifactId=openbeans -Dversion=1.0 

# Using OrmLite version 4.49-SNAPSHOT. Public snapshot repository (Sourceforge) requires login.

echo ** Install ormlite-core.jar to local Maven repository
mvn -q install:install-file -Dfile=ormlite-core/ormlite-core.jar -DpomFile=ormlite-core/pom.xml -Dpackaging=jar 
echo ** Install ormlite-jdbc.jar to local Maven repository
mvn -q install:install-file -Dfile=ormlite-jdbc/ormlite-jdbc.jar -DpomFile=ormlite-jdbc/pom.xml -Dpackaging=jar 
echo ** Install ormlite-android.jar to local Maven repository
mvn -q install:install-file -Dfile=ormlite-android/ormlite-android.jar -DpomFile=ormlite-android/pom.xml -Dpackaging=jar 
    
echo "Done!"
