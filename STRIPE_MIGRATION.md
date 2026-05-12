# 🔄 MIGRATION VERS STRIPE - Documentation

## 📋 Résumé des changements

Le système de paiement UniEarn a été **migré de l'ancien système PaymentEscrow vers Stripe**. Ce document explique les changements et la nouvelle architecture.

---

## 🆕 Nouvelle Architecture

### 1. **Service Stripe**
- **Fichier** : `uniearn.services.payment.StripePaymentService`
- **Responsabilités** :
  - Créer des sessions de paiement Stripe Checkout
  - Gérer les webhooks Stripe (à implémenter)
  - Mode simulation pour le développement
  
### 2. **Controllers Client**
- **Fichier** : `uniearn.controller.profile.client.ClientPaymentsController`
- **Remplace** : Ancien système basé sur `PaymentEscrow`
- **Fonctionnalités** :
  - Affiche les contrats à payer (status = 3 = signed)
  - Affiche les contrats payés (status >= 4 = funded/released/completed)
  - Bouton "Pay Now" qui redirige vers Stripe Checkout
  - Statistiques en temps réel
  
### 3. **Controllers Freelancer**
- **Fichier** : `uniearn.controller.profile.freelancer.FreelancerPaymentsController`
- **Remplace** : Ancien système basé sur `PaymentEscrow`
- **Fonctionnalités** :
  - Affiche les fonds bloqués en escrow (status = 4 = funded)
  - Affiche les fonds reçus (status >= 5 = released/completed)
  - Statistiques des montants

---

## 🔢 Statuts des Contrats

| Status | Valeur | Description | Ancien système | Nouveau système |
|--------|--------|-------------|----------------|-----------------|
| Draft | 0 | Brouillon | ❌ | ❌ |
| Pending Client | 1 | En attente client | ❌ | ❌ |
| Pending Freelancer | 2 | En attente freelancer | ❌ | ❌ |
| **Signed** | **3** | **Signé (à payer)** | ❌ | ✅ **Affiche "Pay Now"** |
| **Funded** | **4** | **Payé (en escrow)** | ✅ PENDING | ✅ **Bloqué chez Stripe** |
| **Released** | **5** | **Libéré** | ✅ RELEASED | ✅ **Transféré au freelancer** |
| **Completed** | **6** | **Complété** | ✅ COMPLETED | ✅ **Projet terminé** |
| Cancelled | 7 | Annulé | ✅ REFUNDED | ⚠️ À gérer |

---

## 🔧 Configuration Stripe

### 1. Fichier `.env`
Créez un fichier `.env` à la racine du projet :

```env
STRIPE_SECRET_KEY=sk_test_votre_cle_secrete_stripe
STRIPE_PUBLISHABLE_KEY=pk_test_votre_cle_publique_stripe
```

### 2. Mode Test vs Production

**Mode Test (Développement)** :
- Utilisez les clés commençant par `sk_test_` et `pk_test_`
- Cartes de test : https://stripe.com/docs/testing

**Mode Production** :
- Utilisez les clés commençant par `sk_live_` et `pk_live_`
- **ATTENTION** : Argent réel !

### 3. Mode Simulation (Sans Stripe)

Si Stripe n'est pas configuré, le système passe automatiquement en **mode simulation** :
- Pas besoin de clés API
- Les paiements sont simulés instantanément
- Utile pour les tests locaux

---

## 📁 Fichiers Modifiés

### ✅ Fichiers Créés / Remplacés

| Fichier | Type | Description |
|---------|------|-------------|
| `StripePaymentService.java` | Service | Service principal Stripe |
| `ClientPaymentsController.java` | Controller | **REMPLACÉ** - Nouveau avec Stripe |
| `FreelancerPaymentsController.java` | Controller | **REMPLACÉ** - Nouveau avec Stripe |
| `client-stripe-payments.fxml` | Vue | Nouvelle vue pour les paiements client |

### ⚠️ Fichiers Dépréciés (conservés pour compatibilité)

| Fichier | Statut | Raison |
|---------|--------|--------|
| `EscrowPaymentService.java` | @Deprecated | Remplacé par StripePaymentService |
| `PaymentEscrow.java` | Conservé | Peut contenir des données anciennes |

