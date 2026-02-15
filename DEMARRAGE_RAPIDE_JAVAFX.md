# 🚀 DÉMARRAGE RAPIDE : INTERFACE JAVAFX

## 3 étapes pour lancer l'application

### ✅ Étape 1 : Vérifier les prérequis
***REMOVED***bash
# Vérifie que Java est installé (version 17+)
java -version

# Vérifie que Maven est installé
mvn -version

# Assure-toi que MySQL est lancé et la BD est accessible
***REMOVED***

### ✅ Étape 2 : Compiler le projet
***REMOVED***bash
cd C:\Users\MSI\Desktop\uniearn

# Nettoie et compile
mvn clean compile
***REMOVED***

**Résultat attendu** : 
***REMOVED***
[INFO] BUILD SUCCESS
***REMOVED***

### ✅ Étape 3 : Lancer l'application

#### Option A : Via Maven (RECOMMANDÉ)
***REMOVED***bash
mvn javafx:run -Djavafx.mainClass=uniearn.example.ContratJavaFXApp
***REMOVED***

#### Option B : Via IntelliJ IDEA
1. Ouvre `ContratJavaFXApp.java`
2. Clique le bouton ▶️ **Run** en haut à droite
3. L'app s'ouvre dans une nouvelle fenêtre

#### Option C : Depuis le terminal (manuel)
***REMOVED***bash
# Crée un package exécutable
mvn clean package

# Lance l'application
java -module-path "C:\path\to\javafx-sdk\lib" \
     --add-modules javafx.controls,javafx.fxml \
     -cp target/uniearn_java-1.0-SNAPSHOT.jar \
     uniearn.example.ContratJavaFXApp
***REMOVED***

---

## 🎯 L'application est lancée !

Tu vois maintenant une fenêtre moderne avec :

***REMOVED***
┌─────────────────────────────────────────────────┐
│  [BLEU] UniEarn - Gestion des Contrats         │
│  Gérez vos contrats clients et freelancer       │
│  [➕ Ajouter] [🔄 Rafraîchir] [📊 Exporter]    │
├─────────────────────────────────────────────────┤
│                                                 │
│  Tableau affichant tous les contrats            │
│  ID │ Client │ Projet │ Montant │ Statut │ Act.│
│  1  │   5    │   2    │ 400 DA  │ Vert   │ ✏️🗑│
│  2  │   1    │   1    │ 50000DA │ Orange │ ✏️🗑│
│                                                 │
├─────────────────────────────────────────────────┤
│ ✓ Tableau rafraîchi (2 contrats)               │
└─────────────────────────────────────────────────┘
***REMOVED***

---

## 📋 Utilisation de base

### 1. Ajouter un contrat
***REMOVED***
1. Clique [➕ Ajouter un Contrat]
   → Une fenêtre de formulaire s'ouvre
