-- ======================================================================
-- SCRIPT DE MIGRATION: Upgrade de la table contract
-- Exécutez ce script pour ajouter les nouvelles colonnes
-- ======================================================================

-- 1. Ajouter les colonnes manquantes à la table contract
ALTER TABLE contract ADD COLUMN Type VARCHAR(100) NOT NULL DEFAULT 'Standard' AFTER idContract;
ALTER TABLE contract ADD COLUMN freelancerID INT AFTER clientID;
ALTER TABLE contract ADD COLUMN clientSignatureDate DATETIME AFTER freelancerID;
ALTER TABLE contract ADD COLUMN freelancerSignatureDate DATETIME AFTER clientSignatureDate;

-- 2. Ajouter les contraintes de clés étrangères pour freelancerID
ALTER TABLE contract ADD CONSTRAINT fk_contract_freelancer
    FOREIGN KEY (freelancerID) REFERENCES user(idUser) ON DELETE CASCADE ON UPDATE CASCADE;

-- 3. Créer la table contract_template pour les templates
CREATE TABLE IF NOT EXISTS contract_template (
    idTemplate INT PRIMARY KEY AUTO_INCREMENT,
    templateName VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    templateContent LONGTEXT NOT NULL,
    createdDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Ajouter une colonne templateID à la table contract (optionnel)
ALTER TABLE contract ADD COLUMN templateID INT AFTER Type;
ALTER TABLE contract ADD CONSTRAINT fk_contract_template
    FOREIGN KEY (templateID) REFERENCES contract_template(idTemplate) ON DELETE SET NULL ON UPDATE CASCADE;

-- 5. Créer les templates par défaut
INSERT INTO contract_template (templateName, description, templateContent) VALUES
('Template Standard', 'Template standard pour contrats génériques',
'CONTRAT DE PRESTATIONS DE SERVICES\n\nEntre:\n- CLIENT: [Client Name]\n- FREELANCER: [Freelancer Name]\n\nLe présent contrat définit les termes et conditions de la prestation de services.\n\nDATES:\n- Date de début: [StartDate]\n- Date de fin: [EndDate]\n\nMONTANT: [Amount] DA\n\nCONDITIONS:\n1. Le freelancer s\'engage à fournir les services convenus\n2. Le client s\'engage à verser le montant convenu\n3. Les deux parties acceptent les termes du contrat\n\nSignatures:\n- Client: _________________ Date: _______\n- Freelancer: _________________ Date: _______'),

('Template Développement Web', 'Template pour projets de développement web',
'CONTRAT DE DÉVELOPPEMENT WEB\n\nEntre:\n- CLIENT: [Client Name]\n- FREELANCER: [Freelancer Name]\n\nLe freelancer s\'engage à développer/modifier le site web selon les spécifications.\n\nDATES:\n- Date de début: [StartDate]\n- Date de fin: [EndDate]\n\nMONTANT: [Amount] DA\n\nLIVRABLES:\n- Code source\n- Documentation\n- Support de 3 mois\n\nPAIEMENT:\n- 50% à la signature\n- 50% à la livraison\n\nSignatures:\n- Client: _________________ Date: _______\n- Freelancer: _________________ Date: _______'),

('Template Design Graphique', 'Template pour projets de design',
'CONTRAT DE SERVICES DE DESIGN GRAPHIQUE\n\nEntre:\n- CLIENT: [Client Name]\n- FREELANCER: [Freelancer Name]\n\nLe freelancer s\'engage à fournir des services de design graphique.\n\nDATES:\n- Date de début: [StartDate]\n- Date de fin: [EndDate]\n\nMONTANT: [Amount] DA\n\nLIVRABLES:\n- 3 propositions de design\n- 2 tours de révisions\n- Fichiers finaux en haute résolution\n\nDROITS D\'AUTEUR:\nTous les droits des œuvres finales reviennent au client.\n\nSignatures:\n- Client: _________________ Date: _______\n- Freelancer: _________________ Date: _______');

-- 6. Index pour optimiser les recherches
ALTER TABLE contract ADD INDEX idx_type (Type);
ALTER TABLE contract ADD INDEX idx_freelancerID (freelancerID);
ALTER TABLE contract ADD INDEX idx_signatures (clientSignatureDate, freelancerSignatureDate);
ALTER TABLE contract_template ADD INDEX idx_templateName (templateName);

-- 7. Vérifier la structure
DESCRIBE contract;
DESCRIBE contract_template;

-- ======================================================================
-- FIN DE LA MIGRATION
-- ======================================================================

