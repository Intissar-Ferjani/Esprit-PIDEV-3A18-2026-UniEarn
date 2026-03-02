# Guide d'Intégration des Paiements en Escrow 💳

## 📋 Vue d'ensemble

Ce guide explique comment intégrer le système de paiements en escrow dans UniEarn. Le système fonctionne comme suit:

```
CLIENT              →  MONTANT BLOQUÉ  →  FREELANCER
           créé contrat    (chez admin)    reçoit après validation
```

## 🏗️ Fichiers Créés

### 1. **Entités**
- `PaymentEscrow.java` - Modèle de données pour les paiements en escrow

### 2. **Services**
- `EscrowPaymentService.java` - Service pour gérer les opérations d'escrow

### 3. **Contrôleurs**
- `ClientPaymentsController.java` - Gestion paiements pour le client
- `FreelancerPaymentsController.java` - Gestion revenus pour le freelancer
- `AdminPaymentsController.java` - Gestion/validation des escrows par l'admin

### 4. **Interfaces (FXML)**
- `client-payments.fxml` - Section Paiements du dashboard client
- `freelancer-payments.fxml` - Section Revenus du profil freelancer
- `admin-payments.fxml` - Section Gestion Escrow du dashboard admin

### 5. **Base de Données**
- `create_payment_escrow_table.sql` - Script de création de la table

## 🔧 Étapes d'Intégration

### Étape 1: Créer la Table en Base de Données

```sql
-- Exécute le fichier SQL fourni
mysql -u root -p uniearn_db < create_payment_escrow_table.sql
```

Ou exécute manuellement le contenu du fichier dans phpMyAdmin.

### Étape 2: Ajouter les Boutons de Navigation

#### Dans `client-profile.fxml` (ligne ~75):
```xml
<Button onAction="#handlePayments" prefHeight="42.0" prefWidth="220.0" styleClass="nav-button">
    <graphic><HBox alignment="CENTER_LEFT" spacing="12.0"><children>
        <FontIcon iconLiteral="fas-credit-card" iconSize="15" />
        <Label style="-fx-font-size: 14px;" text="Paiements" />
    </children></HBox></graphic>
</Button>
```

#### Dans `ClientProfileController.java`:
```java
@FXML
private void handlePayments() {
    loadFXML("profile/client/client-payments.fxml");
    updateNavStyle("btnPayments");
}
```

### Étape 3: Ajouter pour le Freelancer

#### Dans `freelancer-profile.fxml`:
```xml
<Button onAction="#handlePayments" prefHeight="42.0" prefWidth="220.0" styleClass="nav-button">
    <graphic><HBox alignment="CENTER_LEFT" spacing="12.0"><children>
        <FontIcon iconLiteral="fas-wallet" iconSize="15" />
        <Label style="-fx-font-size: 14px;" text="Revenus" />
    </children></HBox></graphic>
</Button>
```

#### Dans `FreelancerProfileController.java`:
```java
@FXML
private void handlePayments() {
    loadFXML("profile/freelancer/freelancer-payments.fxml");
    updateNavStyle("btnPayments");
}
```

### Étape 4: Intégrer au Dashboard Admin

Ajoute la section Paiements au dashboard admin dans `admin-dashboard.fxml`.

## 📊 Flux de Fonctionnement

### Client
1. ✅ Crée un contrat → Escrow créé (PENDING)
2. ✅ Voir montants bloqués chez admin
3. ✅ Voir montants libérés disponibles

### Freelancer
1. 📝 Livre le projet
2. ⏳ Voir montants en attente (COMPLETED)
3. 💰 Voir montants libérés (RELEASED)
4. ✅ Retirer depuis portefeuille

### Admin
1. 📋 Voir tous les escrows en attente
2. ✅ Marquer comme "livré" quand projet est reçu
3. 🔓 Libérer le montant au freelancer
4. 🔄 Rembourser le client si nécessaire

## 🔗 Intégration avec les Contrats

### Quand un contrat est créé:
```java
// Dans ClientContractDialogController.createContract()
ContratService contratService = new ContratService();
Contrat contrat = contratService.createContrat(...);

if (contrat != null && contrat.getIdContract() > 0) {
    // Créer l'escrow associé
    EscrowPaymentService escrowService = new EscrowPaymentService();
    escrowService.createEscrow(
        contrat.getIdContract(),
        clientID,
        contrat.getFreelancerID(),
        new BigDecimal(contrat.getAmount())
    );
}
```

