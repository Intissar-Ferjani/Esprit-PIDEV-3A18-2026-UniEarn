# 🎯 GUIDE COMPLET : CRUD DES CONTRATS - UniEarn

## 📋 Table des matières
1. [Structure des Données](#structure-des-données)
2. [Comment Utiliser](#comment-utiliser)
3. [Opérations CRUD](#opérations-crud)
4. [Exemple Complet](#exemple-complet)
5. [Statuts des Contrats](#statuts-des-contrats)
6. [Troubleshooting](#troubleshooting)

---

## 🗄️ Structure des Données

### Relations entre les tables
```
user (idUser, name, email, password, role)
  ↓
client (idClient, userID, amount, rating)
  ↓
contract (idContract, clientID, projectID, paymentID, ...)
  ↓
project (idProject, clientID, ...)
payment (idPayment, userID, ...)
```

### Champs du Contrat
```
idContract      : INT (PK, AUTO_INCREMENT)
startDate       : TIMESTAMP
endDate         : TIMESTAMP
status          : TINYINT (0-3)
amount          : DOUBLE (montant du contrat)
projectID       : INT (FK vers project)
clientID        : INT (FK vers client)
paymentID       : INT (FK vers payment, peut être NULL)
```

---

## 🚀 Comment Utiliser

### Option 1 : Application Interactive (RECOMMANDÉE)
```bash
# Compile
mvn clean compile

# Lance l'app interactive
java -cp target/classes uniearn.example.ContratTestApp
```

Cela ouvrira un menu interactif où tu peux :
- ✓ Créer des contrats
- ✓ Voir tous les contrats
- ✓ Rechercher par ID/Client/Projet
- ✓ Mettre à jour
- ✓ Signer par client/freelancer
- ✓ Supprimer
- ✓ Voir les statistiques

### Option 2 : Classe ContratCRUD (Programmation)
```java
ContratCRUD crud = new ContratCRUD();

// Créer
Contrat contrat = new Contrat();
contrat.setClientID(1);
contrat.setProjectID(1);
contrat.setAmount(50000.0);
contrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
contrat.setStatus(0);

crud.create(contrat);

// Lire
Contrat c = crud.readById(1);
List<Contrat> tous = crud.readAll();

// Mettre à jour
c.setAmount(75000.0);
crud.update(c);

// Supprimer
crud.delete(1);
```

### Option 3 : ContratService (Plus bas niveau)
```java
ContratService service = new ContratService();

// Même API que CRUD mais avec des noms différents
service.createContrat(contrat);
service.getContratById(1);
service.updateContrat(contrat);
service.deleteContrat(1);
```

---

## 📊 Opérations CRUD

### CREATE (Créer)
```java
Contrat contrat = new Contrat();
contrat.setClientID(1);           // Obligatoire
contrat.setProjectID(1);          // Obligatoire
contrat.setAmount(50000.0);       // Obligatoire
contrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
contrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
contrat.setStatus(0);             // 0 = Brouillon
contrat.setPaymentID(1);          // Optionnel

if (crud.create(contrat)) {
    System.out.println("✓ Créé");
} else {
    System.out.println("✗ Erreur");
}
```

### READ (Lire)
```java
// Tous les contrats
List<Contrat> tous = crud.readAll();

// Par ID
Contrat c = crud.readById(1);

// Par client
List<Contrat> duClient = crud.readByClient(1);

// Par projet
List<Contrat> duProjet = crud.readByProject(1);

// Vérifier existence
if (crud.exists(1)) {
    System.out.println("Existe");
}

// Compter
int nombre = crud.count();
```

### UPDATE (Mettre à jour)
```java
Contrat c = crud.readById(1);
c.setAmount(75000.0);
c.setStatus(1); // Signer client

if (crud.update(c)) {
    System.out.println("✓ Mis à jour");
}
```

### DELETE (Supprimer)
```java
if (crud.delete(1)) {
    System.out.println("✓ Supprimé");
}
```

### SIGNER
```java
// Signer par le client (status 0 → 1)
crud.signByClient(1);

// Signer par freelancer (status 1 → 3 ou 0 → 2)
crud.signByFreelancer(1);
```

### STATISTIQUES
```java
// Répartition par statut
int[] stats = crud.getStatsByStatus();
// [brouillon, signéClient, signéFreelancer, complété]

// Compter par client
int count = crud.count(); // Total

// Afficher
crud.displayContrat(contrat); // Format beau
```

---

## 📝 Exemple Complet

```java
import uniearn.crud.ContratCRUD;
import uniearn.model.entities.Contrat;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ExempleContrat {
    public static void main(String[] args) {
        ContratCRUD crud = new ContratCRUD();

        // 1. CRÉER
        System.out.println("=== CRÉATION ===");
        Contrat nouveau = new Contrat();
        nouveau.setClientID(1);
        nouveau.setProjectID(1);
        nouveau.setAmount(50000.0);
        nouveau.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
        nouveau.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
        nouveau.setStatus(0);
        nouveau.setPaymentID(1);

        if (crud.create(nouveau)) {
            System.out.println("✓ Contrat créé");
        }

        // 2. LIRE
        System.out.println("\n=== LECTURE ===");
        Contrat c = crud.readById(1);
        if (c != null) {
            crud.displayContrat(c);
        }

        // 3. AFFICHER TOUS
        System.out.println("\n=== TOUS LES CONTRATS ===");
        for (Contrat contrat : crud.readAll()) {
            System.out.printf("ID: %d | Client: %d | Montant: %.2f DA%n",
                    contrat.getIdContract(),
                    contrat.getClientID(),
                    contrat.getAmount());
        }

        // 4. METTRE À JOUR
        System.out.println("\n=== MISE À JOUR ===");
        c.setAmount(75000.0);
        if (crud.update(c)) {
            System.out.println("✓ Montant augmenté à 75000 DA");
        }

        // 5. SIGNER
        System.out.println("\n=== SIGNATURE ===");
        if (crud.signByClient(1)) {
            System.out.println("✓ Signé par le client");
        }

        if (crud.signByFreelancer(1)) {
            System.out.println("✓ Signé par le freelancer → Complété !");
        }

        // 6. STATISTIQUES
        System.out.println("\n=== STATISTIQUES ===");
        int[] stats = crud.getStatsByStatus();
        System.out.println("Total: " + crud.count());
        System.out.println("Brouillon: " + stats[0]);
        System.out.println("Signé Client: " + stats[1]);
        System.out.println("Complété: " + stats[3]);

        // 7. SUPPRIMER
        System.out.println("\n=== SUPPRESSION ===");
        // crud.delete(1);
    }
}
```

---

## 🎨 Statuts des Contrats

| Code | Nom | Description |
|------|-----|-------------|
| 0 | **Brouillon** | Contrat créé, pas encore signé |
| 1 | **Signé Client** | Le client a signé, en attente du freelancer |
| 2 | **Signé Freelancer** | Le freelancer a signé avant le client (rare) |
| 3 | **Complété** | Les deux ont signé, contrat valide |

### Flux de Signature Typique
```
0 (Brouillon)
    ↓
1 (Signé Client) ← signByClient()
    ↓
3 (Complété) ← signByFreelancer()
```

---

## 🛠️ Troubleshooting

### ❌ "Cannot add or update a child row: foreign key constraint fails"
**Cause** : Un ID référencé n'existe pas

**Solution** :
1. Vérifier que le client existe : `SELECT * FROM client WHERE idClient = 1;`
2. Vérifier que le projet existe : `SELECT * FROM project WHERE idProject = 1;`
3. Si nécessaire, exécuter `insert_test_data.sql`

### ❌ "Contrat nul"
**Cause** : L'ID du contrat n'existe pas

**Solution** :
```java
if (contrat != null) {
    // Utiliser
} else {
    System.out.println("Contrat non trouvé");
}
```

### ❌ Connexion impossible
**Cause** : MyConnection ne peut pas se connecter

**Solution** :
1. Vérifier que MySQL est lancé
2. Vérifier `MyConnection.java` : URL, login, password

---

## 📚 Fichiers Importants

| Fichier | Description |
|---------|-------------|
| `ContratService.java` | Service métier (bas niveau) |
| `ContratCRUD.java` | Wrapper CRUD (recommandé) |
| `ContratTestApp.java` | **Application interactive** ← À utiliser |
| `Contrat.java` | Entité Java |
| `insert_test_data.sql` | Données de test |

---

## 🎯 Résumé Rapide

```java
// Tout ce dont tu as besoin
ContratCRUD crud = new ContratCRUD();

// Créer
crud.create(contrat);

// Lire
crud.readById(1);
crud.readAll();
crud.readByClient(1);

// Mettre à jour
crud.update(contrat);

// Signer
crud.signByClient(1);
crud.signByFreelancer(1);

// Supprimer
crud.delete(1);
```

**Besoin d'aide ?** Lance `ContratTestApp` et explore le menu ! 🚀

