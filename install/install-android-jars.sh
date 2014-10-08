#!/bin/sh
#
# This script installs Android SDK jar files in your local Maven repository.
# The SDK level must passed as a parameter. Recommended level for Robolectric 2.3 compatibility is "4.3" (API 18).
#
# Usage:
#   install-android-jars.sh
#
if [ "x$ANDROID_HOME" = "x" ]; then 
  echo "Please set environment parameter ANDROID_HOME to location of Android SDK"
  return 1
fi
if [ "x$1" = "x" ]; then 
  echo "Usage: install-android-jars.sh SDK-LEVEL"
  echo "For example: install-android-jars.sh 4.3"
  echo "  Installs jars for Android sdk level 4.3 (API 18), which is recommended level."
  return 1
fi
echo "NOTE: Run Android SDK Manager (from command line enter:  $ANDROID_HOME\tools\android)"
echo "to check SDK Level $1 is installed, including Google Maps add-on plus Android Support Library"
if  [! -d "maven-android-sdk-deployer" ]; then
  echo "** Cloning  mosabua maven-android-sdk-deployer"
  git clone https://github.com/mosabua/maven-android-sdk-deployer.git
fi
pushd maven-android-sdk-deployer
echo "** Install sdk level $1"
mvn -q install -P "$1"
popd
echo "** Done!"

 