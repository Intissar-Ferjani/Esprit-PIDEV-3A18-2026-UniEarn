
@echo off
setlocal enabledelayedexpansion

REM Script pour compiler le projet avec Maven et les dépendances PDF

set JAVA_HOME=C:\Program Files\Java\jdk-17
set PROJECT_DIR=C:\Users\MSI\Desktop\uniearn
set M2_REPO=%USERPROFILE%\.m2\repository
set MAVEN_HOME=C:\Program Files\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3

REM Ajouter Maven au PATH
set PATH=!MAVEN_HOME!\bin;!PATH!

echo ======================================
echo 📦 Compilation UniEarn avec Maven
echo ======================================
echo.
echo Projet: %PROJECT_DIR%
echo Maven Home: %MAVEN_HOME%
echo M2 Repository: %M2_REPO%
echo.

cd /d "%PROJECT_DIR%"

REM Vérifier que Maven est disponible
echo ⏳ Vérification de Maven...
call "%MAVEN_HOME%\bin\mvn.cmd" -version > nul 2>&1
if errorlevel 1 (
    echo ❌ Maven n'est pas disponible!
    pause
    exit /b 1
)

echo ✅ Maven trouvé
echo.

REM Nettoyer et compiler
echo ⏳ Nettoyage des fichiers précédents...
call "%MAVEN_HOME%\bin\mvn.cmd" clean > nul 2>&1

echo ⏳ Compilation du projet...
call "%MAVEN_HOME%\bin\mvn.cmd" compile 2>&1

if errorlevel 1 (
    echo.
    echo ❌ ERREURS DE COMPILATION!
    pause
    exit /b 1
)

echo.
echo ✅ Compilation réussie!
echo.

REM Vérifier que les dépendances sont dans le repository
echo ⏳ Vérification des dépendances...
if not exist "%M2_REPO%\com\itextpdf\itextpdf\5.5.13.3\itextpdf-5.5.13.3.jar" (
    echo ❌ La dépendance iText n'a pas été téléchargée!
    echo ⏳ Téléchargement des dépendances...
    call "%MAVEN_HOME%\bin\mvn.cmd" dependency:resolve 2>&1
) else (
    echo ✅ Dépendance iText trouvée
)

echo.
echo ✅ Préparation complète! Vous pouvez maintenant exécuter build_and_run.bat
pause

endlocal

