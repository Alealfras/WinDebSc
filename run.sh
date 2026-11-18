#!/bin/bash
# WinDebSc Launcher para macOS / Linux (Modo prueba de interfaz)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

mkdir -p bin
javac -cp "lib/*" -d bin src/com/windebsc/**/*.java
java -cp "bin:lib/*" com.windebsc.Main
