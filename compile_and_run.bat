@echo off
setlocal enabledelayedexpansion

REM Configuration
set JAVA_HOME=C:\Program Files\Java\jdk-17
set M2_REPO=%USERPROFILE%\.m2\repository
set PROJECT_DIR=C:\Users\MSI\Desktop\uniearn

REM Construire le classpath
set CLASSPATH=%PROJECT_DIR%\target\classes
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
echo 📦 Compilation et Lancement UniEarn
echo ======================================

REM Compiler d'abord
cd /d %PROJECT_DIR%

echo.
echo 1️⃣ Compilation du projet...
echo.

"%JAVA_HOME%\bin\javac.exe" ^
  -d target\classes ^
  -cp "!CLASSPATH!" ^
  --enable-preview ^
  -encoding UTF-8 ^
  src\main\java\uniearn\**\*.java 2>compile_errors.txt

if errorlevel 1 (
    echo ❌ Erreurs de compilation!
    type compile_errors.txt
    pause
    exit /b 1
)

echo ✅ Compilation réussie!
echo.
echo 2️⃣ Lancement de l'application...
echo.

REM Lancer l'application
"%JAVA_HOME%\bin\java.exe" ^
  -Dfile.encoding=UTF-8 ^
  --enable-preview ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.base ^
  --add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED ^
  -cp "!CLASSPATH!" ^
  uniearn.example.AppLauncher

endlocal

