@echo off
REM Script pour lancer les applications JavaFX
REM Usage: run_app.bat <MainClass>

setlocal enabledelayedexpansion

set JAVA_HOME=C:\Program Files\Java\jdk-17
set M2_REPO=%USERPROFILE%\.m2\repository

REM Classpath avec toutes les dépendances Maven
set CLASSPATH=C:\Users\MSI\Desktop\uniearn\target\classes
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

REM Lancer l'application
"%JAVA_HOME%\bin\java.exe" ^
  -Dfile.encoding=UTF-8 ^
  --enable-preview ^
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.base ^
  --add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED ^
  -cp "!CLASSPATH!" ^
  %1

endlocal

