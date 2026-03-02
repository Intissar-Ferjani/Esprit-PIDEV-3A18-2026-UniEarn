-- ============================================================
-- Migration : Création des tables bank_account et payment_escrow
-- UniEarn - Système de gestion des paiements en escrow
-- ============================================================

-- Table des comptes bancaires
CREATE TABLE IF NOT EXISTS bank_account (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT NOT NULL,
    account_holder_name VARCHAR(255) NOT NULL,
    iban                VARCHAR(34) NOT NULL,
    bic                 VARCHAR(11),
    bank_name           VARCHAR(100) NOT NULL,
    is_default          TINYINT(1) NOT NULL DEFAULT 0,
    date_added          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bank_account_user
        FOREIGN KEY (user_id) REFERENCES user(idUser)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Table des paiements en escrow
CREATE TABLE IF NOT EXISTS payment_escrow (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    contract_id    INT NOT NULL,
    client_id      INT NOT NULL,
    freelancer_id  INT NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_escrow_contract
        FOREIGN KEY (contract_id) REFERENCES contract(idContract)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_escrow_client
        FOREIGN KEY (client_id) REFERENCES user(idUser)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_escrow_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES user(idUser)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Correction du type de paymentStatus dans la table payment (INT -> VARCHAR)
-- Exécuter uniquement si la colonne est encore de type INT
-- ALTER TABLE payment MODIFY COLUMN paymentStatus VARCHAR(20) NOT NULL DEFAULT 'PENDING';
