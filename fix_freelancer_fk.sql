-- Fix the foreign key constraint for freelancerID in contract table
-- The freelancerID should reference user(idUser) instead of freelancer(idFreelancer)

-- First, drop the existing foreign key constraint
ALTER TABLE contract
DROP FOREIGN KEY fk_contract_freelancer;

-- Then add the corrected constraint that references user(idUser)
ALTER TABLE contract
ADD CONSTRAINT fk_contract_freelancer
FOREIGN KEY (freelancerID) REFERENCES user(idUser)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- Verify the constraint was added correctly
SELECT CONSTRAINT_NAME, COLUMN_NAME, REFERENCED_TABLE_NAME, REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'contract' AND COLUMN_NAME = 'freelancerID';

