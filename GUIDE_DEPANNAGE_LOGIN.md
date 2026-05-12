# 🔧 GUIDE DE DÉPANNAGE - Problème de Connexion

## ❌ PROBLÈME : "Je ne peux pas m'identifier à la plateforme"

### 🔍 Causes Possibles

1. **Mot de passe mal haché** (problème principal après migration Stripe)
2. **Compte désactivé** (`activated = false`)
3. **Compte verrouillé** (après 3 tentatives échouées)
4. **Email incorrect**
5. **Base de données non accessible**

---

## ✅ SOLUTIONS (Dans l'ordre)

### 🎯 **Solution 1 : Supprimer et recréer l'utilisateur**

**C'est la solution la plus simple et la plus sûre.**

#### Étape 1 : Ouvrir MySQL
```bash
mysql -u root -p
use uniearn;
```

#### Étape 2 : Exécuter le script
```sql
-- Supprimer le client
DELETE FROM client WHERE userID = (SELECT idUser FROM user WHERE email = 'clientjava@gmail.com');

-- Supprimer l'utilisateur
DELETE FROM user WHERE email = 'clientjava@gmail.com';

-- Vérifier
SELECT COUNT(*) FROM user WHERE email = 'clientjava@gmail.com';
-- Devrait retourner 0
```

#### Étape 3 : Relancer l'application et créer un nouveau compte
1. Lancez l'application
2. Allez dans "Inscription"
3. Créez un nouveau compte avec le même email
4. Le mot de passe sera correctement haché cette fois !

---

### 🎯 **Solution 2 : Réinitialiser le mot de passe manuellement**

**Si vous voulez garder l'utilisateur existant.**

#### Étape 1 : Générer un nouveau hash

**Option A - Avec l'utilitaire Java :**
```bash
cd C:\Users\MSI\Desktop\uniearn_java
mvn compile
mvn exec:java -Dexec.mainClass="uniearn.utils.user.PasswordHashGenerator"
```

Entrez le nouveau mot de passe quand demandé, et copiez le hash généré.

**Option B - Via le code Java directement :**
```java
// Dans n'importe quelle classe main()
String newPassword = "MonNouveauMotDePasse123";
String hash = PasswordUtil.hashPassword(newPassword);
System.out.println("Hash: " + hash);
```

#### Étape 2 : Mettre à jour la base de données
```sql
UPDATE user 
SET password = 'LE_HASH_GENERE_ICI'
WHERE email = 'clientjava@gmail.com';
```

#### Étape 3 : Activer le compte
```sql
UPDATE user 
SET activated = TRUE
WHERE email = 'clientjava@gmail.com';
```

#### Étape 4 : Tester la connexion
- Relancez l'application
- Connectez-vous avec le nouveau mot de passe

---

### 🎯 **Solution 3 : Activer un compte désactivé**

Si le message d'erreur dit "Compte désactivé" :

```sql
UPDATE user 
SET activated = TRUE 
WHERE email = 'clientjava@gmail.com';
```

---

### 🎯 **Solution 4 : Débloquer un compte verrouillé**

Si le message dit "⏳ Compte verrouillé" :

**Le verrouillage est temporaire (5 minutes) et géré en mémoire.**

**Solutions :**
1. **Attendre 5 minutes** puis réessayer
2. **OU redémarrer l'application** (réinitialise les tentatives)

---

## 🔍 VÉRIFICATIONS

### 1. Vérifier l'utilisateur dans la base de données

```sql
-- Voir l'utilisateur
SELECT 
    u.idUser, 
    u.name, 
    u.email, 
    u.role,
    u.activated,
    LENGTH(u.password) as password_length,
    SUBSTRING(u.password, 1, 10) as password_start
FROM user u
WHERE u.email = 'clientjava@gmail.com';
```

**Ce qu'il faut vérifier :**
- ✅ `activated` doit être **1** (TRUE)
- ✅ `password_length` doit être **60** (pour BCrypt)
- ✅ `password_start` doit commencer par **$2a$** ou **$2b$**

### 2. Vérifier les logs de l'application

Quand vous essayez de vous connecter, regardez la console :

**Messages normaux :**
```
✓ Password hashed successfully
✓ User created with hashed password. User ID: 88
✓ Login: ClientName
```

**Messages d'erreur :**
```
⚠ Password verification failed: invalid hash format  ❌ PROBLÈME !
⚠ Password verification failed: password does not match
⛔ Compte verrouillé
```

---

## 📊 DIAGNOSTIC COMPLET

