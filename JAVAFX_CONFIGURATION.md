# Configuration JavaFX pour UniEarn - Gestion des Contrats

## ✅ Solution Complète pour le Problème JavaFX

Le problème **"JavaFX runtime components are missing"** est résolu avec la configuration suivante :

## 📋 Configuration Effectuée

### 1. **Maven (pom.xml)**
- ✅ Ajouté le plugin `javafx-maven-plugin` v0.0.8
- ✅ Configuré le `maven-surefire-plugin` avec les arguments `--add-modules`
- ✅ Arguments modules nécessaires:
  ***REMOVED***
  --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.base
  --add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
  ***REMOVED***

### 2. **IntelliJ IDEA (.idea/runConfigurations/JavaFXApps.xml)**
- ✅ Créé les configurations de lancement pour :
  - `TestAdminContracts`
  - `TestFreelancerContracts`
  - `AdminContractApp`
  - `FreelancerContractApp`

## 🚀 Comment Utiliser

### Option 1: Depuis IntelliJ IDEA (Recommandé)
1. Cliquez sur **Run** → **Edit Configurations**
2. Les configurations sont déjà présentes (JavaFXApps.xml)
3. Sélectionnez la configuration souhaitée :
   - **TestAdminContracts** : Interface Admin (test simple)
   - **TestFreelancerContracts** : Interface Freelancer (test simple)
   - **AdminContractApp** : Interface Admin (avec FXML)
   - **FreelancerContractApp** : Interface Freelancer (avec FXML)
4. Cliquez sur **Run**

### Option 2: Via la Ligne de Commande
***REMOVED***bash
# Pour Admin
mvn javafx:run -Djavafx.mainClass=uniearn.test.TestAdminContracts

# Pour Freelancer
mvn javafx:run -Djavafx.mainClass=uniearn.test.TestFreelancerContracts

# Avec compilation complète
mvn clean compile javafx:run -Djavafx.mainClass=uniearn.test.TestAdminContracts
***REMOVED***

### Option 3: Compilation et Exécution Personnalisée
***REMOVED***bash
# Compiler
mvn clean compile

# Exécuter avec arguments VM complets
mvn exec:java@admin-contracts

# ou
java --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.base \
     --add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED \
     -cp target/classes:target/lib/* \
     uniearn.test.TestAdminContracts
***REMOVED***

## 📁 Applications Disponibles

### Interfaces de Test (Classes)
- **TestAdminContracts** : Interface Admin sans FXML (composants créés en Java)
- **TestFreelancerContracts** : Interface Freelancer sans FXML

### Interfaces Complètes (FXML)
- **AdminContractApp** : Charge `admin_contracts.fxml`
- **FreelancerContractApp** : Charge `freelancer_contracts.fxml`
- **ClientContractApp** : Charge `client_contracts.fxml`

## 🎨 Fonctionnalités

### Interface Admin
- ✅ CRUD complet des contrats
- ✅ Filtrage par statut
- ✅ Recherche par ID ou Type
- ✅ Gestion des templates
- ✅ Statistiques des contrats

### Interface Freelancer
- ✅ Liste des contrats assignés
- ✅ Filtrage par statut
- ✅ Signature des contrats
- ✅ Affichage des détails
- ✅ Statistiques personnalisées

### Interface Client
- ✅ Création de contrats
- ✅ Sélection de templates
- ✅ Signature des contrats
- ✅ Export en PDF

## 🔧 Dépannage

### Si vous avez toujours l'erreur "JavaFX runtime components are missing":

1. **Vérifiez le JDK**:
   ***REMOVED***bash
   java --version
   # Doit être Java 17 ou plus
   ***REMOVED***

2. **Vérifiez Maven**:
   ***REMOVED***bash
   mvn --version
   # Version 3.6.0 ou plus
   ***REMOVED***

3. **Reconstructisez le projet**:
   ***REMOVED***bash
   mvn clean compile
   ***REMOVED***

4. **Invalidez le cache IntelliJ**:
   - File → Invalidate Caches → Invalidate and Restart

5. **Mettez à jour les dépendances**:
   ***REMOVED***bash
   mvn dependency:resolve
   mvn dependency:tree
   ***REMOVED***

## 📝 Arguments VM Complets Expliqués

***REMOVED***
--add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.base
***REMOVED***
- Charge tous les modules JavaFX nécessaires

***REMOVED***
--add-opens javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
***REMOVED***
- Autorise l'accès aux classes internes de JavaFX (nécessaire pour certaines opérations)

## ✨ Résumé de la Configuration

| Élément | Configuration |
|---------|--------------|
| **JDK** | Java 17+ |
| **JavaFX** | 17.0.12 |
| **Maven Compiler** | 3.13.0 |
| **JavaFX Maven Plugin** | 0.0.8 |
| **Surefire Plugin** | 3.0.0-M9 |

---

**Status**: ✅ Configuration définitive et complète
**Date**: 2026-02-14

