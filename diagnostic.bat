@echo off
REM Script de diagnostic des erreurs de compilation

echo ============================================
echo Diagnostic des Erreurs de Compilation
echo ============================================
echo.

echo Vérification des fichiers critiques :
echo.

echo 1. IContrat.java
if exist "src\main\java\uniearn\interfaces\IContrat.java" (
    echo   ✓ Existe
) else (
    echo   ✗ Manquant
)

echo 2. ContratService.java
if exist "src\main\java\uniearn\services\contracts\ContratService.java" (
    echo   ✓ Existe
) else (
    echo   ✗ Manquant
)

echo 3. MainApp.java
if exist "src\main\java\uniearn\app\MainApp.java" (
    echo   ✓ Existe
) else (
    echo   ✗ Manquant
)

echo.
echo Lancement de la compilation Maven...
echo.

call mvn clean compile 2>&1 | findstr /C:"ERROR" /C:"BUILD"

echo.
echo Fin du diagnostic
pause