### Quand le freelancer signe:
```java
// Dans ContractSignatureController.handleFreelancerSignature()
if (allSignaturesComplete) {
    // Marquer contrat comme ACTIF
    contratService.updateContractStatus(contractId, 2); // 2 = ACTIF
    
    // Marquer escrow comme COMPLETED (livraison en cours)
    escrowService.markAsCompleted(escrowId);
}
```

## 💾 Statuts d'Escrow

| Statut | Signification | Qui voit | Actions |
|--------|---------------|----------|---------|
| **PENDING** | Montant bloqué | Admin | Marquer livré / Rembourser |
| **COMPLETED** | Projet livré | Admin | Libérer / Rembourser |
| **RELEASED** | Montant libéré | Freelancer | Retirer |
| **REFUNDED** | Montant remboursé | Client | N/A |

## 🎯 Cas d'Usage

### Cas 1: Paiement Réussi
```
Client crée contrat (100 TND)
    ↓
Escrow: PENDING (montant bloqué chez admin)
    ↓
Freelancer livre le projet
    ↓
Admin valide la livraison → COMPLETED
    ↓
Admin libère le montant → RELEASED
    ↓
Freelancer reçoit 100 TND
```

### Cas 2: Remboursement
```
Client crée contrat (100 TND)
    ↓
Escrow: PENDING (montant bloqué)
    ↓
Client/Admin demande remboursement
    ↓
Admin rembourse → REFUNDED
    ↓
Client reçoit 100 TND
```

## 🧪 Test

### Test Client:
1. Crée un contrat
2. Vas dans "Paiements"
3. Vérifies que le montant s'affiche (PENDING)

### Test Freelancer:
1. Vas dans "Revenus"
2. Vérifies les montants en attente
3. Clique "Détails" pour voir les informations

### Test Admin:
1. Vas dans "Gestion Paiements"
2. Vérifies les montants totaux
3. Teste "Marquer livré" → "Libérer" → valide

## ⚙️ Personnalisation

### Modifier les statuts
Les statuts peuvent être customisés dans le switch/case des contrôleurs:
```java
private String translateStatus(String status) {
    return switch (status) {
        case "PENDING" -> "Bloqué ⏳";
        case "COMPLETED" -> "Livré 📦";
        // ...
    };
}
```

### Modifier les montants
```java
// Dans EscrowPaymentService
escrow.setAmount(new BigDecimal("1000.50")); // TND
```

### Ajouter des commissions admin
```java
BigDecimal adminCommission = amount.multiply(new BigDecimal("0.10")); // 10%
BigDecimal freelancerAmount = amount.subtract(adminCommission);
```

## 📱 API REST (Optionnel)

Si tu veux ajouter une API REST:
```java
@RestController
@RequestMapping("/api/payments")
public class PaymentRestController {
    
    @GetMapping("/escrow/{id}")
    public ResponseEntity<PaymentEscrow> getEscrow(@PathVariable int id) {
        return ResponseEntity.ok(escrowService.getEscrowById(id));
    }
    
    @PostMapping("/escrow/release/{id}")
    public ResponseEntity<String> releasePayment(@PathVariable int id, @RequestBody String notes) {
        if (escrowService.releasePayment(id, notes)) {
            return ResponseEntity.ok("Paiement libéré");
        }
        return ResponseEntity.badRequest().build();
    }
}
```

## 🐛 Troubleshooting

### Problème: Escrow pas créé après le contrat
**Solution**: Assure-toi que `createEscrow()` est appelé dans `createContrat()`

### Problème: Montants ne s'affichent pas
**Solution**: Vérifie que l'ID client/freelancer est correctement défini

### Problème: Erreur de clé étrangère
**Solution**: Exécute le script SQL pour créer la table avec les bonnes contraintes

## 📞 Support

Pour toute question, consulte la documentation ou contacte l'équipe de développement.

---
**Dernière mise à jour**: 2026-03-01  
**Version**: 1.0

