@echo off
REM Script pour lancer l'application avec Maven

setlocal enabledelayedexpansion

echo ============================================
echo      LANCEMENT UNIEARN
echo ============================================
echo.

REM Configurer Maven
set "MAVEN_HOME=C:\tools\apache-maven-3.8.6"
set "PATH=!MAVEN_HOME!\bin;!PATH!"

REM Aller au répertoire du projet
cd /d "C:\Users\MSI\Desktop\uniearn2"

REM Compiler et lancer
echo Compilation et lancement de l'application...
echo.

call mvn clean compile exec:java -Dexec.mainClass="uniearn.app.AppLauncher" -DskipTests

echo.
echo ============================================
echo      FERMETURE DE L'APPLICATION
echo ============================================
pause

