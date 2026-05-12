-- ================================================================
-- SCRIPT DE DÉPANNAGE - Problème de connexion
-- ================================================================

-- OPTION 1: Supprimer l'utilisateur problématique et le recréer
-- ----------------------------------------------------------------
-- Ceci supprimera l'utilisateur clientjava@gmail.com avec le mot de passe mal haché

-- 1. Vérifier l'utilisateur
SELECT u.idUser, u.name, u.email, u.password, LENGTH(u.password) as pwd_length, u.activated
FROM user u
WHERE u.email = 'clientjava@gmail.com';

-- 2. Supprimer le client (cascade vers user si configuré)
DELETE FROM client WHERE userID = (SELECT idUser FROM user WHERE email = 'clientjava@gmail.com');

-- 3. Supprimer l'utilisateur
DELETE FROM user WHERE email = 'clientjava@gmail.com';

-- 4. Vérifier que l'utilisateur est supprimé
SELECT COUNT(*) as remaining FROM user WHERE email = 'clientjava@gmail.com';
-- Devrait retourner 0


-- ================================================================
-- OPTION 2: Réinitialiser le mot de passe d'un utilisateur existant
-- ================================================================
-- Si vous voulez garder l'utilisateur mais changer son mot de passe

-- Mot de passe haché pour "Password123" (à titre d'exemple)
-- ATTENTION: Ce hash est un exemple, il faut utiliser celui généré par votre application

-- Pour générer un nouveau hash, utilisez ce code Java:
-- String newPassword = "VotreNouveauMotDePasse";
-- String hashedPassword = PasswordUtil.hashPassword(newPassword);
-- System.out.println("Hash: " + hashedPassword);

-- Puis exécutez cette requête avec le hash généré:
-- UPDATE user
-- SET password = '$2a$10$VotreHashIci'
-- WHERE email = 'clientjava@gmail.com';


-- ================================================================
-- OPTION 3: Activer un compte désactivé
-- ================================================================
-- Si le compte existe mais est désactivé

UPDATE user
SET activated = TRUE
WHERE email = 'clientjava@gmail.com';


-- ================================================================
-- OPTION 4: Vérifier et débloquer un compte verrouillé
-- ================================================================
-- Le système peut avoir verrouillé le compte après 3 tentatives échouées
-- Il n'y a pas de table pour ça car c'est géré en mémoire (LoginAttemptService)
-- Solution: Redémarrer l'application pour réinitialiser les tentatives


-- ================================================================
-- VÉRIFICATIONS GÉNÉRALES
-- ================================================================

-- Voir tous les utilisateurs
SELECT idUser, name, email, role, activated,
       LENGTH(password) as pwd_length,
       SUBSTRING(password, 1, 10) as pwd_start
FROM user
ORDER BY idUser DESC
LIMIT 10;

-- Vérifier les clients récents
SELECT c.idClient, c.userID, u.name, u.email, u.activated, c.company
FROM client c
JOIN user u ON c.userID = u.idUser
ORDER BY c.idClient DESC
LIMIT 10;

-- Compter les utilisateurs par rôle
SELECT role, COUNT(*) as count,
       SUM(CASE WHEN activated = 1 THEN 1 ELSE 0 END) as activated_count
FROM user
GROUP BY role;


-- ================================================================
-- NOTES IMPORTANTES
-- ================================================================

/*
1. Le hash BCrypt correct commence toujours par $2a$ ou $2b$ ou $2y$
2. La longueur d'un hash BCrypt est toujours 60 caractères
3. Si le hash ne fait pas 60 caractères, il est invalide
4. Si le hash a été haché 2 fois, il sera plus long et invalide

APRÈS AVOIR CORRIGÉ:
- Redémarrez l'application
- Créez un nouveau compte OU reconnectez-vous
- Le nouveau mot de passe sera correctement haché (une seule fois)
*/

-- ================================================================

