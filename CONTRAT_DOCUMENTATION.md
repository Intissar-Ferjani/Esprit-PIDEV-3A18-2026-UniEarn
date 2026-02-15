# GESTION DES CONTRATS - Documentation

## Vue d'ensemble
Ce module implémente une gestion complète des contrats (CRUD) pour l'application UniEarn. Les contrats sont des templates qui sont remplis et signés par les clients et les freelancers.

## Statuts des Contrats
- **0 - Brouillon**: Contrat en création, non signé
- **1 - Signé Client**: Contrat signé par le client, en attente de signature freelancer
- **2 - Signé Freelancer**: Contrat signé par le freelancer, en attente de signature client
- **3 - Complété**: Contrat signé par les deux parties

## Architecture

### Modèle de Données
**Fichier**: `uniearn/model/entities/Contrat.java`
```
Contrat
├── idContract (int) - Clé primaire
├── startDate (Timestamp) - Date de début
├── endDate (Timestamp) - Date de fin
├── status (int) - Statut du contrat (0-3)
├── amount (double) - Montant en DA
├── projectID (int) - Référence au projet
├── clientID (int) - Référence au client
└── paymentID (int) - Référence au paiement
```

### Service
**Fichier**: `uniearn/services/ContratService.java`
- Gère toutes les opérations CRUD sur la base de données
- Implémente l'interface `IContrat`
- Méthodes disponibles:
  - `createContrat()` - Créer un contrat
  - `getContratById()` - Récupérer par ID
  - `getAllContrats()` - Récupérer tous les contrats
  - `getContratsByClient()` - Filtrer par client
  - `getContratsByProject()` - Filtrer par projet
  - `getContratsByStatus()` - Filtrer par statut
  - `updateContrat()` - Mettre à jour
  - `deleteContrat()` - Supprimer
  - `signByClient()` - Signer par le client
  - `signByFreelancer()` - Signer par le freelancer

### Interface
**Fichier**: `uniearn/interfaces/IContrat.java`
- Définit le contrat (pun intended) pour le service
- Assure la cohérence des signatures de méthodes

### Contrôleurs

#### ContratController
**Fichier**: `uniearn/controller/ContratController.java`
- Contrôleur principal pour l'interface JavaFX
- Gère la TableView et les interactions utilisateur
- Responsable du chargement et de l'affichage des données

#### ContratDialogController
**Fichier**: `uniearn/controller/ContratDialogController.java`
- Contrôleur de la boîte de dialogue d'édition
- Gère la création et la modification des contrats
- Validation des champs saisis

#### ContratRestController
**Fichier**: `uniearn/controller/ContratRestController.java`
- Contrôleur pour les opérations REST (API)
- Peut être utilisé avec un serveur web

### Interfaces Utilisateur

#### Liste des Contrats
**Fichier**: `src/main/resources/contracts/contracts.fxml`
- TableView affichant tous les contrats
- Colonnes: ID, Date Début, Date Fin, Montant, Statut, IDs de référence
- Boutons: Ajouter, Modifier, Supprimer, Signer (Client), Signer (Freelancer), Actualiser

#### Dialogue d'Édition
**Fichier**: `src/main/resources/contracts/contract_dialog.fxml`
- Formulaire pour la création et la modification de contrats
- Champs: Montant, Date Début, Date Fin, Project ID, Client ID, Payment ID
- Validation automatique des champs

## Utilisation

### Intégration dans MainApp
Pour ajouter la gestion des contrats à votre application principale:

```java
// Dans le contrôleur approprié (ex: DashboardController)
FXMLLoader loader = new FXMLLoader(getClass().getResource("/contracts/contracts.fxml"));
Parent contractsView = loader.load();
// Ajouter à votre BorderPane ou TabPane
```

### Utilisation du Service
```java
// Créer une instance du service
ContratService service = new ContratService();

// Récupérer tous les contrats
List<Contrat> contrats = service.getAllContrats();

// Créer un nouveau contrat
Contrat nouveauContrat = new Contrat();
nouveauContrat.setAmount(50000);
nouveauContrat.setStartDate(Timestamp.valueOf(LocalDateTime.now()));
nouveauContrat.setEndDate(Timestamp.valueOf(LocalDateTime.now().plusMonths(1)));
nouveauContrat.setProjectID(1);
nouveauContrat.setClientID(1);
nouveauContrat.setPaymentID(1);
service.createContrat(nouveauContrat);

// Signer par le client
service.signByClient(contratId);

// Signer par le freelancer
service.signByFreelancer(contratId);
```

## Tests
**Fichier**: `uniearn/services/ContratServiceTest.java`

Pour exécuter les tests:
```bash
javac -cp ".:./target/classes:./target/lib/*" uniearn/services/ContratServiceTest.java
java -cp ".:./target/classes:./target/lib/*" uniearn.services.ContratServiceTest
```

Les tests couvrent:
1. Création d'un contrat
2. Récupération de tous les contrats
3. Récupération par ID
4. Mise à jour d'un contrat
5. Signature par le client
6. Signature par le freelancer
7. Filtrage par client
8. Filtrage par projet
9. Filtrage par statut
10. Suppression d'un contrat

## Structure de Fichiers Complète

```
src/main/java/uniearn/
├── controller/
│   ├── ContratController.java          # Contrôleur principal JavaFX
│   ├── ContratDialogController.java    # Contrôleur du dialogue d'édition
│   └── ContratRestController.java      # Contrôleur REST API
├── model/entities/
│   └── Contrat.java                    # Entité du modèle de données
├── interfaces/
│   └── IContrat.java                   # Interface du service
├── services/
│   ├── ContratService.java             # Service métier CRUD
│   └── ContratServiceTest.java         # Tests unitaires
└── (autres fichiers existants)

src/main/resources/
└── contracts/
    ├── contracts.fxml                  # Vue principale
    └── contract_dialog.fxml            # Vue du dialogue
```

## Base de Données
La table `contract` doit avoir la structure suivante:
```sql
CREATE TABLE contract (
  idContract INT PRIMARY KEY AUTO_INCREMENT,
  startDate TIMESTAMP,
  endDate TIMESTAMP,
  status TINYINT DEFAULT 0,
  amount DOUBLE,
  projectID INT NOT NULL,
  clientID INT NOT NULL,
  paymentID INT,
  FOREIGN KEY (projectID) REFERENCES project(idProject),
  FOREIGN KEY (clientID) REFERENCES client(idClient),
  FOREIGN KEY (paymentID) REFERENCES payment(idPayment)
);
```

## Notes Importantes

1. **Gestion de la Signature**: 
   - Un contrat peut être signé par le client ou le freelancer indépendamment
   - Le statut passe à "Complété" (3) lorsque les deux ont signé

2. **Validation des Données**:
   - Tous les champs sont obligatoires
   - Les montants doivent être positifs
   - Les dates de début doivent être avant les dates de fin

3. **Sécurité**:
   - Assurez-vous d'ajouter une authentification
   - Vérifiez que l'utilisateur connecté peut accéder aux contrats appropriés

4. **Performance**:
   - Pour un grand nombre de contrats, envisagez l'ajout de pagination
   - Utilisez des index sur projectID et clientID

## Auteur
Implémentation pour UniEarn - Plateforme de Freelancing

