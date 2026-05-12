-- Fix: Rendre freelancer_id nullable dans payment_escrow
-- Cela permet de créer des escrows même si le freelancer_id est NULL ou invalide

ALTER TABLE payment_escrow
MODIFY freelancer_id INT NULL,
DROP FOREIGN KEY fk_escrow_freelancer;

-- Ajouter à nouveau la contrainte en autorisant NULL
ALTER TABLE payment_escrow
ADD CONSTRAINT fk_escrow_freelancer FOREIGN KEY (freelancer_id)
REFERENCES freelancer(idFreelancer) ON DELETE SET NULL ON UPDATE CASCADE;

