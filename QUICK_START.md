# ⚡ COMMANDES RAPIDES

## 🚀 DÉMARRAGE RAPIDE

### 1. Préparer la Base de Données
***REMOVED***bash
# Copier et exécuter le contenu de:
schema_contract.sql
# dans phpMyAdmin ou MySQL Workbench
***REMOVED***

### 2. Compiler le Projet
***REMOVED***bash
# Windows (PowerShell)
cd C:\Users\MSI\Desktop\uniearn
javac -d target/classes -cp "src/main/java:target/lib/*" src/main/java/uniearn/crud/ContratCRUD.java

# Ou avec Maven (si disponible)
mvn clean compile
***REMOVED***

### 3. Tester le CRUD
***REMOVED***bash
# Option 1: Tests automatisés
cd C:\Users\MSI\Desktop\uniearn
java -cp "target/classes:target/lib/*" uniearn.crud.ContratCRUDTest

# Option 2: Application interactive
java -cp "target/classes:target/lib/*" uniearn.example.ContratApp
***REMOVED***

---

## 💻 UTILISATION DANS LE CODE

### Importer
***REMOVED***java
import uniearn.crud.ContratCRUD;
import uniearn.model.entities.Contrat;
import java.sql.Timestamp;
import java.time.LocalDateTime;
***REMOVED***

### Créer une Instance
***REMOVED***java
ContratCRUD crud = new ContratCRUD();
***REMOVED***

### Créer un Contrat
***REMOVED***java
Contrat c = new Contrat();
c.setAmount(50000);
c.setClientID(1);
c.setProjectID(1);
c.setStatus(0);
c.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
c.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));

crud.create(c);
***REMOVED***

### Récupérer
***REMOVED***java
// Tous
List<Contrat> tous = crud.readAll();

// Par ID
Contrat c = crud.readById(1);

// Par Client
List<Contrat> mesContrats = crud.readByClient(1);

// Par Projet
List<Contrat> contratsDuProjet = crud.readByProject(1);
***REMOVED***

### Modifier
***REMOVED***java
Contrat c = crud.readById(1);
c.setAmount(75000);
crud.update(c);
***REMOVED***

### Signer
***REMOVED***java
// Client
crud.signByClient(1);

// Freelancer
crud.signByFreelancer(1);
***REMOVED***

### Supprimer
***REMOVED***java
crud.delete(1);
***REMOVED***

### Statistiques
***REMOVED***java
int total = crud.count();
int[] stats = crud.getStatsByStatus();
// stats[0] = Brouillons
// stats[1] = Signés Client
// stats[2] = Signés Freelancer
// stats[3] = Complétés
***REMOVED***

### Afficher
***REMOVED***java
Contrat c = crud.readById(1);
crud.displayContrat(c);
***REMOVED***

---

## 📚 LECTURE RAPIDE

### Fichiers à Lire (dans l'ordre)

1. **START_HERE.md** (5 min)
   - Aperçu rapide
   
2. **README_CONTRATS.md** (10 min)
   - Guide principal
   
3. **CRUD_GUIDE.md** (15 min)
   - Exemples détaillés
   
4. **ContratCRUD.java** (20 min)
   - Regarder le code source

---

## 🎯 CAS D'USAGE COURANTS

### Cas 1: Afficher tous les contrats dans une TableView
***REMOVED***java
@FXML private TableView<Contrat> table;

public void charger() {
    ContratCRUD crud = new ContratCRUD();
    List<Contrat> contrats = crud.readAll();
    table.setItems(FXCollections.observableArrayList(contrats));
}
***REMOVED***

### Cas 2: Créer un contrat depuis un formulaire
***REMOVED***java
@FXML private TextField tfMontant;
@FXML private TextField tfClientID;
@FXML private TextField tfProjectID;

public void creer() {
    ContratCRUD crud = new ContratCRUD();
    Contrat c = new Contrat();
    c.setAmount(Double.parseDouble(tfMontant.getText()));
    c.setClientID(Integer.parseInt(tfClientID.getText()));
    c.setProjectID(Integer.parseInt(tfProjectID.getText()));
    c.setStatus(0);
    c.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
    c.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
    
    if (crud.create(c)) {
        afficherAlerte("Succès", "Contrat créé");
    }
}
***REMOVED***

### Cas 3: Filtrer les contrats d'un client
***REMOVED***java
public void afficherContratsDuClient(int clientID) {
    ContratCRUD crud = new ContratCRUD();
    List<Contrat> contrats = crud.readByClient(clientID);
    // Afficher dans la table
}
***REMOVED***

