@echo off
if "%ANDROID_HOME%"=="" goto android-home-not-set
if "%1"=="" goto sdk_param_required
echo NOTE: Run Android SDK Manager (from command line it is ^> %ANDROID_HOME%\tools\android)
echo to check SDK Level "%1" is installed, including Google Maps add-on plus Android Support Library
if exist maven-android-sdk-deployer\ goto deployer-install
@echo ** Cloning  mosabua maven-android-sdk-deployer
git clone https://github.com/mosabua/maven-android-sdk-deployer.git
:deployer-install
pushd maven-android-sdk-deployer
@echo ** Install sdk level "%1"
call mvn install -P "%1"
popd
echo ** Done!
goto :EOF

:sdk_param_required
echo Usage: install-android-jars SDK-LEVEL
echo eg. ^> install-android-jars 4.3
echo   Installs jars for Android sdk level 4.3 (API 18), which is recommended level.
goto :EOF

:android-home-not-set
echo Please set environment parameter ANDROID_HOME to location of Android SDK