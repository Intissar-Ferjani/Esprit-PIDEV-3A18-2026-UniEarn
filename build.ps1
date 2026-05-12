# Script PowerShell pour compiler le projet UniEarn sans Maven

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "      COMPILATION UNIEARN - SANS MAVEN" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Variables
$SOURCE_DIR = "src/main/java"
$OUTPUT_DIR = "target/classes"
$LIB_DIR = "$env:USERPROFILE/.m2/repository"

# Créer le répertoire de sortie
if (-not (Test-Path $OUTPUT_DIR)) {
    New-Item -ItemType Directory -Path $OUTPUT_DIR -Force | Out-Null
}

# Déterminer le chemin vers le JDK
$JDK_PATH = "C:\Program Files\Java\jdk-17"
if (-not (Test-Path $JDK_PATH)) {
    Write-Host "❌ JDK non trouvé à $JDK_PATH" -ForegroundColor Red
    exit 1
}

$JAVAC = "$JDK_PATH\bin\javac.exe"

Write-Host "Compilation des sources Java..." -ForegroundColor Yellow

# Compiler tout le projet
& $JAVAC -d $OUTPUT_DIR -sourcepath $SOURCE_DIR `
    -encoding UTF-8 `
    @(Get-ChildItem -Path $SOURCE_DIR -Filter "*.java" -Recurse | ForEach-Object { $_.FullName })

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation réussie!" -ForegroundColor Green
} else {
    Write-Host "❌ Compilation échouée!" -ForegroundColor Red
    exit 1
}

# Vérifier que les classes critiques ont été compilées
$criticalClasses = @(
    "target/classes/uniearn/database/MyConnection.class",
    "target/classes/uniearn/app/MainApp.class",
    "target/classes/uniearn/app/AppLauncher.class"
)

Write-Host ""
Write-Host "Vérification des classes critiques..." -ForegroundColor Yellow

foreach ($class in $criticalClasses) {
    if (Test-Path $class) {
        Write-Host "   ✓ $class" -ForegroundColor Green
    } else {
        Write-Host "   ✗ $class MANQUANT!" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "✅ Compilation terminée!" -ForegroundColor Green