### Cas 4: Supprimer un contrat avec confirmation
***REMOVED***java
public void supprimer(int contractID) {
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirmation");
    confirm.setContentText("Êtes-vous sûr?");
    
    if (confirm.showAndWait().isPresent()) {
        ContratCRUD crud = new ContratCRUD();
        if (crud.delete(contractID)) {
            afficherAlerte("Succès", "Supprimé");
        }
    }
}
***REMOVED***

---

## 🔍 DÉPANNAGE RAPIDE

### Erreur: "Cannot find symbol"
***REMOVED***
Solution: Vérifier que les fichiers sont compilés
javac -d target/classes -cp "..." *.java
***REMOVED***

### Erreur: "Connection refused"
***REMOVED***
Solution: Vérifier que:
1. MySQL est démarré
2. La base de données existe
3. MyConnection est correctement configurée
***REMOVED***

### Erreur: "Table doesn't exist"
***REMOVED***
Solution: Exécuter schema_contract.sql
pour créer la table contract
***REMOVED***

### Erreur: "No suitable driver"
***REMOVED***
Solution: Vérifier que mysql-connector-java
est dans le classpath (target/lib/)
***REMOVED***

---

## 📊 RÉSUMÉ DES MÉTHODES

| Méthode | Description | Retour |
|---------|-------------|--------|
| `create(Contrat)` | Créer | boolean |
| `readAll()` | Lire tous | List |
| `readById(int)` | Lire par ID | Contrat |
| `readByClient(int)` | Lire par client | List |
| `readByProject(int)` | Lire par projet | List |
| `update(Contrat)` | Modifier | boolean |
| `signByClient(int)` | Signer client | boolean |
| `signByFreelancer(int)` | Signer freelancer | boolean |
| `delete(int)` | Supprimer | boolean |
| `exists(int)` | Existe? | boolean |
| `count()` | Compter | int |
| `getStatsByStatus()` | Stats | int[] |
| `displayContrat(Contrat)` | Afficher | void |

---

## 🎓 PATTERN D'UTILISATION

***REMOVED***java
// Pattern général
1. Créer une instance du CRUD
   ContratCRUD crud = new ContratCRUD();

2. Appeler la méthode voulue
   crud.create(contrat);
   crud.readAll();
   crud.update(contrat);
   etc.

3. Vérifier le résultat (si boolean)
   if (crud.create(contrat)) {
       // Succès
   } else {
       // Erreur
   }

4. Rafraîchir l'interface
   table.setItems(...);
   afficherAlerte(...);
***REMOVED***

---

## ⚙️ CONFIGURATION REQUISE

- Java 17+
- MySQL 5.7+
- MySQL Connector Java 8.0+
- JavaFX 17+ (pour interface)

---

## 📱 EXEMPLE COMPLET MINIMALISTE

***REMOVED***java
import uniearn.crud.ContratCRUD;
import uniearn.model.entities.Contrat;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class App {
    public static void main(String[] args) {
        ContratCRUD crud = new ContratCRUD();
        
        // Créer
        Contrat c = new Contrat();
        c.setAmount(50000);
        c.setClientID(1);
        c.setProjectID(1);
        c.setStatus(0);
        c.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
        c.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
        crud.create(c);
        
        // Lire
        crud.readAll().forEach(crud::displayContrat);
        
        // Modifier
        Contrat r = crud.readById(1);
        if (r != null) {
            r.setAmount(75000);
            crud.update(r);
        }
        
        // Signer
        crud.signByClient(1);
        
        // Supprimer
        // crud.delete(1);
    }
}
***REMOVED***

---

## 🚀 EN 30 SECONDES

1. Lire **START_HERE.md**
2. Exécuter **schema_contract.sql**
3. Importer **ContratCRUD**
4. Utiliser: `crud.create()`, `crud.readAll()`, etc.
5. C'est prêt! ✅

---

## 📞 AIDE RAPIDE

- **Créer un contrat**: `crud.create(contrat)`
- **Récupérer tous**: `crud.readAll()`
- **Récupérer un**: `crud.readById(id)`
- **Modifier**: `crud.update(contrat)`
- **Signer**: `crud.signByClient(id)`
- **Supprimer**: `crud.delete(id)`

**C'est aussi simple que ça!** 🎉

---

**Vous êtes prêt à partir! Bon codage! 🚀**

