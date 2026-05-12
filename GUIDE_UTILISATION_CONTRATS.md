# 🎓 GUIDE D'UTILISATION - INTÉGRATION CONTRATS

## 📱 INTERFACE CLIENT

### Accéder à "Mes Contrats"
1. **Se connecter** en tant que Client
2. **Cliquer** sur le bouton "Mes contrats" dans la barre latérale
3. **Voir** la liste de tous les contrats du client

### Fonctionnalités Disponibles

#### 🔍 Recherche
```
- Tapez dans le champ "Rechercher un contrat..."
- La recherche se fait sur : ID, Type, Description
- Résultats mis à jour en temps réel
```

#### 🏷️ Filtrage par Statut
```
Sélectionnez dans le dropdown :
- Tous les statuts (défaut)
- En attente
- Actif
- Signé
- Complété
- Annulé
```

#### 👁️ Voir les Détails
```
Cliquez sur "👁 Voir les détails"
- ID du contrat
- Type
- Description complète
- Montant
- Statut
- Date de création
```

#### 📥 Télécharger
```
Cliquez sur "📥 Télécharger"
(Fonctionnalité à compléter)
- Génère un PDF du contrat
- Sauvegarde en local
```

#### 🗑️ Supprimer
```
Cliquez sur "🗑 Supprimer"
- Confirmation demandée
- Contrat supprimé définitivement
- Liste mise à jour
```

#### ➕ Créer un Contrat
```
Cliquez sur "➕ Nouveau Contrat"
- Sélectionnez un template
- Remplissez les informations
- Sélectionnez un freelancer
- Confirmez la création
```

---

## 💼 INTERFACE FREELANCER

### Accéder à "Mes Contrats"
1. **Se connecter** en tant que Freelancer
2. **Cliquer** sur le bouton "Mes contrats" dans la barre latérale
3. **Voir** la liste de tous les contrats du freelancer

### Fonctionnalités Disponibles

#### 🔍 Recherche & Filtre
```
Identique au client :
- Recherche dynamique
- Filtrage par statut
```

#### 👁️ Voir les Détails
```
Identique au client
```

#### ✍️ Signer un Contrat
```
Cliquez sur "✍ Signer"
- Ouvre le formulaire de signature
- Signe le contrat numériquement
- Statut passe à "Signé"
```

#### 📥 Télécharger
```
Cliquez sur "📥 Télécharger"
- Télécharge le PDF du contrat
```

#### 📊 Différences avec Client
```
Freelancer NE PEUT PAS :
- Supprimer un contrat
- Créer un contrat

Freelancer PEUT :
- Signer un contrat
- Télécharger un contrat
- Voir les détails
```

---

## 🔐 INTERFACE ADMIN

### Accéder à "Gestion des Templates"
1. **Se connecter** en tant que Admin
2. **Dashboard** s'affiche avec statistiques
3. **Cliquer** sur un bouton "Templates" (à ajouter dans dashboard) OU
4. **Accès direct** via menu latéral (si configuré)

### Fonctionnalités Disponibles

#### 🔍 Recherche
```
Tapez dans le champ "Rechercher un template..."
- Recherche sur nom et description
- Résultats mis à jour en temps réel
```

#### ➕ Créer un Template
```
Cliquez sur "➕ Nouveau Template"

Fenêtre de création :
1. Nom du template *
   - Requis
   - Ex: "Contrat Développeur"

2. Description
   - Optionnel
   - Ex: "Pour les missions de développement"

3. Contenu
   - Code ou texte du contrat
   - Peut inclure des variables : {CLIENT_NAME}, {FREELANCER_NAME}, etc.

Cliquez "Créer" pour enregistrer
```

#### 👁️ Aperçu
```
Cliquez sur "👁 Aperçu"
- Fenêtre de lecture du contenu
- Voir comment le contrat s'affichera
- Ne peut pas éditer depuis cet aperçu
```

#### ✏️ Éditer
```
Cliquez sur "✏ Éditer"

Fenêtre d'édition :
- Modifiez le nom
- Modifiez la description
- Modifiez le contenu

Cliquez "Enregistrer" pour sauvegarder
```

#### 🗑️ Supprimer
```
Cliquez sur "🗑 Supprimer"
- Confirmation obligatoire
- Template supprimé définitivement
- Les contrats utilisant ce template restent
```

#### 📋 Informations Affichées
```
Pour chaque template :
- Nom (titre)
- Description (sous-titre)
- ID (badge)
- Date de création
- Boutons d'action (Aperçu, Éditer, Supprimer)
```

---

## 🎯 CAS D'UTILISATION COURANTS

