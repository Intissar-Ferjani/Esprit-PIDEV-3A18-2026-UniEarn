$env:PATH = "C:\tools\apache-maven-3.8.6\bin;$env:PATH"
cd C:\Users\MSI\Desktop\uniearn2
# Copier les dépendances dans le répertoire target/lib
mvn dependency:copy-dependencies -DoutputDirectory=target/lib -q
# Créer le classpath avec toutes les JARs
$libs = Get-ChildItem "target/lib" -Filter "*.jar" | ForEach-Object { $_.FullName }
$classpath = "target/classes;" + ([string]::Join(";", $libs))
# Ajouter aussi les dépendances du plugin
$repo = $env:USERPROFILE + "/.m2/repository"
if (Test-Path $repo) {
    $libs2 = Get-ChildItem $repo -Filter "*.jar" -Recurse | Select-Object -First 50
    if ($libs2) {
        $classpath += ";" + ([string]::Join(";", ($libs2 | ForEach-Object { $_.FullName })))
    }
}
Write-Host "CLASSPATH configuré avec" (($classpath -split ";").Count) "items"
# Lancer l'application
$javaHome = "C:\Program Files\Java\jdk-17"
& "$javaHome/bin/java" -cp $classpath uniearn.app.AppLauncher
