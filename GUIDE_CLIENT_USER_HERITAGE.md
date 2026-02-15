# 📚 Guide : Héritage Client extends User

## 🏗️ Structure de ta Base de Données

***REMOVED***
user (table parent)
├── idUser (INT PRIMARY KEY)
├── name (VARCHAR)
├── email (VARCHAR)
├── password (VARCHAR)
└── role (ENUM)
     │
     └──> client (table enfant)
          ├── idClient (INT PRIMARY KEY)
          ├── userID (INT FOREIGN KEY → user.idUser)
          ├── amount (DOUBLE)
          └── rating (DOUBLE)
***REMOVED***

## 📝 Code Java

***REMOVED***java
public class User {
    private int idUser;
    private String name;
    private String email;
    private String password;
    private UserRole role;
    // ... getters/setters
}

public class Client extends User {
    private double amount;      // balance dans la BD
    private double rating;
    // ... getters/setters
}
***REMOVED***

## 🗄️ Structure MySQL

***REMOVED***sql
CREATE TABLE user (
    idUser INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100),
    email VARCHAR(100),
    password VARCHAR(100),
    role ENUM('CLIENT', 'FREELANCER', 'ADMIN')
);

CREATE TABLE client (
    idClient INT PRIMARY KEY AUTO_INCREMENT,
    userID INT NOT NULL UNIQUE,
    amount DOUBLE,
    rating DOUBLE,
    FOREIGN KEY (userID) REFERENCES user(idUser) ON DELETE CASCADE
);
***REMOVED***

## 🎯 Ordre d'Insertion IMPORTANT

### ❌ FAUX (causera une erreur FK) :
***REMOVED***sql
-- Ceci va échouer car userID=1 n'existe pas !
INSERT INTO client (userID, amount, rating) VALUES (1, 0.0, 0.0);
***REMOVED***

### ✅ CORRECT :
***REMOVED***sql
-- Étape 1 : Créer l'utilisateur parent
INSERT INTO user (name, email, password, role) 
VALUES ('Jean Test', 'jean@example.com', 'pwd', 'CLIENT');
-- idUser = 1 (auto-généré)

-- Étape 2 : Créer le client enfant lié à cet utilisateur
INSERT INTO client (userID, amount, rating) 
VALUES (1, 0.0, 0.0);
-- idClient = 1, userID = 1 (référence le user créé)
***REMOVED***

## 🔗 Relations en Chaîne

Pour un contrat, tu dois avoir :

***REMOVED***sql
-- 1. User (client)
INSERT INTO user (name, email, password, role) 
VALUES ('Jean Test', 'jean@example.com', 'pwd', 'CLIENT');
-- idUser = 1

-- 2. Client (lié au user)
INSERT INTO client (userID, amount, rating) 
VALUES (1, 0.0, 0.0);
-- idClient = 1

-- 3. User (freelancer optionnel)
INSERT INTO user (name, email, password, role) 
VALUES ('Jean Freelancer', 'freelancer@example.com', 'pwd', 'FREELANCER');
-- idUser = 2

-- 4. Project (lié au client)
INSERT INTO project (projectName, description, clientID, status) 
VALUES ('Mon Projet', 'Description', 1, 'active');
-- idProject = 1

-- 5. Payment (optionnel)
INSERT INTO payment (amount, datePayment, method, userID, status) 
VALUES (50000.0, NOW(), 'Virement', 1, 'pending');
-- idPayment = 1

-- 6. Contract (lié à tout le monde)
INSERT INTO contract (startDate, endDate, status, amount, projectID, clientID, paymentID) 
VALUES (NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH), 0, 50000.0, 1, 1, 1);
-- idContract = 1
***REMOVED***

## ⚠️ Pièges Courants

### Piège 1 : Oublier de créer l'utilisateur d'abord
***REMOVED***java
// ❌ MAUVAIS
new Client(name, email, pwd, role); // Sans user parent

// ✅ BON
User user = new User(name, email, pwd, role);
userService.addUser(user);
// Récupère idUser = 1
Client client = new Client();
client.setUserID(1); // Lié au user
***REMOVED***

### Piège 2 : IDs hardcodés sans vérification
***REMOVED***java
// ❌ MAUVAIS
contrat.setClientID(1); // Et si ce client n'existe pas ?

// ✅ BON
Client client = clientService.getClientById(1);
if (client != null) {
    contrat.setClientID(1);
}
***REMOVED***

### Piège 3 : Oublier les cascades
***REMOVED***sql
-- ✅ BON : suppression en cascade
FOREIGN KEY (userID) REFERENCES user(idUser) ON DELETE CASCADE
-- Si on supprime le user, le client est aussi supprimé
***REMOVED***

## 🧪 Tester l'Héritage

***REMOVED***java
// Test 1 : Créer un user
User user = new User("John", "john@test.com", "pwd", UserRole.CLIENT);
userService.addUser(user); // idUser = 1

// Test 2 : Créer un client lié
Client client = new Client();
client.setUserID(1);  // Lié au user créé
client.setAmount(100.0);
client.setRating(5.0);
clientService.addClient(client); // idClient = 1

// Test 3 : Le client hérite les propriétés du user
System.out.println(client.getName()); // "John" (hérité)
System.out.println(client.getAmount()); // 100.0 (spécifique)
***REMOVED***

## 📊 Vérifier les Relations

***REMOVED***sql
-- Voir tous les users
SELECT * FROM user;

-- Voir tous les clients et leurs users liés
SELECT c.idClient, c.userID, u.name, u.email, c.amount
FROM client c
JOIN user u ON c.userID = u.idUser;

-- Vérifier les contrats liés à un client
SELECT con.idContract, con.clientID, c.userID, u.name
FROM contract con
JOIN client c ON con.clientID = c.idClient
JOIN user u ON c.userID = u.idUser
WHERE con.clientID = 1;
***REMOVED***

## 🎯 Checklist avant les Tests

- [ ] ✓ J'ai créé un user en premier
- [ ] ✓ Le user a un idUser (vérifier avec SELECT)
- [ ] ✓ J'ai créé un client lié à ce user (userID = idUser)
- [ ] ✓ Le client a un idClient (vérifier avec SELECT)
- [ ] ✓ J'ai créé un project lié au client (clientID = idClient)
- [ ] ✓ J'ai créé un payment (optionnel)
- [ ] ✓ Les IDs dans le test correspondent aux vrais IDs en BD
- [ ] ✓ Je vais lancer le test Java

Besoin d'aide ? 😊

