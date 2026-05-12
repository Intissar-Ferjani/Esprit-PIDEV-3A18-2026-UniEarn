-- ===============================================
-- Script pour corriger le problème de mot de passe
-- du client clientjava@gmail.com
-- ===============================================

-- 1. Vérifier l'utilisateur existant
SELECT
    u.idUser,
    u.name,
    u.email,
    u.password,
    LENGTH(u.password) as password_length,
    u.role,
    c.idClient,
    c.company
FROM user u
LEFT JOIN client c ON u.idUser = c.userID
WHERE u.email = 'clientjava@gmail.com';

-- 2. Supprimer le client (cela va aussi supprimer l'utilisateur si CASCADE est configuré)
-- Si vous avez des foreign keys avec ON DELETE CASCADE
DELETE FROM client WHERE userID = (SELECT idUser FROM user WHERE email = 'clientjava@gmail.com');

-- 3. Supprimer l'utilisateur
DELETE FROM user WHERE email = 'clientjava@gmail.com';

-- 4. Supprimer les photos d'intrusion associées (optionnel)
-- Les fichiers dans uploads/intrusion/clientjava@gmail.com.png doivent être supprimés manuellement

-- ===============================================
-- NOTES :
-- ===============================================
-- Après avoir exécuté ce script :
-- 1. Redémarrez l'application
-- 2. Créez à nouveau le client avec l'interface
-- 3. Le mot de passe sera correctement haché cette fois
-- 4. La connexion devrait fonctionner
-- ===============================================

-- Vérification finale (devrait retourner 0 lignes)
SELECT COUNT(*) as remaining_users
FROM user
WHERE email = 'clientjava@gmail.com';

