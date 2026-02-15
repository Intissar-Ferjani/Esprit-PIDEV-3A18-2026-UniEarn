# 🔧 GUIDE D'INTÉGRATION: GESTION DES CONTRATS

## 📋 FICHIERS CRÉÉS

### Base de Données
- ✅ `migration_contract_upgrade.sql` - Script de migration BD

### Modèles Java (Entities)
- ✅ `Contrat.java` - Modèle enrichi
- ✅ `ContractTemplate.java` - Modèle template

### Services
- ✅ `ContractTemplateService.java` - CRUD templates
- ✅ `ContratService.java` - CRUD contrats (amélioré)
- ✅ `DataLoaderService.java` - Chargement données dynamiques
- ✅ `ContractPDFService.java` - Export PDF

### Interfaces FXML
- ✅ `contract_template_admin.fxml` - Dashboard admin
- ✅ `contract_template_dialog.fxml` - Dialog template
- ✅ `client_contracts.fxml` - Dashboard client
- ✅ `client_contract_dialog.fxml` - Dialog contrat client
- ✅ `contract_signature.fxml` - Interface signature

### Contrôleurs JavaFX
- ✅ `ContractTemplateController.java` - Contrôleur admin
- ✅ `ContractTemplateDialogController.java` - Dialog template
- ✅ `ClientContractController.java` - Dashboard client
- ✅ `ClientContractDialogController.java` - Dialog client
- ✅ `ContractSignatureController.java` - Signature

### Styles
- ✅ `contract_styles.css` - Feuille CSS personnalisée

---

## 🚀 ÉTAPES D'INTÉGRATION

### ÉTAPE 1: Préparation de la Base de Données

```bash
1. Ouvrir votre logiciel de gestion BD (PhpMyAdmin, MySQL Workbench, etc.)
2. Exécuter le script: migration_contract_upgrade.sql
3. Vérifier que:
   - Les colonnes sont ajoutées à "contract"
   - La table "contract_template" est créée
   - Les 3 templates par défaut sont insérés
```

**Vérification SQL:**
```sql
DESCRIBE contract;  -- Vérifier les colonnes
SELECT * FROM contract_template;  -- Vérifier les templates
```

---

### ÉTAPE 2: Mise à Jour du Projet Maven

**Ajouter au pom.xml (optionnel - pour export PDF):**

```xml
<!-- Pour export PDF avec Apache PDFBox (gratuit) -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.28</version>
</dependency>

<!-- OU pour iText (commercial mais plus puissant) -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itextpdf</artifactId>
    <version>5.5.13.3</version>
</dependency>
```

Puis exécuter: `mvn clean install`

---

### ÉTAPE 3: Compiler le Projet

```bash
# Depuis le répertoire du projet
mvn clean compile

# Ou depuis votre IDE (Right-click → Maven → Reload Project)
```

Vérifier qu'il n'y a pas d'erreurs de compilation.

---

### ÉTAPE 4: Intégrer les Interfaces au Menu Principal

**Dans votre application principale (ex: MainApp.java ou ApplicationController.java):**

```java
// Importer les contrôleurs
import uniearn.controller.ContractTemplateController;
import uniearn.controller.ClientContractController;

// Dans votre menu ou navigation:
MenuItem adminContractsMenu = new MenuItem("Gestion Templates (Admin)");
adminContractsMenu.setOnAction(e -> openContractTemplateManager());

MenuItem clientContractsMenu = new MenuItem("Mes Contrats (Client)");
clientContractsMenu.setOnAction(e -> openClientContractManager());

// Méthodes pour ouvrir les interfaces:
private void openContractTemplateManager() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contract_template_admin.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setTitle("Gestion des Templates de Contrats");
        stage.setScene(new Scene(root, 1000, 600));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

private void openClientContractManager() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/client_contracts.fxml"));
        Parent root = loader.load();
        ClientContractController controller = loader.getController();
        controller.setCurrentClientID(getCurrentUserClientID()); // Passer l'ID du client connecté
        
        Stage stage = new Stage();
        stage.setTitle("Mes Contrats");
        stage.setScene(new Scene(root, 1000, 600));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

---

### ÉTAPE 5: Configurer l'ID du Client Connecté

**IMPORTANT:** Modifier `ClientContractController.java`

```java
// Ligne à modifier (actuellement hardcodée à 1):
private int currentClientID = 1;

