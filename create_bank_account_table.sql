-- Script de création de la table bank_account pour stocker les données bancaires

CREATE TABLE IF NOT EXISTS bank_account (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    iban VARCHAR(34) NOT NULL,
    bic VARCHAR(11) NOT NULL,
    bank_name VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    date_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    -- Contrainte de clé étrangère
    CONSTRAINT fk_bank_account_user FOREIGN KEY (user_id)
        REFERENCES user(idUser) ON DELETE CASCADE ON UPDATE CASCADE,

    -- Index pour les recherches
    INDEX idx_user_id (user_id),
    INDEX idx_is_default (is_default)
);

-- Ajouter des commentaires à la table
ALTER TABLE bank_account
COMMENT='Table de gestion des comptes bancaires des utilisateurs. Les IBAN et BIC sont chiffrés en base.';

