@echo off
REM Script final de compilation et exécution

setlocal enabledelayedexpansion

echo ============================================
echo      COMPILATION FINAL - UNIEARN
echo ============================================
echo.

REM Nettoyage complet
echo Nettoyage des caches...
if exist target rmdir /s /q target >nul 2>&1
if exist .idea rmdir /s /q .idea >nul 2>&1

echo.
echo Compilation Maven...
echo.

call mvn clean compile -X 2>&1 | findstr /C:"BUILD" /C:"ERROR" /C:"SUCCESS"

if %errorlevel% equ 0 (
    echo.
    echo ✅ COMPILATION RÉUSSIE !
    echo.
    echo Lancement de l'application...
    call mvn javafx:run
) else (
    echo.
    echo ❌ ERREURS DE COMPILATION
    echo Exécutez la commande suivante pour plus de détails:
    echo   mvn clean compile
    echo.
)

pause

