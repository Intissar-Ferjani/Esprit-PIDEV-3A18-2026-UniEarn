-- Run this ONCE in phpMyAdmin (SQL tab, database uniearn_db) to create the contract tables
-- Required by ContractTemplateService and ContractTypeService

USE uniearn_db;

-- ═══ contract_template ═══════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS contract_template (
    idTemplate      INT AUTO_INCREMENT PRIMARY KEY,
    templateName    VARCHAR(255) NOT NULL,
    description     TEXT,
    templateContent TEXT,
    createdDate     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedDate     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══ contract_type ═══════════════════════════════════════════════════════
CREATE TABLE IF NOT EXISTS contract_type (
    idContractType  INT AUTO_INCREMENT PRIMARY KEY,
    typeName        VARCHAR(255) NOT NULL,
    description     TEXT,
    metier          VARCHAR(255) NOT NULL,
    createdAt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ═══ Seed: default métiers + contract types ══════════════════════════════
INSERT INTO contract_type (typeName, description, metier) VALUES
-- Développement
('Développement Web',        'Création de sites et applications web',              'Développement'),
('Développement Mobile',     'Création d''applications mobiles iOS/Android',       'Développement'),
('Développement Backend',    'Développement de serveurs et APIs',                  'Développement'),
('Développement Frontend',   'Interfaces utilisateur et intégration',              'Développement'),

-- Design
('Design Graphique',         'Création de visuels, logos et identité visuelle',    'Design'),
('Design UI/UX',             'Conception d''interfaces utilisateur',               'Design'),
('Design Web',               'Maquettes et prototypes de sites web',              'Design'),

-- Rédaction
('Rédaction Web',            'Rédaction de contenu pour sites web',               'Rédaction'),
('Rédaction Technique',      'Documentation technique et manuels',                'Rédaction'),
('Traduction',               'Traduction de documents et contenus',               'Rédaction'),

-- Marketing
('Marketing Digital',        'Stratégie marketing en ligne',                       'Marketing'),
('SEO',                      'Optimisation pour les moteurs de recherche',         'Marketing'),
('Community Management',     'Gestion des réseaux sociaux',                        'Marketing'),

-- Consulting
('Consulting IT',            'Conseil en informatique et systèmes',                'Consulting'),
('Consulting Business',      'Conseil en stratégie d''entreprise',                 'Consulting'),

-- Data
('Data Analysis',            'Analyse de données et reporting',                    'Data'),
('Data Science',             'Machine learning et modèles prédictifs',             'Data');
