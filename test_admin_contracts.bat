@echo off
setlocal enabledelayedexpansion

REM Configuration des chemins
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PROJECT_DIR=C:\Users\MSI\Desktop\uniearn
set M2_REPO=%USERPROFILE%\.m2\repository
set TARGET_DIR=%PROJECT_DIR%\target\classes
set SRC_DIR=%PROJECT_DIR%\src\main\java

REM Créer le répertoire target/classes s'il n'existe pas
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"

REM Construire le classpath complet avec les ressources
set CLASSPATH=%TARGET_DIR%
set CLASSPATH=!CLASSPATH!;%PROJECT_DIR%\src\main\resources
set CLASSPATH=!CLASSPATH!;%M2_REPO%\mysql\mysql-connector-java\8.0.27\mysql-connector-java-8.0.27.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-controls\17.0.12\javafx-controls-17.0.12.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-controls\17.0.12\javafx-controls-17.0.12-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-fxml\17.0.12\javafx-fxml-17.0.12.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-fxml\17.0.12\javafx-fxml-17.0.12-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-graphics\17.0.12\javafx-graphics-17.0.12.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-graphics\17.0.12\javafx-graphics-17.0.12-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-base\17.0.12\javafx-base-17.0.12.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-base\17.0.12\javafx-base-17.0.12-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-swing\17.0.12\javafx-swing-17.0.12.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\openjfx\javafx-swing\17.0.12\javafx-swing-17.0.12-win.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\junit\junit\4.12\junit-4.12.jar
set CLASSPATH=!CLASSPATH!;%M2_REPO%\org\hamcrest\hamcrest-core\1.3\hamcrest-core-1.3.jar

echo ======================================
echo 📦 Compilation UniEarn - Interface ADMIN
echo ======================================
echo.
echo Source: %SRC_DIR%
echo Target: %TARGET_DIR%
echo.

REM Compiler tous les fichiers Java
echo ⏳ Compilation en cours...
cd /d "%PROJECT_DIR%"

REM Créer un fichier listant tous les fichiers Java
for /r "%SRC_DIR%" %%F in (*.java) do (
    echo %%F >> sources.txt
)

"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -d "%TARGET_DIR%" -cp "!CLASSPATH!" --enable-preview -source 17 @sources.txt 2>compilation.log

REM Nettoyer le fichier temporaire
if exist sources.txt del sources.txt

if errorlevel 1 (
    echo.
    echo ❌ ERREURS DE COMPILATION DÉTECTÉES:
    echo.
    type compilation.log
    pause
    exit /b 1
)

echo ✅ Compilation réussie!
echo.
echo 🚀 Lancement de l'interface ADMIN CONTRACTS...
echo.

REM Lancer l'application
set JAVAFX_PATH=%M2_REPO%\org\openjfx

"%JAVA_HOME%\bin\java.exe" ^
  -Dfile.encoding=UTF-8 ^
  --enable-preview ^
  -p "%JAVAFX_PATH%\javafx-controls\17.0.12\javafx-controls-17.0.12.jar;%JAVAFX_PATH%\javafx-controls\17.0.12\javafx-controls-17.0.12-win.jar;%JAVAFX_PATH%\javafx-fxml\17.0.12\javafx-fxml-17.0.12.jar;%JAVAFX_PATH%\javafx-fxml\17.0.12\javafx-fxml-17.0.12-win.jar;%JAVAFX_PATH%\javafx-graphics\17.0.12\javafx-graphics-17.0.12.jar;%JAVAFX_PATH%\javafx-graphics\17.0.12\javafx-graphics-17.0.12-win.jar;%JAVAFX_PATH%\javafx-base\17.0.12\javafx-base-17.0.12.jar;%JAVAFX_PATH%\javafx-base\17.0.12\javafx-base-17.0.12-win.jar;%JAVAFX_PATH%\javafx-swing\17.0.12\javafx-swing-17.0.12.jar;%JAVAFX_PATH%\javafx-swing\17.0.12\javafx-swing-17.0.12-win.jar" ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.base ^
  --add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED ^
  --add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED ^
  --add-opens javafx.base/com.sun.javafx.reflect=ALL-UNNAMED ^
  -cp "!CLASSPATH!" ^
  uniearn.example.AdminContractApp

endlocal

