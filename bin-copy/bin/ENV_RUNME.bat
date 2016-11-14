@echo off

:: Set JRE_HOME
for /d %%i in ("%ProgramFiles%\Java\jdk*") do set JRE_HOME=%%i\jre
for /d %%i in ("%ProgramFiles%\Java\jre*") do set JRE_HOME=%%i
for /d %%i in ("%ProgramFiles(x86)%\Java\jdk*") do set JRE_HOME=%%i\jre
for /d %%i in ("%ProgramFiles(x86)%\Java\jre*") do set JRE_HOME=%%i

set P="%JRE_HOME%\bin";"%JRE_HOME%\bin\client"

set JFX_HOME=%~dp0
cd "%JFX_HOME%"

set CP=%JFX_HOME%..\lib\jfx-2.6.2.jar
set CP=%CP%;%JFX_HOME%..\lib\log4j.jar

echo To run Java strategies inside MT4 terminal
echo 1. Set all -D/-X options via JFX_JVM_OPTIONS environment variable (e.g. JFX_JVM_OPTIONS=-Xmx64M -Djfx_activation_key=123)
echo 2. Your PATH and CLASSPATH environment variables should look like ...
pause
echo PATH=%P%;%PATH%
echo _
echo CLASSPATH=%CLASSPATH%;%CP%
echo __________________________________________________________________
echo To get this ...
echo ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
pause
echo _
echo Prepend existing PATH with 
echo _
echo %P%
echo _
pause
echo _
echo Append existing CLASSPATH with 
echo _
echo %CP%
echo _
echo .the end.
pause