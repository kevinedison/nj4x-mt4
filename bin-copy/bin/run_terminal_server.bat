@echo off

set JFX_HOME=%~dp0
cd "%JFX_HOME%"

nj4x-ts-2.6.2.exe 

REM nj4x-ts-2.6.2.exe SW_HIDE false 
REM java -DSW_HIDE=false -jar nj4x-ts-2.6.2.exe 

REM nj4x-ts-2.6.2.exe use_mstsc true 
REM java -Duse_mstsc=true -jar nj4x-ts-2.6.2.exe 

