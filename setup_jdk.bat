@echo off
REM Script pour configurer le JDK et compiler le projet

echo ============================================
echo      CONFIGURATION JDK - UNIEARN
echo ============================================
echo.

REM Chercher le JDK installé
echo Recherche du JDK...

REM Vérifier plusieurs emplacements courants
set "JDK_PATH="

if exist "C:\Program Files\Java\jdk-17" (
    set "JDK_PATH=C:\Program Files\Java\jdk-17"
    echo ✓ JDK 17 trouvé: !JDK_PATH!
) else if exist "C:\Program Files\Java\jdk17" (
    set "JDK_PATH=C:\Program Files\Java\jdk17"
    echo ✓ JDK 17 trouvé: !JDK_PATH!
) else if exist "C:\Program Files (x86)\Java\jdk-17" (
    set "JDK_PATH=C:\Program Files (x86)\Java\jdk-17"
    echo ✓ JDK 17 trouvé: !JDK_PATH!
) else (
    echo ✗ JDK 17 non trouvé dans les emplacements standards
    echo.
    echo Emplacements recherchés:
    echo   - C:\Program Files\Java\jdk-17
    echo   - C:\Program Files\Java\jdk17
    echo   - C:\Program Files (x86)\Java\jdk-17
    echo.
    echo Installez JDK 17 ou configurez JAVA_HOME manuellement
    echo.
    pause
    exit /b 1
)

REM Configurer JAVA_HOME
set "JAVA_HOME=!JDK_PATH!"
echo JAVA_HOME configuré: !JAVA_HOME!

REM Compilation Maven
echo.
echo Compilation avec Maven...
echo.

mvn clean compile -DskipTests

if errorlevel 1 (
    echo.
    echo ❌ ERREUR DE COMPILATION
) else (
    echo.
    echo ✅ COMPILATION RÉUSSIE !
    echo.
    echo Pour lancer l'application:
    echo   mvn javafx:run
)

pause

