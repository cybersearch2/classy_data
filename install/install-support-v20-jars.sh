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
mvn clean install -Dsdk.extras.compatibility.path=/opt/android-sdk-linux/extras/android_v20/support -Dproject.version=20.0.0 -Djar.version=20.0.0 -Dandroid.sdk.platform=20
cd ..
echo "** Install compatibility support 'v7 appcompat'"
cd compatibility-v7-appcompat
mvn clean install -Dsdk.extras.compatibility.path=/opt/android-sdk-linux/extras/android_v20/support -Dproject.version=20.0.0 -Djar.version=20.0.0 -Dandroid.sdk.platform=20 
cd ..
cd ..
cd ..
echo "** Done!"
return 0

 
