
set which=%~$PATH:1

if "%which%" == "" goto notFound
goto end

:notFound
REM echo "%1" not found in PATH

for /d %%i in ("%ProgramFiles%\Java\jdk*") do set JRE_HOME=%%i\jre
for /d %%i in ("%ProgramFiles%\Java\jre*") do set JRE_HOME=%%i
for /d %%i in ("%ProgramFiles(x86)%\Java\jdk*") do set JRE_HOME=%%i\jre
for /d %%i in ("%ProgramFiles(x86)%\Java\jre*") do set JRE_HOME=%%i

set PATH=%PATH%;"%JRE_HOME%\bin";"%JRE_HOME%\bin\client"

:end
