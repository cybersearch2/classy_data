@echo off
setlocal
set TYPE=maven
if "%1"=="" goto start_op
set TYPE=classic
:start_op
@echo classyjava 
pushd ..\classyjava
xcopy /Y eclipse\%TYPE%\project .\.project
xcopy /Y eclipse\%TYPE%\.classpath .
popd
@echo classyandroid
pushd ..\classyandroid
xcopy /Y eclipse\%TYPE%\project .\.project
xcopy /Y eclipse\%TYPE%\.classpath .
popd
@echo many2many-example
pushd ..\many2many-example
xcopy /Y eclipse\%TYPE%\project .\.project
xcopy /Y eclipse\%TYPE%\.classpath .
popd
