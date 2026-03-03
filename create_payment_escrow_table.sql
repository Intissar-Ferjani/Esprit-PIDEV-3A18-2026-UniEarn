-- Script de création de la table payment_escrow pour la gestion des paiements en escrow
-- Les montants sont bloqués chez l'admin jusqu'à la validation de la livraison

CREATE TABLE IF NOT EXISTS payment_escrow (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contract_id INT NOT NULL,
    client_id INT NOT NULL,
    freelancer_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, RELEASED, REFUNDED',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_completion TIMESTAMP NULL COMMENT 'Quand le projet est livré',
    date_liberation TIMESTAMP NULL COMMENT 'Quand l''admin libère le montant',
    notes TEXT NULL COMMENT 'Notes admin sur la validation',

    -- Contraintes de clés étrangères
    CONSTRAINT fk_escrow_contract FOREIGN KEY (contract_id)
        REFERENCES contract(idContract) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_escrow_client FOREIGN KEY (client_id)
        REFERENCES client(idClient) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_escrow_freelancer FOREIGN KEY (freelancer_id)
        REFERENCES freelancer(idFreelancer) ON DELETE CASCADE ON UPDATE CASCADE,

    -- Index pour les recherches
    INDEX idx_contract_id (contract_id),
    INDEX idx_client_id (client_id),
    INDEX idx_freelancer_id (freelancer_id),
    INDEX idx_status (status),
    INDEX idx_date_creation (date_creation)
);

-- Ajouter des commentaires à la table
ALTER TABLE payment_escrow
COMMENT='Table de gestion des paiements en escrow. Les montants sont bloqués jusqu''à la validation de la livraison par l''admin.';

-- Créer des vues utiles pour les statistiques
CREATE OR REPLACE VIEW v_escrow_statistics AS
SELECT
    'PENDING' as status,
    COUNT(*) as total_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount
FROM payment_escrow
WHERE status IN ('PENDING', 'COMPLETED')
UNION ALL
SELECT
    'RELEASED',
    COUNT(*),
    SUM(amount),
    AVG(amount)
FROM payment_escrow
WHERE status = 'RELEASED'
UNION ALL
SELECT
    'REFUNDED',
    COUNT(*),
    SUM(amount),
    AVG(amount)
FROM payment_escrow
WHERE status = 'REFUNDED';

-- Vue pour les paiements en attente par freelancer
CREATE OR REPLACE VIEW v_freelancer_pending_payments AS
SELECT
    pe.id,
    pe.contract_id,
    pe.freelancer_id,
    pe.amount,
    pe.status,
    pe.date_creation,
    uf.name as freelancer_name,
    uc.name as client_name
FROM payment_escrow pe
JOIN freelancer f ON pe.freelancer_id = f.idFreelancer
JOIN user uf ON f.idUser = uf.idUser
JOIN client c ON pe.client_id = c.idClient
JOIN user uc ON c.userID = uc.idUser
WHERE pe.status IN ('COMPLETED', 'RELEASED')
ORDER BY pe.date_creation DESC;

-- Vue pour les paiements par client
CREATE OR REPLACE VIEW v_client_payments AS
SELECT
    pe.id,
    pe.contract_id,
    pe.amount,
    pe.status,
    pe.date_creation,
    pe.date_liberation,
    uf.name as freelancer_name
FROM payment_escrow pe
JOIN freelancer f ON pe.freelancer_id = f.idFreelancer
JOIN user uf ON f.idUser = uf.idUser
ORDER BY pe.date_creation DESC;

