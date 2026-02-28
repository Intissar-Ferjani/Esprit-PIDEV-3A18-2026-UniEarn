@echo off
REM Script de nettoyage complet et recompilation

echo ============================================
echo Nettoyage Complet du Projet UniEarn
echo ============================================
echo.

echo Suppression des caches Maven...
rmdir /s /q target 2>nul
rmdir /s /q .idea 2>nul
del /q *.log 2>nul

echo.
echo ============================================
echo Recompilation Complète
echo ============================================
echo.

call mvn clean compile -DskipTests

echo.
if %errorlevel% equ 0 (
    echo ✅ Compilation réussie !
    echo.
    echo Lancement de l'application...
    call mvn javafx:run
) else (
    echo ❌ La compilation a échoué
    echo Vérifiez les erreurs ci-dessus
)

pause