### ❌ Fichiers Supprimés

| Fichier | Raison |
|---------|--------|
| Ancien `ClientPaymentsController.java` | Remplacé par version Stripe |
| Ancien `FreelancerPaymentsController.java` | Remplacé par version Stripe |

---

## 🚀 Comment Utiliser

### Pour le Client

1. **Connectez-vous** en tant que Client
2. **Allez sur** "Mes Paiements" dans le menu
3. **Vous verrez** :
   - **Section "À Payer"** : Contrats signés mais non payés
   - **Section "Payés"** : Contrats déjà payés et sécurisés
4. **Cliquez sur** "💳 Pay Now" pour payer
5. **Vous serez redirigé** vers Stripe Checkout
6. **Payez avec** votre carte bancaire
7. **Revenez** à l'application après le paiement

### Pour le Freelancer

1. **Connectez-vous** en tant que Freelancer
2. **Allez sur** "Mes Paiements" dans le menu
3. **Vous verrez** :
   - **Section "Bloqués"** : Fonds en escrow chez Stripe
   - **Section "Reçus"** : Fonds déjà transférés sur votre compte
4. **Attendez** que l'admin libère les fonds

### Pour l'Admin

1. **Connectez-vous** en tant qu'Admin
2. **Allez sur** "Gestion Escrow"
3. **Vous pouvez** :
   - Voir tous les fonds en escrow
   - Libérer les fonds vers le freelancer
   - Rembourser le client en cas de litige

---

## 🔐 Sécurité

### Avantages de Stripe

✅ **Conformité PCI-DSS** : Stripe gère la sécurité des cartes
✅ **Authentification 3D Secure** : Protection contre la fraude
✅ **Chiffrement de bout en bout** : Données sécurisées
✅ **Protection contre les chargebacks** : Gestion des litiges
✅ **Webhooks sécurisés** : Vérification des signatures

### Ce que UniEarn ne stocke PAS

❌ Numéros de carte bancaire
❌ CVV / CVC
❌ Données sensibles de paiement

Tout est géré par Stripe de manière sécurisée !

---

## 📊 Flux de Paiement

```
1. Client crée un contrat
   ↓
2. Les deux parties signent
   ↓ (Status = 3 : Signed)
3. Client clique "Pay Now"
   ↓
4. Redirection vers Stripe Checkout
   ↓
5. Client paie avec sa carte
   ↓ (Status = 4 : Funded)
6. Fonds bloqués en escrow chez Stripe
   ↓
7. Freelancer livre le travail
   ↓
8. Admin libère les fonds
   ↓ (Status = 5 : Released)
9. Stripe transfère l'argent au freelancer
   ↓ (Status = 6 : Completed)
10. Projet terminé ✅
```

---

## 🐛 Dépannage

### Problème : "Stripe is not initialized"

**Solution** : Créez un fichier `.env` avec votre clé Stripe ou le système utilisera le mode simulation.

### Problème : Paiement non mis à jour après Stripe

**Solution** : Implémentez les webhooks Stripe pour recevoir les confirmations de paiement automatiquement.

### Problème : "Desktop not supported"

**Solution** : Sur certains systèmes, l'ouverture du navigateur peut échouer. Copiez manuellement l'URL affichée dans la console.

---

## 📞 Support

Pour toute question sur l'intégration Stripe :
- **Documentation Stripe** : https://stripe.com/docs
- **Dashboard Stripe** : https://dashboard.stripe.com

---

## 📝 TODO / Améliorations Futures

- [ ] Implémenter les webhooks Stripe pour les confirmations automatiques
- [ ] Ajouter le support de Stripe Connect pour les transferts directs
- [ ] Implémenter les remboursements via Stripe API
- [ ] Ajouter l'historique détaillé des transactions
- [ ] Gérer les devises multiples (USD, EUR, TND)
- [ ] Ajouter des factures PDF automatiques

---

**Date de migration** : 2026-05-11
**Version** : 2.0.0
**Auteur** : GitHub Copilot