### CLIENT : Créer un Contrat
```
1. Aller à Mes contrats
2. Cliquer "➕ Nouveau Contrat"
3. Sélectionner un template
4. Remplir les informations
5. Choisir un freelancer
6. Définir la période
7. Cliquer "Créer"
8. Contrat créé avec statut "En attente"
9. Freelancer reçoit une notification
```

### FREELANCER : Signer un Contrat
```
1. Aller à Mes contrats
2. Chercher le contrat (si nécessaire)
3. Cliquer "👁 Voir les détails"
4. Vérifier les conditions
5. Cliquer "✍ Signer"
6. Signer numériquement
7. Contrat passe à "Signé"
8. Client en est notifié
```

### ADMIN : Gérer les Templates
```
Créer :
1. Aller à Gestion des Templates
2. Cliquer "➕ Nouveau Template"
3. Entrer les informations
4. Cliquer "Créer"

Éditer :
1. Chercher le template
2. Cliquer "✏ Éditer"
3. Modifier les informations
4. Cliquer "Enregistrer"

Supprimer :
1. Chercher le template
2. Cliquer "🗑 Supprimer"
3. Confirmer la suppression
4. Template supprimé
```

---

## ⚠️ POINTS IMPORTANTS

### Validations
- ✅ Les champs obligatoires sont marqués *
- ✅ Les données sont validées avant enregistrement
- ⚠️ Les confirmations sont demandées avant suppression

### Statuts de Contrat
```
En attente   → Créé, en attente de signature
Actif        → En cours d'exécution
Signé        → Signé par les deux parties
Complété     → Travail terminé
Annulé       → Contrat annulé
```

### Permissions
```
CLIENT :
- Peut créer, voir, modifier, supprimer ses contrats
- Peut voir les contrats avec freelancers

FREELANCER :
- Peut voir et signer ses contrats
- Peut télécharger les contrats
- NE peut pas supprimer

ADMIN :
- Gère les templates
- Peut voir tous les contrats
- Peut voir toutes les templates
```

---

## 🔔 NOTIFICATIONS & STATUTS

### Messages de Succès
```
✅ "Le contrat a été créé avec succès"
✅ "Le contrat a été modifié avec succès"
✅ "Le contrat a été supprimé avec succès"
✅ "Le template a été créé avec succès"
```

### Messages d'Erreur
```
❌ "Aucun contrat trouvé"
❌ "Impossible de charger les contrats"
❌ "Impossible de supprimer le contrat"
❌ "Le nom du template est obligatoire"
```

### Indicateurs Visuels
```
Statuts colorés :
🟡 En attente (Jaune)
🔵 Actif (Bleu)
🟢 Signé (Vert)
🟢 Complété (Vert foncé)
🔴 Annulé (Rouge)
```

---

## 💡 CONSEILS D'UTILISATION

### Pour les Clients
1. **Créez des templates** variés pour différents types de missions
2. **Recherchez** les freelancers avant de créer des contrats
3. **Vérifiez les détails** avant de finaliser
4. **Conservez** des copies de contrats importants

### Pour les Freelancers
1. **Révisez** chaque contrat avant de signer
2. **Posez des questions** si quelque chose n'est pas clair
3. **Téléchargez** les contrats pour vos dossiers
4. **Respectez** les délais définis dans le contrat

### Pour les Admins
1. **Maintenez** les templates à jour
2. **Supprimez** les templates obsolètes
3. **Vérifiez** régulièrement les contrats
4. **Surveillez** les conflits ou problèmes

---

## 🐛 DÉPANNAGE

### "Aucun contrat ne s'affiche"
```
Vérifications :
- Êtes-vous connecté(e) ?
- Êtes-vous le propriétaire du contrat ?
- Essayez de rafraîchir la page
- Vérifiez les filtres et recherche
```

### "Le bouton 'Mes contrats' ne fonctionne pas"
```
Solutions :
- Rechargez l'application
- Vérifiez votre connexion
- Vérifiez les logs pour les erreurs
- Contactez le support
```

### "Je ne peux pas signer"
```
Vérifications :
- Êtes-vous freelancer ?
- Le contrat est-il dans le bon statut ?
- Vérifiez les droits d'accès
```

---

## 📞 SUPPORT

En cas de problème :
1. **Note** : Message d'erreur exact
2. **Décrivez** : Ce que vous faisiez
3. **Contactez** : L'équipe support
4. **Attendez** : Confirmation du problème

---

## 🎓 RÉSUMÉ RAPIDE

| Rôle | Accès | Actions |
|---|---|---|
| **Client** | Mes contrats | Créer, Voir, Télécharger, Supprimer |
| **Freelancer** | Mes contrats | Voir, Signer, Télécharger |
| **Admin** | Templates | Créer, Voir, Éditer, Supprimer |


