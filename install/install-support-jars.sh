#!/bin/sh
#
# This script installs Android compatibility jar files in your local Maven repository.
#
# Usage:
#   install-support-jars.sh
#
if  [ ! -f "maven-android-sdk-deployer/pom.xml" ]; then
echo "Please run "install-android-jars.sh" first"
return 1
fi
cd maven-android-sdk-deployer
echo "** Install compatibility support 'v4'"
mvn clean install -N
cd extras
mvn clean install -N
cd compatibility-v4
mvn clean install
cd ..
echo "** Install compatibility support 'v7 appcompat'"
cd compatibility-v7-appcompat
mvn clean install
cd ..
cd ..
cd ..
echo "** Done!"
return 0

 
