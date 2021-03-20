-- Info: File contains changes by Philipp Eichinger (@peichinger)

CREATE DATABASE IF NOT EXISTS `mail`;
CREATE USER 'mailserver'@'localhost' IDENTIFIED BY 'S6TTAykTfAEMJjqN'; -- PE: Change PW in the productive environment
USE `mail`;
GRANT ALL ON `mail`.* TO 'mailserver'@'localhost';
SET @@global.time_zone = 'SYSTEM';