@echo off
REM  Installing Maven (prerequisite)
REM  ----------------
REM
REM  1) Unpack the archive where you would like to store the binaries, eg:
REM
REM    Windows
REM      unzip apache-maven-3.x.y.zip
REM
REM  2) A directory called "apache-maven-3.x.y" will be created.
REM
REM  3) Add the bin directory to your PATH, for example:
REM
REM    Windows
REM      set PATH="c:\program files\apache-maven-3.x.y\bin";%PATH%
REM
REM  4) Make sure JAVA_HOME is set to the location of your JDK
REM
REM  5) Run "mvn --version" to verify that it is correctly installed.
REM
REM  For complete documentation, see http://maven.apache.org/download.html#Installation

echo ** Install persistence-api-2.0.jar to local Maven repository
call mvn install:install-file -Dfile=persistence-api-2.0.jar -Dpackaging=jar -DgeneratePom=true -DgroupId=javax.persistence -DartifactId=persistence-api -Dversion=2.0 
if not errorlevel 0 goto error_exit
echo ** Install openbeans-1.0.jar to local Maven repository
call mvn install:install-file -Dfile=openbeans-1.0.jar -Dpackaging=jar -DgeneratePom=true -DgroupId=com.googlecode.openbeans -DartifactId=openbeans -Dversion=1.0 
if not errorlevel 0 goto error_exit

REM Using OrmLite version 4.49-SNAPSHOT. Public snapshot repository (Sourceforge) requires login.

echo ** Install ormlite-core.jar to local Maven repository
call mvn install:install-file -Dfile=ormlite-core\ormlite-core.jar -DpomFile=ormlite-core\pom.xml -Dpackaging=jar 
if not errorlevel 0 goto error_exit
echo ** Install ormlite-jdbc.jar to local Maven repository
call mvn install:install-file -Dfile=ormlite-jdbc\ormlite-jdbc.jar -DpomFile=ormlite-jdbc\pom.xml -Dpackaging=jar 
if not errorlevel 0 goto error_exit
echo ** Install ormlite-android.jar to local Maven repository
call mvn install:install-file -Dfile=ormlite-android\ormlite-android.jar -DpomFile=ormlite-android\pom.xml -Dpackaging=jar 
if not errorlevel 0 goto error_exit
echo ** Done!
goto :EOF
:error_exit 
echo ** Installation failed
