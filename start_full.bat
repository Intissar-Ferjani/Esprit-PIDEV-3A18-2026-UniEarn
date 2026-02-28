@echo off
REM Script simplifié pour compiler et lancer l'application
REM Exécute automatiquement les étapes nécessaires pour l'export PDF

setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PROJECT_DIR=C:\Users\MSI\Desktop\uniearn
set M2_REPO=%USERPROFILE%\.m2\repository
set MAVEN_HOME=C:\Program Files\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║    UniEarn - Démarrage Complet (Compilation + Exécution)       ║
echo ║    Export PDF Activé ✓                                         ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

cd /d "%PROJECT_DIR%"

REM Étape 1: Vérifier Maven
echo [1/5] Vérification de Maven...
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    echo ❌ Maven n'a pas été trouvé
    echo    Attendu: %MAVEN_HOME%\bin\mvn.cmd
    pause
    exit /b 1
)
echo ✅ Maven trouvé

REM Étape 2: Vérifier si les dépendances iText existent
echo.
echo [2/5] Vérification des dépendances PDF...
if not exist "%M2_REPO%\com\itextpdf\itextpdf\5.5.13.3\itextpdf-5.5.13.3.jar" (
    echo ⚠️  Dépendance iText manquante, téléchargement en cours...
    call "%MAVEN_HOME%\bin\mvn.cmd" dependency:resolve -q > nul 2>&1
    if errorlevel 1 (
        echo ⚠️  Téléchargement via Maven...
        call "%MAVEN_HOME%\bin\mvn.cmd" clean compile -q
    )
) else (
    echo ✅ Dépendance iText trouvée
)

REM Étape 3: Compiler
echo.
echo [3/5] Compilation du projet...
call "%MAVEN_HOME%\bin\mvn.cmd" clean compile -q 2>nul
if errorlevel 1 (
    echo ❌ Erreur lors de la compilation
    call "%MAVEN_HOME%\bin\mvn.cmd" clean compile
    pause
    exit /b 1
)
echo ✅ Compilation réussie

REM Étape 4: Vérifier les dépendances finales
echo.
echo [4/5] Vérification finale...
if not exist "%M2_REPO%\com\itextpdf\itextpdf\5.5.13.3\itextpdf-5.5.13.3.jar" (
    echo ❌ ERREUR: iText n'a pas pu être téléchargé
    echo    Tentez: build_compile.bat
    pause
    exit /b 1
)
echo ✅ Toutes les dépendances sont présentes

REM Étape 5: Démarrer l'application
echo.
echo [5/5] Démarrage de l'application...
echo.
call build_and_run.bat

endlocal

