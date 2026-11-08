@echo off
:: Se placer a la racine du projet (la ou se trouve ce script)
cd /d "%~dp0"

:: Compilation de tous les fichiers .java du projet dans out/
dir /s /b src\*.java > sources.txt
javac -d out -cp src @sources.txt

if errorlevel 1 (
    del sources.txt
    pause
    exit /b 1
)

del sources.txt

:: Execution du programme
java -cp out Main

pause
