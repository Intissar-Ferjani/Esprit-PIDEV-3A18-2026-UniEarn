-- Run this ONCE in phpMyAdmin (SQL tab, database uniearn_db) to create the default freelancer
-- and all entities he depends on. The forum will then use idFreelancer=1 for comments and reactions.

USE uniearn_db;

SET FOREIGN_KEY_CHECKS = 0;

-- 1) Default user (idUser=1) for the default freelancer
INSERT IGNORE INTO `user` (`idUser`, `name`, `email`, `password`, `role`)
VALUES (1, 'Forum User', 'forum@uniearn.local', 'default', 'FREELANCER');

-- 2) Default client (needed for project)
INSERT IGNORE INTO `client` (`idClient`, `amount`, `rating`, `userID`)
VALUES (1, 0, 0, 1);

-- 3) Default project (needed for task)
INSERT IGNORE INTO `project` (`idProject`, `title`, `description`, `budget`, `status`, `ClientID`)
VALUES (1, 'Default Project', 'Default', 0, 0, 1);

-- 4) Default task (needed for freelancer)
INSERT IGNORE INTO `task` (`idTask`, `title`, `description`, `deadline`, `TaskStatus`, `dateAssign`, `role`, `priority`, `idProject`)
VALUES (1, 0, 0, CURRENT_TIMESTAMP, 'Todo', CURRENT_TIMESTAMP, 'default', 0, 1);

-- 5) Application table if missing (freelancer.idApplication references it in some setups)
CREATE TABLE IF NOT EXISTS `application` (
  `idApplication` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`idApplication`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
INSERT IGNORE INTO `application` (`idApplication`) VALUES (1);

-- 6) Default portfolio (idPortfolio=1) – insert first with freelancerId=1; freelancer inserted next (FK checks are off)
INSERT IGNORE INTO `portfolio` (`idPortfolio`, `title`, `description`, `created_At`, `freelancerId`)
VALUES (1, 'Default Portfolio', 'Default', CURRENT_TIMESTAMP, 1);

-- 7) Default freelancer (idFreelancer=1) – references portfolio 1 and task 1
INSERT IGNORE INTO `freelancer` (
  `idFreelancer`, `pricePerHour`, `amount`, `rating`, `skills`,
  `verificationStatus`, `status`, `idUser`, `idApplication`, `idPortfolio`, `idTask`
) VALUES (
  1, 0, 0, 0, '[]',
  'unverified', 'available', 1, 1, 1, 1
);

SET FOREIGN_KEY_CHECKS = 1;
