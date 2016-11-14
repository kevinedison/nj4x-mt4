@echo off

mkdir "%~dp0LOGS" 2>nul

set ZIPFILE="%~dp0LOGS\nj4x_logs_%date:~-4,4%%date:~-7,2%%date:~-10,2%_%time:~0,2%%time:~3,2%.zip"

del %ZIPFILE% 2>nul

pkzipc.exe -add -rec -dir=relative -include="%USERPROFILE%\.jfx_terminals\*.log" -silent %ZIPFILE%
pkzipc.exe -add -rec -dir=relative -include="%USERPROFILE%\jfx_term\*.log" -silent %ZIPFILE%

echo .
echo .
echo ----------------------------
echo Generated NJ4X logs archive:
echo %ZIPFILE%
echo ----------------------------
echo .
echo .

pause