### Exécuter ce script SQL pour tout vérifier :

```sql
-- ================================================================
-- DIAGNOSTIC COMPLET
-- ================================================================

-- 1. Vérifier l'utilisateur
SELECT 
    '1. USER INFO' as step,
    u.idUser, 
    u.name, 
    u.email, 
    u.role,
    u.activated as is_activated,
    LENGTH(u.password) as pwd_length,
    SUBSTRING(u.password, 1, 10) as pwd_start,
    CASE 
        WHEN LENGTH(u.password) = 60 THEN '✅ Longueur OK'
        ELSE '❌ Longueur incorrecte'
    END as pwd_status
FROM user u
WHERE u.email = 'clientjava@gmail.com';

-- 2. Vérifier le client associé
SELECT 
    '2. CLIENT INFO' as step,
    c.idClient,
    c.userID,
    c.company,
    c.amount,
    c.rating
FROM client c
WHERE c.userID = (SELECT idUser FROM user WHERE email = 'clientjava@gmail.com');

-- 3. Statistiques générales
SELECT 
    '3. STATS' as step,
    role,
    COUNT(*) as total,
    SUM(CASE WHEN activated = 1 THEN 1 ELSE 0 END) as activated_count,
    SUM(CASE WHEN activated = 0 THEN 1 ELSE 0 END) as deactivated_count
FROM user
GROUP BY role;
```

---

## ⚡ SOLUTION RAPIDE (Recommandée)

**La solution la plus simple en 3 commandes :**

```sql
-- 1. Supprimer l'ancien utilisateur
DELETE FROM client WHERE userID = (SELECT idUser FROM user WHERE email = 'clientjava@gmail.com');
DELETE FROM user WHERE email = 'clientjava@gmail.com';

-- 2. Vérifier que c'est supprimé
SELECT COUNT(*) FROM user WHERE email = 'clientjava@gmail.com';
```

Puis dans l'application :
1. Créer un nouveau compte avec le même email
2. Se connecter avec le nouveau mot de passe
3. ✅ Ça marche !

---

## 🆘 SI RIEN NE FONCTIONNE

### Créer un utilisateur de test directement en SQL

```sql
-- Mot de passe haché pour "Test123" (exemple)
INSERT INTO user (name, email, password, role, activated)
VALUES (
    'Test User',
    'test@uniearn.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO',  -- Password: Test123
    'CLIENT',
    TRUE
);

-- Récupérer l'ID du user créé
SET @userId = LAST_INSERT_ID();

-- Créer le client associé
INSERT INTO client (userID, amount, rating, company, industry)
VALUES (@userId, 0, 0, 'Test Company', 'IT');
```

**Connexion :**
- Email : `test@uniearn.com`
- Password : `Test123`

---

## 📝 NOTES IMPORTANTES

### Comment reconnaître un mot de passe bien haché ?

✅ **BON HASH (BCrypt) :**
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhkO
```
- Commence par `$2a$` ou `$2b$` ou `$2y$`
- Longueur exacte : **60 caractères**

❌ **MAUVAIS HASH (double hachage) :**
```
$2a$10$...plus de 60 caractères...
```
- Longueur > 60 caractères
- A été haché 2 fois par erreur

### Pourquoi ce problème est arrivé ?

1. **Avant le fix :** `UserService.addUser()` hachait TOUJOURS le mot de passe
2. **Le bug :** Si le mot de passe était déjà haché, il était haché 2 fois
3. **Résultat :** Hash invalide → impossible de se connecter
4. **Le fix :** Maintenant on vérifie si c'est déjà haché avant de hacher

### Le problème est-il résolu pour les nouveaux comptes ?

✅ **OUI !** Depuis la correction dans `UserService.java`, tous les **nouveaux** comptes créés auront un mot de passe correctement haché (une seule fois).

⚠️ **MAIS** les anciens comptes créés AVANT le fix ont toujours un mot de passe invalide et doivent être recréés.

---

## 🎯 RECOMMANDATION FINALE

**Pour clientjava@gmail.com :**

1. Exécutez le script `fix_login_problem.sql` (fourni)
2. Supprimez l'ancien compte
3. Recréez un nouveau compte via l'interface
4. Connectez-vous avec le nouveau mot de passe

**Temps nécessaire :** 2 minutes ⏱️

---

**Date** : 2026-05-11  
**Problème** : Mot de passe double-haché (invalide)  
**Solution** : Supprimer et recréer le compte  
**Statut** : ✅ Le bug est corrigé pour les nouveaux comptes

