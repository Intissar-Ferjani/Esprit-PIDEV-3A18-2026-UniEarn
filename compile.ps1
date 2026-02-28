#!/usr/bin/env pwsh
# Script PowerShell pour nettoyer et compiler le projet UniEarn

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "      COMPILATION COMPLÈTE - UNIEARN" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

# Nettoyage complet
Write-Host "1. Nettoyage des caches..." -ForegroundColor Yellow
if (Test-Path "target") {
    Remove-Item -Recurse -Force "target" -ErrorAction SilentlyContinue
    Write-Host "   ✓ Dossier target supprimé" -ForegroundColor Green
}

if (Test-Path ".idea") {
    Remove-Item -Recurse -Force ".idea" -ErrorAction SilentlyContinue
    Write-Host "   ✓ Dossier .idea supprimé" -ForegroundColor Green
}

# Vérifier les fichiers critiques
Write-Host ""
Write-Host "2. Vérification des fichiers critiques..." -ForegroundColor Yellow

$critical_files = @(
    "src/main/java/uniearn/model/enums/UserRole.java",
    "src/main/java/uniearn/model/entities/users/User.java",
    "src/main/java/uniearn/database/MyConnection.java",
    "src/main/java/uniearn/interfaces/IUser.java"
)

foreach ($file in $critical_files) {
    if (Test-Path $file) {
        Write-Host "   ✓ $file existe" -ForegroundColor Green
    } else {
        Write-Host "   ✗ $file MANQUANT !" -ForegroundColor Red
    }
}

# Compilation Maven
Write-Host ""
Write-Host "3. Compilation Maven..." -ForegroundColor Yellow
Write-Host ""

& mvn clean compile -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Green
    Write-Host "   ✅ COMPILATION RÉUSSIE !" -ForegroundColor Green
    Write-Host "============================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Pour lancer l'application, exécutez:" -ForegroundColor Cyan
    Write-Host "   mvn javafx:run" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "============================================" -ForegroundColor Red
    Write-Host "   ❌ ERREURS DE COMPILATION" -ForegroundColor Red
    Write-Host "============================================" -ForegroundColor Red
}

Read-Host "Appuyez sur Entrée pour continuer"

