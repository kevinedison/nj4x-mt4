@echo off

if "%1" == "" goto noArg


set which=%~$PATH:1
if "%which%" == "" goto notFound
goto end


:noArg
echo No Argument specified
goto end


:notFound
echo "%1" not found in PATH
set which=%1


:end
