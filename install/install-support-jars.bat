@echo off
if not exist maven-android-sdk-deployer\ goto install-android-jars
pushd maven-android-sdk-deployer
@echo ** Install compatibility support 'v4'
call mvn clean install -N
pushd extras
call mvn clean install -N
pushd compatibility-v4
call mvn clean install
popd
@echo ** Install compatibility support 'v7 appcompat'
pushd compatibility-v7-appcompat
call mvn clean install
popd
popd
popd
echo ** Done!
goto :EOF

:install-android-jars
echo Please run "install-android-jars.bat" first
