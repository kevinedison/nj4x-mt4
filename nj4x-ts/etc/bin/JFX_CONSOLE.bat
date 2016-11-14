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

set PATH=%P%;%PATH%
set CLASSPATH=%CLASSPATH%;%CP%
set JFX_JVM_OPTIONS=-Djfx_activation_key=%JFX_ACTIVATION_KEY% %JFX_JVM_OPTIONS%

if "%1" == "" goto local
start %1
exit

:local
echo Environment variables PATH and CLASSPATH will be configured to use %JFX_HOME%
pause
start %1
