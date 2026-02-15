# 🎨 GUIDE D'UTILISATION : INTERFACE JAVAFX DES CONTRATS

## 📋 Table des matières
1. [Vue d'ensemble](#vue-densemble)
2. [Lancer l'application](#lancer-lapplication)
3. [Fonctionnalités](#fonctionnalités)
4. [Utilisation complète](#utilisation-complète)
5. [Couleurs et Design](#couleurs-et-design)
6. [Troubleshooting](#troubleshooting)

---

## 🎯 Vue d'ensemble

Tu as maintenant une **interface graphique complète** pour gérer les contrats avec :

✅ **Header moderne** avec boutons d'action (bleu #1E56DB)
✅ **Tableau interactif** affichant tous les contrats
✅ **Formulaire d'ajout** avec validation en temps réel
✅ **Design moderne** avec tes couleurs personnalisées
✅ **Footer avec statistiques** en temps réel

### Couleurs utilisées :
- 🔵 **Primaire** : `#1E56DB` (Bleu) - Headers, boutons secondaires
- 🟢 **Secondaire** : `#4CAF50` (Vert) - Bouton principal, succès
- ⚪ **Arrière-plan** : `#F5F5F5` (Gris clair) - Fond principal
- ⚫ **Texte** : `#333333` (Noir) - Texte principal
- ⚪ **Blanc** : `#FFFFFF` - Cartes, tableaux

---

## 🚀 Lancer l'application

### Option 1 : Depuis le terminal
***REMOVED***bash
cd C:\Users\MSI\Desktop\uniearn

# Compiler
mvn clean compile

# Lancer l'application JavaFX
mvn javafx:run -Djavafx.mainClass=uniearn.example.ContratJavaFXApp
***REMOVED***

### Option 2 : Depuis IntelliJ IDEA
1. Ouvre `ContratJavaFXApp.java`
2. Clique sur le bouton ▶️ (Run) en haut à droite
3. L'application s'ouvre automatiquement

### Option 3 : Package exécutable
***REMOVED***bash
mvn clean package
java -module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -cp target/uniearn_java-1.0-SNAPSHOT.jar uniearn.example.ContratJavaFXApp
***REMOVED***

---

## ✨ Fonctionnalités

### 1️⃣ **Ajouter un Contrat** (Bouton vert ➕)

Clique sur le bouton **"➕ Ajouter un Contrat"** pour ouvrir un formulaire rempli de champs :

- **ID Client** * (obligatoire) - Entier
- **ID Projet** * (obligatoire) - Entier
- **Montant (DA)** * (obligatoire) - Nombre décimal
- **ID Paiement** (optionnel) - Entier
- **Date de Début** * (obligatoire) - Date
- **Date de Fin** * (obligatoire) - Date (doit être ≥ début)
- **Statut** - ComboBox : Brouillon, Signé Client, Signé Freelancer, Complété

**Validation en temps réel** : Le bouton "Ajouter" est grisé tant que les champs obligatoires ne sont pas remplis correctement.

### 2️⃣ **Afficher les Contrats** (Tableau)

Le tableau affiche tous les contrats avec :
- ID du contrat
- ID du client
- ID du projet
- Montant
- **Statut** (coloré) :
  - 🟠 Brouillon (orange)
  - 🔵 Signé Client (bleu)
  - 🟣 Signé Freelancer (violet)
  - 🟢 Complété (vert)

### 3️⃣ **Éditer un Contrat** (Bouton ✏️)

Clique sur le bouton **"✏️"** d'un contrat pour l'éditer (à implémenter).

### 4️⃣ **Supprimer un Contrat** (Bouton 🗑️)

Clique sur le bouton **"🗑️"** pour supprimer un contrat avec confirmation.

### 5️⃣ **Rafraîchir le Tableau** (Bouton 🔄)

Clique sur **"🔄 Rafraîchir"** pour recharger les données depuis la BD.

### 6️⃣ **Exporter les Données** (Bouton 📊)

Clique sur **"📊 Exporter"** pour exporter les contrats (à implémenter).

### 7️⃣ **Statistiques en temps réel** (Footer)

Le footer affiche :
- Total de contrats
- Nombre de contrats par statut

---

## 📖 Utilisation Complète

### Créer un nouveau contrat
***REMOVED***
1. Clique sur "➕ Ajouter un Contrat"
2. Remplis les champs :
   - ID Client: 5
   - ID Projet: 2
   - Montant: 50000
   - Date début: 2026-02-13
   - Date fin: 2026-04-13
   - Statut: 0 - Brouillon
   - ID Paiement: (laisse vide ou entre 1)
3. Clique "✓ Ajouter"
4. Un message confirme l'ajout
5. La fenêtre se ferme et le tableau se rafraîchit
***REMOVED***

### Supprimer un contrat
***REMOVED***
1. Trouve le contrat dans le tableau
2. Clique sur le bouton "🗑️"
3. Confirme la suppression
4. Le contrat est supprimé et le tableau se rafraîchit
***REMOVED***

### Voir les statistiques
***REMOVED***
Regarde en bas du tableau dans le "Footer"
Tu vois le total et la répartition par statut
***REMOVED***

---

## 🎨 Couleurs et Design

### En-tête (Header)
- Fond : `#1E56DB` (Bleu foncé)
- Texte : Blanc
- Contient le titre, le sous-titre et les 3 boutons principaux

### Boutons
1. **"➕ Ajouter"** - Vert `#4CAF50` → Ajouter un contrat
2. **"🔄 Rafraîchir"** - Bleu `#2196F3` → Recharger les données
3. **"📊 Exporter"** - Orange `#FF9800` → Exporter les données

### Tableau
- Fond : Blanc
- Bordure : Gris clair `#DDDDDD`
- **Statut** coloré selon le statut du contrat
- **Boutons d'action** : Bleu (éditer) et Rouge (supprimer)

### Pied de page (Footer)
- Fond : Blanc
- Affiche le statut et les statistiques
- Séparateur gris en haut

### Formulaire d'ajout
- Fond : Gris clair `#F5F5F5`
- Champs : Bordure grise `#CCCCCC`
- **Focus** : Bordure bleue `#1E56DB`
- Boutons colorés selon l'action

---

## 🛠️ Fichiers créés

***REMOVED***
uniearn/
├── src/main/resources/
│   ├── contracts/
│   │   └── contrat_add_dialog.fxml ✅ (Interface FXML)
│   └── styles/
│       └── styles.css ✅ (Styles CSS)
│
├── src/main/java/uniearn/
│   ├── controller/
│   │   └── ContratDialogController2.java ✅ (Contrôleur amélioré)
│   └── example/
│       └── ContratJavaFXApp.java ✅ (App principale)
│
└── GUIDE_JAVAFX_CONTRATS.md ✅ (Ce fichier)
***REMOVED***

---

## 🐛 Troubleshooting

### ❌ "Impossible de charger le formulaire"
**Cause** : Le fichier `contrat_add_dialog.fxml` n'est pas trouvé

**Solution** :
***REMOVED***bash
# Assure-toi que le FXML est dans les ressources
src/main/resources/contracts/contrat_add_dialog.fxml

# Recompile
mvn clean compile
***REMOVED***

### ❌ "La base de données n'est pas accessible"
**Cause** : MyConnection ne peut pas se connecter

**Solution** :
1. Vérifie que MySQL est lancé
2. Vérifie les identifiants dans `MyConnection.java`
3. Assure-toi que la BD `uniearn_db` existe

### ❌ "Les boutons n'ont pas les bonne couleurs"
**Cause** : Le CSS n'est pas chargé

**Solution** :
***REMOVED***bash
# Recompile pour inclure les ressources
mvn clean compile

# Assure-toi que styles.css existe
src/main/resources/styles/styles.css
***REMOVED***

### ❌ "Le formulaire ne se ferme pas après ajout"
**Cause** : La méthode `fermerDialog()` ne fonctionne pas

**Solution** : Ajoute cette ligne dans `ajouterContrat()` :
***REMOVED***java
Platform.runLater(() -> {
    Stage stage = (Stage) addButton.getScene().getWindow();
    stage.close();
});
***REMOVED***

---

## 🎓 Concepts JavaFX utilisés

1. **BorderPane** - Layout principal
2. **TableView** - Affichage des données
3. **Dialog/Stage** - Fenêtres modales
4. **FXML** - Structure de l'interface
5. **CSS** - Style des éléments
6. **Binding** - Liaisons de données
7. **Events** - Gestion des clics
8. **ObservableList** - Listes dynamiques

---

## 📝 Résumé des Commandes

***REMOVED***bash
# Compiler
mvn clean compile

# Lancer l'app
mvn javafx:run -Djavafx.mainClass=uniearn.example.ContratJavaFXApp

# Ou depuis IntelliJ
Run → ContratJavaFXApp
***REMOVED***

---

## 🎯 Prochaines étapes (Optionnel)

- [ ] Implémenter l'édition de contrats
- [ ] Ajouter l'export en PDF/Excel
- [ ] Ajouter la recherche/filtrage
- [ ] Ajouter les graphiques de statistiques
- [ ] Ajouter la signature numérique
- [ ] Ajouter les notifications

---

**Besoin d'aide ?** 
Consulte les fichiers sources :
- `ContratJavaFXApp.java` - Logique de l'app
- `ContratDialogController2.java` - Logique du formulaire
- `contrat_add_dialog.fxml` - Structure FXML
- `styles.css` - Styles personnalisés

**Bon développement !** 🚀