2. Remplis les champs (validation en temps réel)
   - ID Client: 5
   - ID Projet: 2
   - Montant: 400
   - Dates: (auto-remplies avec aujourd'hui)
   - Statut: Brouillon
3. Clique [✓ Ajouter]
   → Message : "✓ Contrat ajouté avec succès !"
4. La fenêtre se ferme automatiquement
5. Le tableau se rafraîchit avec le nouveau contrat
***REMOVED***

### 2. Voir tous les contrats
***REMOVED***
Le tableau principal affiche :
- Tous les contrats en BD
- Coloré selon le statut :
  🟠 Brouillon (Orange)
  🔵 Signé Client (Bleu)
  🟣 Signé Freelancer (Violet)
  🟢 Complété (Vert)
***REMOVED***

### 3. Supprimer un contrat
***REMOVED***
1. Trouve le contrat dans le tableau
2. Clique le bouton [🗑️] à droite
3. Confirme : "Êtes-vous sûr ?"
4. Clique [OK]
   → Le contrat est supprimé ✓
5. Le tableau se rafraîchit
***REMOVED***

### 4. Rafraîchir les données
***REMOVED***
1. Clique [🔄 Rafraîchir]
   → Le tableau recharge les données depuis la BD
   → Message : "✓ Tableau rafraîchir (X contrats)"
***REMOVED***

### 5. Voir les statistiques
***REMOVED***
Regarde en bas de la fenêtre (Footer blanc) :
"Total: 3 | Brouillon: 1 | Signé Client: 1 | Complété: 1"
***REMOVED***

---

## 🎨 Design personnalisé

Les couleurs utilisées :
- **Bleu** `#1E56DB` → Headers, boutons, focus
- **Vert** `#4CAF50` → Bouton Ajouter, succès
- **Gris** `#F5F5F5` → Arrière-plan principal
- **Blanc** `#FFFFFF` → Tableaux, cartes
- **Orange** `#FF9800` → Bouton Exporter

---

## ⚠️ Si tu as une erreur

### ❌ "Impossible de charger le formulaire"
***REMOVED***bash
# Recompile avec les ressources
mvn clean compile
***REMOVED***

### ❌ "Cannot connect to database"
***REMOVED***bash
# Vérifie :
1. MySQL est lancé (services → MySQL)
2. Credentials sont corrects dans MyConnection.java
3. BD uniearn_db existe
***REMOVED***

### ❌ "JavaFX not found"
***REMOVED***bash
# Assure-toi que le pom.xml a les dépendances JavaFX
# Sinon, relance Maven
mvn clean install
***REMOVED***

### ❌ "Port 3306 already in use"
***REMOVED***bash
# Autre instance MySQL en cours d'exécution
# Redémarre le service MySQL
***REMOVED***

---

## 📁 Fichiers importants

***REMOVED***
uniearn/
├── src/main/java/uniearn/example/
│   └── ContratJavaFXApp.java ⭐ (Fichier principal)
│
├── src/main/java/uniearn/controller/
│   └── ContratDialogController2.java (Formulaire)
│
├── src/main/resources/
│   ├── contracts/
│   │   └── contrat_add_dialog.fxml (Interface)
│   └── styles/
│       └── styles.css (Couleurs)
│
└── Documentation/
    ├── INTERFACE_JAVAFX_COMPLETE.md (Résumé)
    ├── GUIDE_JAVAFX_CONTRATS.md (Guide détaillé)
    └── DEMARRAGE_RAPIDE.md (Ce fichier)
***REMOVED***

---

## 🔄 Cycle de travail typique

***REMOVED***
1. Lance l'app    → ContratJavaFXApp ▶️
   ↓
2. Vois contrats  → Tableau s'affiche
   ↓
3. Ajoute un      → Dialog modal s'ouvre
   ↓
4. Remplis form   → Validation en temps réel
   ↓
5. Clique Ajouter → Message de succès
   ↓
6. Tableau se     → Rafraîchissement auto
   rafraîchit
   ↓
7. Nouveau        → Visible dans le tableau
   contrat
   ↓
8. Supprimer si   → Confirmation + suppression
   besoin
***REMOVED***

---

## 📚 Pour approfondir

Consulte ces fichiers pour plus de détails :

1. **INTERFACE_JAVAFX_COMPLETE.md**
   - Architecture complète
   - Tous les composants
   - Concepts JavaFX

2. **GUIDE_JAVAFX_CONTRATS.md**
   - Utilisation détaillée
   - Troubleshooting avancé
   - Concepts JavaFX

3. **CRUD_CONTRATS_GUIDE_COMPLET.md**
   - Utilisation du CRUD
   - Opérations sur la BD
   - API complète

---

## ✅ Checklist avant de commencer

- [x] Java 17+ installé
- [x] Maven installé
- [x] MySQL lancé
- [x] Projet compilé
- [x] Fichiers FXML et CSS existent
- [x] Données de test en BD (optionnel)

**Tout est bon ?** → Clique sur ▶️ et lance ! 🚀

---

## 🎯 En cas de blocage

***REMOVED***
Erreur → Regarde dans cet ordre :
1. Console → Lis le message d'erreur
2. INTERFACE_JAVAFX_COMPLETE.md → Troubleshooting
3. GUIDE_JAVAFX_CONTRATS.md → Troubleshooting avancé
4. Code source → Uncommente les println() pour déboguer
5. Stack trace → Recherche l'erreur racine
***REMOVED***

---

**Besoin d'aide ?** Demande ! 😊

**Prêt à commencer ?** Fais : `mvn javafx:run -Djavafx.mainClass=uniearn.example.ContratJavaFXApp`

Puis explore l'interface ! 🎉

