@echo off
REM Script de démarrage de l'application UniEarn
REM Ce script compile et lance l'application

echo ======================================
echo  UniEarn - Application Launcher
echo ======================================
echo.

REM Vérifier si Maven est disponible
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo Maven n'a pas été trouvé. Tentative avec le chemin complet...
    set MVN_CMD="C:\Program Files\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd"
) else (
    set MVN_CMD=mvn
)

REM Compiler le projet
echo [1/3] Compilation du projet...
%MVN_CMD% clean compile
if %errorlevel% neq 0 (
    echo Erreur lors de la compilation!
    pause
    exit /b 1
)

REM Lancer l'application
echo [2/3] Lancement de l'application...
%MVN_CMD% javafx:run
if %errorlevel% neq 0 (
    echo Erreur lors du lancement de l'application!
    pause
    exit /b 1
)

echo.
echo ✅ Application fermée
pause

