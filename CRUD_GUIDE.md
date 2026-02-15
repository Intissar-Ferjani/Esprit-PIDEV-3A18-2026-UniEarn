# GUIDE D'UTILISATION - CRUD CONTRATS

## 📋 Fichiers Créés

### 1. **Modèle de Données**
- `src/main/java/uniearn/model/entities/Contrat.java`
  - Classe entité représentant un contrat
  - Contient tous les attributs et getters/setters

### 2. **Interface**
- `src/main/java/uniearn/interfaces/IContrat.java`
  - Définit le contrat des méthodes CRUD

### 3. **Service**
- `src/main/java/uniearn/services/ContratService.java`
  - Gère les opérations de base de données
  - Implémente l'interface IContrat

### 4. **CRUD Simplifié**
- `src/main/java/uniearn/crud/ContratCRUD.java` ⭐ **FICHIER PRINCIPAL**
  - Classe wrapper simplifiée pour les opérations CRUD
  - Facile à utiliser et sans dépendances externes
  - À utiliser dans votre application

### 5. **Tests**
- `src/main/java/uniearn/services/ContratServiceTest.java`
- `src/main/java/uniearn/crud/ContratCRUDTest.java`

### 6. **Contrôleurs JavaFX** (Optionnel)
- `src/main/java/uniearn/controller/ContratController.java`
- `src/main/java/uniearn/controller/ContratDialogController.java`
- `src/main/resources/contracts/contracts.fxml`
- `src/main/resources/contracts/contract_dialog.fxml`

---

## 🚀 UTILISATION SIMPLE

### Exemple 1: Créer un contrat

```java
import uniearn.crud.ContratCRUD;
import uniearn.model.entities.Contrat;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// Créer une instance du CRUD
ContratCRUD crud = new ContratCRUD();

// Créer un nouveau contrat
Contrat contrat = new Contrat();
contrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
contrat.setStatus(0); // Brouillon
contrat.setAmount(50000.0); // 50 000 DA
contrat.setProjectID(1);
contrat.setClientID(1);
contrat.setPaymentID(1);

// Sauvegarder
if (crud.create(contrat)) {
    System.out.println("✅ Contrat créé");
} else {
    System.out.println("❌ Erreur");
}
```

### Exemple 2: Récupérer tous les contrats

```java
ContratCRUD crud = new ContratCRUD();

List<Contrat> tousLesContrats = crud.readAll();
for (Contrat c : tousLesContrats) {
    System.out.println("Contrat " + c.getIdContract() + ": " + c.getAmount() + " DA");
}
```

### Exemple 3: Récupérer un contrat par ID

```java
ContratCRUD crud = new ContratCRUD();

Contrat contrat = crud.readById(1);
if (contrat != null) {
    crud.displayContrat(contrat);
} else {
    System.out.println("Contrat non trouvé");
}
```

### Exemple 4: Modifier un contrat

```java
ContratCRUD crud = new ContratCRUD();

Contrat contrat = crud.readById(1);
if (contrat != null) {
    contrat.setAmount(75000.0);
    if (crud.update(contrat)) {
        System.out.println("✅ Contrat mis à jour");
    }
}
```

### Exemple 5: Signer un contrat

```java
ContratCRUD crud = new ContratCRUD();

// Signature par le client
if (crud.signByClient(1)) {
    System.out.println("✅ Signé par le client");
}

// Signature par le freelancer
if (crud.signByFreelancer(1)) {
    System.out.println("✅ Signé par le freelancer (Contrat complet)");
}
```

### Exemple 6: Supprimer un contrat

```java
ContratCRUD crud = new ContratCRUD();

if (crud.delete(1)) {
    System.out.println("✅ Contrat supprimé");
}
```

### Exemple 7: Filtrer par client

```java
ContratCRUD crud = new ContratCRUD();

List<Contrat> mesCPntrats = crud.readByClient(1);
System.out.println("Nombre de contrats: " + mesContrats.size());
```

### Exemple 8: Filtrer par projet

```java
ContratCRUD crud = new ContratCRUD();

List<Contrat> contratsDuProjet = crud.readByProject(1);
System.out.println("Contrats du projet 1: " + contratsDuProjet.size());
```

---

## 📊 STATUTS DES CONTRATS

| Code | Statut | Description |
|------|--------|-------------|
| 0 | Brouillon | Contrat créé, non signé |
| 1 | Signé Client | Signé par le client, attend freelancer |
| 2 | Signé Freelancer | Signé par le freelancer, attend client |
| 3 | Complété | Signé par les deux parties ✅ |

---

## 🔧 MÉTHODES DISPONIBLES

### Create
```java
boolean create(Contrat contrat)
```

### Read
```java
List<Contrat> readAll()
Contrat readById(int id)
List<Contrat> readByClient(int clientID)
List<Contrat> readByProject(int projectID)
```

### Update
```java
boolean update(Contrat contrat)
boolean signByClient(int contractID)
boolean signByFreelancer(int contractID)
```

### Delete
```java
boolean delete(int id)
```

### Utilitaires
```java
boolean exists(int id)
int count()
int[] getStatsByStatus()
void displayContrat(Contrat contrat)
```

---

## 🧪 EXÉCUTER LES TESTS

### Test CRUD Simple
```bash
java -cp "./target/classes" uniearn.crud.ContratCRUDTest
```

### Test Service
```bash
java -cp "./target/classes" uniearn.services.ContratServiceTest
```

---

## 📦 SQL - Créer la table

Exécutez ce script SQL sur votre base de données:

```sql
CREATE TABLE IF NOT EXISTS contract (
    idContract INT PRIMARY KEY AUTO_INCREMENT,
    startDate TIMESTAMP NOT NULL,
    endDate TIMESTAMP NOT NULL,
    status TINYINT DEFAULT 0,
    amount DOUBLE NOT NULL,
    projectID INT NOT NULL,
    clientID INT NOT NULL,
    paymentID INT,
    CONSTRAINT fk_contract_project FOREIGN KEY (projectID) 
        REFERENCES project(idProject) ON DELETE CASCADE,
    CONSTRAINT fk_contract_client FOREIGN KEY (clientID) 
        REFERENCES client(idClient) ON DELETE CASCADE,
    CONSTRAINT fk_contract_payment FOREIGN KEY (paymentID) 
        REFERENCES payment(idPayment) ON DELETE SET NULL,
    INDEX idx_clientID (clientID),
    INDEX idx_projectID (projectID),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## ⚠️ NOTES IMPORTANTES

1. **Pas de Spring Boot** - Utilise uniquement Java standard et JDBC
2. **Dépendance**: MyConnection pour la connexion à la base de données
3. **Gestion d'erreurs** - Les erreurs sont loggées dans la console
4. **Thread-safe** - Pour une utilisation multi-thread, à améliorer si nécessaire

---

## 🎯 INTÉGRATION DANS VOTRE APP

Pour utiliser dans vos contrôleurs JavaFX ou autres classes:

```java
// À l'importation
import uniearn.crud.ContratCRUD;

// Dans votre classe
public class MaClasse {
    private ContratCRUD crud;
    
    public MaClasse() {
        this.crud = new ContratCRUD();
    }
    
    // Utiliser crud.create(), crud.readAll(), etc.
}
```

---

Créé sans dépendances Spring Boot - Pure JavaFX + JDBC ✅

