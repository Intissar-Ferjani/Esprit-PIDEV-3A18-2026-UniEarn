-- Run this ONCE in phpMyAdmin (SQL tab, database uniearn_db) to add
-- GIF support and category filtering to forum posts.

USE uniearn_db;

-- 1) Column for storing an optional GIF URL attached to a post
ALTER TABLE freelancer_forum_post
  ADD COLUMN gif_url VARCHAR(500) NULL AFTER content;

-- 2) Column for the auto-detected category of a post
ALTER TABLE freelancer_forum_post
  ADD COLUMN category VARCHAR(50) DEFAULT 'General' AFTER gif_url;
