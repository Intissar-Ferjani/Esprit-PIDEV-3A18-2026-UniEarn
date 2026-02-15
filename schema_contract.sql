-- Script SQL pour créer la table contract dans la base de données UniEarn
-- Exécutez ce script une seule fois pour initialiser la table

-- Vérifier si la table existe et la supprimer si nécessaire (optionnel)
-- DROP TABLE IF EXISTS contract;

-- Créer la table contract
CREATE TABLE IF NOT EXISTS contract (
    idContract INT PRIMARY KEY AUTO_INCREMENT,
    startDate TIMESTAMP NOT NULL,
    endDate TIMESTAMP NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0: Brouillon, 1: Signé Client, 2: Signé Freelancer, 3: Complété',
    amount DOUBLE NOT NULL,
    projectID INT NOT NULL,
    clientID INT NOT NULL,
    paymentID INT,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Clés étrangères
    CONSTRAINT fk_contract_project FOREIGN KEY (projectID)
        REFERENCES project(idProject) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_contract_client FOREIGN KEY (clientID)
        REFERENCES client(idClient) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_contract_payment FOREIGN KEY (paymentID)
        REFERENCES payment(idPayment) ON DELETE SET NULL ON UPDATE CASCADE,

    -- Index pour les requêtes fréquentes
    INDEX idx_clientID (clientID),
    INDEX idx_projectID (projectID),
    INDEX idx_status (status),
    INDEX idx_dates (startDate, endDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insérer des données de test (optionnel)
-- INSERT INTO contract (startDate, endDate, status, amount, projectID, clientID, paymentID)
-- VALUES
-- ('2024-02-01 00:00:00', '2024-03-01 00:00:00', 0, 50000, 1, 1, 1),
-- ('2024-02-15 00:00:00', '2024-04-15 00:00:00', 1, 75000, 2, 2, 2),
-- ('2024-01-01 00:00:00', '2024-02-28 00:00:00', 3, 100000, 1, 1, 1);

-- Afficher la structure de la table
-- DESCRIBE contract;

-- Vérifier les contrats existants
-- SELECT * FROM contract;

