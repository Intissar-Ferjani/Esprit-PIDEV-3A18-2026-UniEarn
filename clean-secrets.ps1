# Script pour nettoyer les secrets de l'historique git
$envFile = ".env.example"

if (Test-Path $envFile) {
    $content = Get-Content $envFile -Raw
    $content = $content -replace "sk_test_[a-zA-Z0-9]{40,}", "sk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    $content = $content -replace "pk_test_[a-zA-Z0-9]{40,}", "pk_test_XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
    Set-Content $envFile $content
    git add $envFile
}

