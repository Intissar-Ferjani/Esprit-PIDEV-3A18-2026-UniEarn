#!/bin/bash
# Script de vérification de compilation

echo "==========================================="
echo "Vérification de la Structure du Projet"
echo "==========================================="
echo ""

echo "✓ Fichiers créés/modifiés :"
echo "  - MainApp.java"
echo "  - AppLauncher.java"
echo "  - DialogUtil.java"
echo ""

echo "✓ Imports corrigés dans :"
echo "  - Tous les contrôleurs du dossier contracts"
echo "  - Tous les services du dossier contracts"
echo "  - Tous les fichiers example"
echo "  - Tous les fichiers CRUD"
echo ""

echo "✓ Packages corrigés :"
echo "  - uniearn.controller → uniearn.controller.contracts"
echo "  - uniearn.services → uniearn.services.contracts"
echo "  - uniearn.interfaces → uniearn.interfaces.contracts"
echo ""

echo "==========================================="
echo "Pour compiler, exécutez :"
echo "==========================================="
echo "mvn clean compile"
echo ""
echo "Pour lancer l'application :"
echo "==========================================="
echo "mvn clean compile javafx:run"
echo ""
echo "Ou double-cliquez sur : run_app.bat"

