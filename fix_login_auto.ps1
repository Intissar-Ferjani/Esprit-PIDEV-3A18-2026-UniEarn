# ================================================================
# SCRIPT DE RÉPARATION AUTOMATIQUE - Problème de connexion
# ================================================================
# Ce script supprime l'utilisateur problématique de la base de données
# Vous pourrez ensuite créer un nouveau compte via l'application

Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "   RÉPARATION AUTOMATIQUE - Problème de connexion UniEarn" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
$dbName = "uniearn"
$email = "clientjava@gmail.com"

# Vérifier si MySQL existe
if (-not (Test-Path $mysqlPath)) {
    Write-Host "❌ MySQL n'a pas été trouvé à: $mysqlPath" -ForegroundColor Red
    Write-Host "⚠️  Veuillez modifier le chemin dans le script" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Chemins possibles:" -ForegroundColor Cyan
    Write-Host "  - C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    Write-Host "  - C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe"
    Write-Host "  - C:\xampp\mysql\bin\mysql.exe"
    Write-Host ""
    Read-Host "Appuyez sur Entrée pour quitter"
    exit
}

Write-Host "✓ MySQL trouvé" -ForegroundColor Green
Write-Host ""

# Demander les identifiants
Write-Host "Veuillez entrer vos identifiants MySQL:" -ForegroundColor Yellow
$username = Read-Host "Nom d'utilisateur (généralement 'root')"
$password = Read-Host "Mot de passe" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "   ÉTAPE 1 : Vérification de l'utilisateur" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan

# Créer le fichier SQL temporaire
$sqlFile = "temp_check_user.sql"
$sqlContent = @"
USE $dbName;
SELECT
    u.idUser,
    u.name,
    u.email,
    u.activated,
    LENGTH(u.password) as pwd_length
FROM user u
WHERE u.email = '$email';
"@

$sqlContent | Out-File -FilePath $sqlFile -Encoding UTF8

# Exécuter la requête
Write-Host "⏳ Vérification de l'utilisateur $email..." -ForegroundColor Yellow
$result = & $mysqlPath -u $username -p$passwordPlain -e "source $sqlFile" 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erreur de connexion à MySQL" -ForegroundColor Red
    Write-Host $result
    Remove-Item $sqlFile -ErrorAction SilentlyContinue
    Read-Host "Appuyez sur Entrée pour quitter"
    exit
}

Write-Host $result
Write-Host ""

# Demander confirmation
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "   ÉTAPE 2 : Suppression de l'utilisateur" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚠️  ATTENTION: Cette action va supprimer l'utilisateur $email" -ForegroundColor Yellow
Write-Host "   Vous devrez recréer le compte via l'application après." -ForegroundColor Yellow
Write-Host ""
$confirm = Read-Host "Voulez-vous continuer? (oui/non)"

if ($confirm -ne "oui") {
    Write-Host "❌ Opération annulée" -ForegroundColor Red
    Remove-Item $sqlFile -ErrorAction SilentlyContinue
    Read-Host "Appuyez sur Entrée pour quitter"
    exit
}

# Créer le script de suppression
$sqlDeleteFile = "temp_delete_user.sql"
$sqlDeleteContent = @"
USE $dbName;

-- Supprimer le client
DELETE FROM client WHERE userID = (SELECT idUser FROM user WHERE email = '$email');

-- Supprimer l'utilisateur
DELETE FROM user WHERE email = '$email';

-- Vérifier
SELECT COUNT(*) as remaining FROM user WHERE email = '$email';
"@

$sqlDeleteContent | Out-File -FilePath $sqlDeleteFile -Encoding UTF8

Write-Host ""
Write-Host "⏳ Suppression en cours..." -ForegroundColor Yellow

$deleteResult = & $mysqlPath -u $username -p$passwordPlain -e "source $sqlDeleteFile" 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Utilisateur supprimé avec succès!" -ForegroundColor Green
    Write-Host $deleteResult
} else {
    Write-Host ""
    Write-Host "❌ Erreur lors de la suppression" -ForegroundColor Red
    Write-Host $deleteResult
}

# Nettoyer
Remove-Item $sqlFile -ErrorAction SilentlyContinue
Remove-Item $sqlDeleteFile -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "   PROCHAINES ÉTAPES" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Lancez l'application UniEarn" -ForegroundColor Yellow
Write-Host "2. Allez dans 'Inscription'" -ForegroundColor Yellow
Write-Host "3. Créez un nouveau compte avec l'email: $email" -ForegroundColor Yellow
Write-Host "4. Connectez-vous avec le nouveau mot de passe" -ForegroundColor Yellow
Write-Host ""
Write-Host "✅ Le nouveau compte aura un mot de passe correctement haché!" -ForegroundColor Green
Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan

Read-Host "Appuyez sur Entrée pour quitter"

