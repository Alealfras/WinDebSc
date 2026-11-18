@echo off
title WinDebSc Launcher
cd /d "%~dp0"
if not exist bin mkdir bin
javac -cp "lib/*" -d bin src\com\windebsc\Main.java src\com\windebsc\core\*.java src\com\windebsc\ui\*.java
start javaw -cp "bin;lib\*" com.windebsc.Main
exit
