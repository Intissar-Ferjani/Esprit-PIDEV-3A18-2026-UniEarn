@echo off
REM Script pour lancer l'application Admin Contracts avec JavaFX

echo Compilation du projet...
call mvn clean compile

if %ERRORLEVEL% neq 0 (
    echo Erreur lors de la compilation!
    exit /b 1
)

echo.
echo Lancement de l'application Admin Contracts...
call mvn javafx:run -Djavafx.mainClass=uniearn.test.TestAdminContracts

pause

