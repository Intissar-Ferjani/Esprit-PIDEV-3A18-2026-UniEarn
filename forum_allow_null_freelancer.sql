-- Run this once in phpMyAdmin (or MySQL) so comments and reactions work without a freelancer row.
-- This allows freelancer_id to be NULL when user/login is not yet integrated.

USE uniearn_db;

ALTER TABLE freelancer_forum_comment
  MODIFY freelancer_id int(11) NULL;

ALTER TABLE freelancer_forum_reaction
  MODIFY freelancer_id int(11) NULL;
