-- Run this ONCE in phpMyAdmin (SQL tab, database uniearn_db) to add DISLIKE support.
-- This modifies the reaction_type column to accept both LIKE and DISLIKE values.

USE uniearn_db;

-- If the column is an ENUM, alter it to include DISLIKE:
ALTER TABLE freelancer_forum_reaction
  MODIFY reaction_type ENUM('LIKE', 'DISLIKE') NOT NULL DEFAULT 'LIKE';

-- If the above fails because the column is VARCHAR, try this instead:
-- ALTER TABLE freelancer_forum_reaction
--   MODIFY reaction_type VARCHAR(20) NOT NULL DEFAULT 'LIKE';
