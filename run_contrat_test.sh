#!/bin/bash
# Script pour tester facilement le CRUD des contrats
# Exécution : bash run_contrat_test.sh

echo "=========================================="
echo "  TEST DU CRUD DES CONTRATS - UniEarn"
echo "=========================================="
echo ""

# Couleurs
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

cd "C:\Users\MSI\Desktop\uniearn"

echo -e "${BLUE}1. Nettoyage et compilation...${NC}"
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation"
    exit 1
fi
echo -e "${GREEN}✓ Compilation réussie${NC}"

echo ""
echo -e "${BLUE}2. Lancement de l'application interactive...${NC}"
echo -e "${GREEN}✓ Ouvre le menu d'interaction${NC}\n"

java -cp target/classes uniearn.example.ContratTestApp

echo ""
echo -e "${GREEN}========== FIN ===========${NC}"

