@echo off
REM Script de compilation UniEarn - Avec détection automatique de Maven

setlocal enabledelayedexpansion

echo ============================================
echo      COMPILATION UNIEARN - FINAL
echo ============================================
echo.

REM Nettoyage complet
echo [1/4] Nettoyage des caches...
if exist target (
    rmdir /s /q target >nul 2>&1
    echo  ✓ Dossier target supprimé
)

if exist ".idea" (
    rmdir /s /q ".idea" >nul 2>&1
    echo  ✓ Dossier .idea supprimé
)

REM Trouver Maven
echo.
echo [2/4] Recherche de Maven...

set "MAVEN_CMD="

REM Chercher Maven dans IntelliJ
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd" (
    set "MAVEN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd"
    echo  ✓ Maven trouvé dans IntelliJ
) else if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition\plugins\maven\lib\maven3\bin\mvn.cmd" (
    set "MAVEN_CMD=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition\plugins\maven\lib\maven3\bin\mvn.cmd"
    echo  ✓ Maven trouvé dans IntelliJ Community
) else if exist "C:\Program Files\Apache\maven\bin\mvn.cmd" (
    set "MAVEN_CMD=C:\Program Files\Apache\maven\bin\mvn.cmd"
    echo  ✓ Maven trouvé dans Program Files
) else (
    REM Essayer la commande globale
    where mvn >nul 2>&1
    if !errorlevel! equ 0 (
        set "MAVEN_CMD=mvn"
        echo  ✓ Maven trouvé dans PATH
    ) else (
        echo  ✗ Maven non trouvé !
        echo.
        echo Emplacements recherchés:
        echo   - C:\Program Files\JetBrains\IntelliJ IDEA 2025.3.2\plugins\maven\lib\maven3\bin\mvn.cmd
        echo   - C:\Program Files\JetBrains\IntelliJ IDEA Community Edition\plugins\maven\lib\maven3\bin\mvn.cmd
        echo   - C:\Program Files\Apache\maven\bin\mvn.cmd
        echo.
        echo Installez Maven ou configurez le PATH
        echo Téléchargez depuis: https://maven.apache.org/download.cgi
        echo.
        pause
        exit /b 1
    )
)

REM Attendre un moment
timeout /t 1 /nobreak >nul

REM Compilation
echo.
echo [3/4] Compilation Maven (cela peut prendre 30-60 secondes)...
echo.

call "!MAVEN_CMD!" clean compile -DskipTests

REM Vérifier le résultat
if errorlevel 1 (
    echo.
    echo ============================================
    echo  ❌ ERREUR DE COMPILATION
    echo ============================================
    echo.
    echo Vérifiez les messages d'erreur ci-dessus
    echo.
) else (
    echo.
    echo ============================================
    echo  ✅ COMPILATION RÉUSSIE !
    echo ============================================
    echo.
    echo [4/4] Prochaines étapes:
    echo.
    echo  Option A - Lancer l'application:
    echo    "!MAVEN_CMD!" javafx:run
    echo.
    echo  Option B - Rouvrir dans IntelliJ:
    echo    1. Fermer IntelliJ complètement
    echo    2. Relancer IntelliJ
    echo    3. Ouvrir le projet
    echo.
)

echo.
pause

