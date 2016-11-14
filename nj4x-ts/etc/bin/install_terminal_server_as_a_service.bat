@echo off

set JFX_HOME=%~dp0
cd "%JFX_HOME%"

echo copying PSUtils.dll to %SystemRoot%\system32
copy *.dll %SystemRoot%\system32
echo copying PSUtils_x64.dll to %SystemRoot%\syswow64
copy *.dll %SystemRoot%\syswow64
echo copying winserv.exe to %SystemRoot%\system32
copy winserv.exe %SystemRoot%\system32

set user=jfx1
set password=jfx1
set port=7789

echo _
echo INFO: We are about to add new system account:
echo INFO: User: %user%, Password: %password%
echo INFO: Press ENTER to continue (Control-C: to abort)
echo _
pause

net user %user% %password% /expires:NEVER /passwordchg:NO /active:YES /add
ntrights.exe +r SeServiceLogonRight  -u %user%

call which.cmd java.exe

echo _
echo INFO: The following java executable was detected:
echo INFO: %which%
echo INFO: Press ENTER to continue using it (Control-C: to abort)
echo _
pause

%SystemRoot%\system32\winserv.exe install JFXTS%port% -start auto -user .\%user% -password %password% %which% -Dport=%port% -jar %JFX_HOME%\nj4x-ts-2.6.2.exe
net start JFXTS%port%


echo _
echo INFO: The following service was installed:
%SystemRoot%\system32\winserv.exe showconfig JFXTS%port%
echo INFO: To uninstall it run the following:
echo INFO:      net stop JFXTS%port%
echo INFO:      winserv uninstall JFXTS%port%
echo INFO: Press ENTER to continue for final instructions
echo _
pause


echo _
echo ---------------------------------------------------------------------------------------
echo Please edit USER/PASSWORD settings of the JFXTS%port% service before starting it.
echo To do so Run services.msc, find JFXTS%port% service and edit `Logon` tab on it.
echo _
echo Currently used: User=%user%, Password=%password%
echo ---------------------------------------------------------------------------------------
pause
echo _
echo _
echo ---------------------------------------------------------------------------------------
echo Increase the size of the non-interactive desktop heap:
echo _
echo Run Registry Editor (Regedit).
echo From the HKEY_LOCAL_MACHINE subtree, go to the following key: 
echo _
echo    \System\CurrentControlSet\Control\Session Manager\SubSystems
echo _
echo Select the `Windows` value, double click.
pause
echo _
echo _
echo Increase the `SharedSection` parameter inside of the string value. 
echo _
echo SharedSection uses the following format to specify the system and desktop heaps:
echo        SharedSection=xxxx,yyyy,zzzz
echo _
echo For 32-bit system set the yyyy value to "3072", zzzz value to "100000" : SharedSection=1024,3072,100000
echo _
echo For 64-bit system set the yyyy value to "6144", zzzz value to "204800" : SharedSection=1024,6144,204800
echo _
echo Overall `Windows` value might look now like this: %SystemRoot%\system32\csrss.exe ObjectDirectory=\Windows SharedSection=1024,6144,204800 Windows=On SubSystemType=Windows ServerDll=basesrv,1 ServerDll=winsrv:UserServerDllInitialization,3 ServerDll=winsrv:ConServerDllInitialization,2 ServerDll=sxssrv,4 ProfileControl=Off MaxRequestThreads=16
echo _
echo Reboot the system.
echo ---------------------------------------------------------------------------------------
pause
echo Sure ?
pause
