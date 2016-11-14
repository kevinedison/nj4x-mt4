@echo off

net stop JFXTerminalServer
winserv uninstall JFXTerminalServer
net user jfx /delete
pause