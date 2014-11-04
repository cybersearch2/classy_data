#!/bin/sh
#
# This script copies Eclipse project files configured for Classic or Maven (default) mode to the relevant projects.
#
# Usage:
#   setup-eclipse.sh [classic]
#
export ECLIPSE_TYPE=maven
if [ "x$1" = "x" ]; then 
export ECLIPSE_TYPE=eclipse
fi
echo "Set up $ECLIPSE_TYPE Eclipse"

echo "classyjava" 
cd ../classyjava
cp eclipse/$ECLIPSE_TYPE/project ./.project
xcopy /Y eclipse\$ECLIPSE_TYPE\.classpath .
cd ..
echo "classyandroid"
cd ../classyandroid
cp eclipse/$ECLIPSE_TYPE/project ./.project
cp eclipse/$ECLIPSE_TYPE/.classpath .
cd ..
echo "many2many-example"
cd ../many2many-example
cp eclipse/$ECLIPSE_TYPE/project ./.project
cp eclipse/$ECLIPSE_TYPE/.classpath .
cd ..
