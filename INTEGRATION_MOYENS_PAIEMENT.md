# Guide d'Intégration des Moyens de Paiement Bancaires 💳

## 📋 Vue d'ensemble

Ce guide explique comment intégrer la gestion des moyens de paiement (comptes bancaires) pour les clients.

## 🏗️ Fichiers Créés

### 1. **Entités**
- `BankAccount.java` - Modèle pour stocker les données bancaires

### 2. **Services**
- `BankAccountService.java` - Service de gestion des comptes bancaires

### 3. **Contrôleurs**
- `ClientPaymentMethodsController.java` - Gestion des moyens de paiement client

### 4. **Interfaces (FXML)**
- `client-payment-methods.fxml` - Section "Moyens de Paiement"

### 5. **Base de Données**
- `create_bank_account_table.sql` - Script de création de la table

## 🔧 Étapes d'Intégration

### Étape 1: Créer la Table en Base de Données

Exécute le script SQL:
```sql
mysql -u root -p uniearn_db < create_bank_account_table.sql
```

Ou copie-colle manuellement dans phpMyAdmin.

### Étape 2: Compiler et Tester

```bash
mvn clean package
mvn javafx:run
```

### Étape 3: Flux d'Utilisation

#### Pour le Client:
1. Va dans le profil → "Moyens de Paiement"
2. Clique sur "+ Ajouter un compte"
3. Remplis les données bancaires:
   - **Titulaire du compte** (nom complet)
   - **IBAN** (ex: FR1420041010050500013M02606)
   - **BIC** (ex: BNPAFRPP)
   - **Banque** (optionnel)
4. Coche "Utiliser par défaut" si tu veux l'utiliser pour les paiements
5. Clique "OK"

#### Modifier un compte:
1. Clique sur "Modifier" dans la liste
2. Change les infos
3. Clique "OK"

#### Supprimer un compte:
1. Clique sur "Supprimer"
2. Confirme

## 🔐 Sécurité

### ⚠️ Important pour la production:
Les IBAN/BIC doivent être **chiffrés** en base de données!

Voici comment ajouter le chiffrement:

```java
// Dans BankAccountService.java, add() method:
stmt.setString(3, encryptIBAN(account.getIban()));
stmt.setString(4, encryptBIC(account.getBic()));

// Méthodes d'encryptage:
private String encryptIBAN(String iban) {
    // Utilise une libraire comme Bouncy Castle ou Java's Cipher
    // Pour tester: retour simplement le chiffrement basique
    return iban; // TODO: Implémenter le vrai chiffrement
}
```

## 📊 Structure de la Table

```sql
CREATE TABLE bank_account (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    account_holder_name VARCHAR(255),
    iban VARCHAR(34),
    bic VARCHAR(11),
    bank_name VARCHAR(100),
    is_default BOOLEAN,
    date_added TIMESTAMP,
    date_modified TIMESTAMP
);
```

## 🎯 Intégration avec les Paiements

### Quand le client crée un contrat et veut payer:

```java
// Dans ClientPaymentsController ou une classe de paiement:
BankAccountService bankService = new BankAccountService();
BankAccount account = bankService.getDefaultBankAccount(clientId);

if (account != null) {
    // Créer l'escrow
    PaymentEscrow escrow = escrowService.createEscrow(
        contractId,
        clientId,
        freelancerId,
        amount
    );
    
    // Simuler le paiement
    simulatePayment(account, amount);
    
    // Marquer escrow comme payé
    escrow.setStatus("PENDING"); // Argent bloqué chez admin
} else {
    showAlert("Erreur", "Veuillez ajouter un compte bancaire d'abord");
}
```

## 🧪 Tests

### Test 1: Ajouter un compte
1. Ouvre le client
2. Va dans "Moyens de Paiement"
3. Clique "+ Ajouter un compte"
4. Remplis avec:
   - Titulaire: "John Doe"
   - IBAN: "FR1420041010050500013M02606"
   - BIC: "BNPAFRPP"
   - Banque: "BNP Paribas"
5. Valide et vérifie que le compte apparaît

### Test 2: Modifier un compte
1. Clique "Modifier" sur un compte
2. Change le titulaire
3. Valide et vérifie la mise à jour

### Test 3: Supprimer un compte
1. Clique "Supprimer"
2. Confirme
3. Vérifie que le compte disparaît

## 🐛 Troubleshooting

### Problème: Table bank_account not found
**Solution**: Exécute le script SQL `create_bank_account_table.sql`

### Problème: IBAN invalide
**Solution**: L'IBAN doit être au bon format (2 lettres pays + 2 chiffres + alphanumériques)

### Problème: Compte par défaut non sauvegardé
**Solution**: Vérifie que `is_default` est bien passé à la base de données

## 🔄 Prochaines Étapes

1. ✅ Intégrer le paiement réel (Stripe, PayPal, etc.)
2. ✅ Ajouter le chiffrement IBAN/BIC
3. ✅ Ajouter des limites de paiement
4. ✅ Ajouter l'historique des paiements
5. ✅ Ajouter les frais de transaction

## 📞 Support

Pour toute question, consulte la documentation ou contacte l'équipe.

---
**Dernière mise à jour**: 2026-03-01  
**Version**: 1.0