// Remplacer par:
private int currentClientID = getCurrentClientIdFromSession();

// Ajouter une méthode:
private int getCurrentClientIdFromSession() {
    // À adapter selon votre système de session/authentification
    // Exemple avec session statique:
    return SessionManager.getInstance().getCurrentClientID();
}
```

---

### ÉTAPE 6: Appliquer les Styles CSS (Optionnel)

**Dans vos FXML ou en code Java:**

```java
// En FXML:
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<BorderPane 
    xmlns="http://javafx.com/javafx"
    stylesheets="@../styles/contract_styles.css">
    <!-- ... -->
</BorderPane>

// OU en Java:
Scene scene = new Scene(root, 1000, 600);
scene.getStylesheets().add(getClass().getResource("/styles/contract_styles.css").toExternalForm());
stage.setScene(scene);
```

---

### ÉTAPE 7: Tester l'Application

**1. Test Admin - Création de Template:**
```
Ouvrir: Gestion Templates
Bouton: "➕ Nouveau Template"
Remplir:
  - Nom: "Mon Template Test"
  - Description: "Test"
  - Contenu: "Contrat [ClientName] - [Amount] DA"
Cliquer: "Enregistrer"
Résultat attendu: ✅ Succès
```

**2. Test Client - Création de Contrat:**
```
Ouvrir: Mes Contrats
Bouton: "➕ Nouveau Contrat"
Remplir:
  - Template: Sélectionner un template
  - Freelancer: Sélectionner ID
  - Projet: Sélectionner ID
  - Montant: 50000
  - Dates: Sélectionner les dates
Cliquer: "Créer Contrat"
Résultat attendu: ✅ Contrat créé
```

**3. Test Signature:**
```
Dans "Mes Contrats", cliquer: "✍️ Signer"
Dessiner une signature
Cliquer: "✓ Signer"
Résultat attendu: ✅ Signé (date affichée)
```

---

## ⚙️ CONFIGURATION AVANCÉE

### Modifier les Statuts de Contrats

**Dans `Contrat.java`:**
```java
public String getStatusString() {
    return switch (status) {
        case 0 -> "Brouillon";
        case 1 -> "Signé Client";
        case 2 -> "Signé Freelancer";
        case 3 -> "Complété";
        default -> "Inconnu";
    };
}
```

### Personnaliser les Couleurs

**Dans tous les FXML et CSS:**
- Remplacer `#1E56DB` (Bleu) par votre couleur
- Remplacer `#4CAF50` (Vert) par votre couleur
- Remplacer `#F5F5F5` (Gris) par votre couleur

### Ajouter des Champs Personnalisés

1. Ajouter une colonne à la table `contract` en BD
2. Ajouter un getter/setter dans `Contrat.java`
3. Mettre à jour les services CRUD
4. Ajouter un TextField dans les dialogs FXML

---

## 🐛 DÉPANNAGE

### Erreur: "Cannot find resource"
→ Vérifier que les chemins des FXML sont corrects

### Erreur: "Foreign Key Constraint"
→ Vérifier que les IDs (freelancer, projet, etc.) existent en BD

### Erreur: "NullPointerException"
→ Vérifier que les données sont chargées avant utilisation

### PDF ne s'exporte pas
→ Ajouter la dépendance PDFBox au pom.xml

---

## 📞 FONCTIONNALITÉS À AJOUTER

1. **Export HTML** - Alternative au PDF
2. **Impression directe** - Imprimer le contrat
3. **Archivage** - Historique des contrats
4. **Notifications** - Alerter les parties pour la signature
5. **Historique** - Tracer toutes les modifications
6. **Modèles avancés** - Conditions dynamiques dans les templates

---

## 📚 RESSOURCES UTILES

- [JavaFX Documentation](https://openjfx.io/)
- [Apache PDFBox](https://pdfbox.apache.org/)
- [iText Documentation](https://itextpdf.com/)
- [MySQL Workbench](https://www.mysql.com/products/workbench/)

---

**✅ Intégration Complète! Vous êtes prêt à utiliser la gestion des contrats! 🚀**

