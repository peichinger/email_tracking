-- Info: File contains changes by Philipp Eichinger (@peichinger)

--
-- Table structure for table `users`
--
CREATE TABLE IF NOT EXISTS `users` (
	`id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'unique auto-incrementing ID',
	`email` VARCHAR(254) NOT NULL UNIQUE COMMENT 'unique email address',
	`email2` VARCHAR(254) NOT NULL UNIQUE COMMENT 'unique secondary email address',
	`register_site` VARCHAR(255) NOT NULL COMMENT 'registration site name',
	`register_url` VARCHAR(2048) NOT NULL COMMENT 'registration URL',
	`register_domain` VARCHAR(255) NOT NULL COMMENT 'domain of the registration URL',
	`register_category` VARCHAR(254) NOT NULL COMMENT 'category of the registered site',
	`register_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'registration time',
	`emails_received` INT(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'number of emails received',
	`emails_received2` INT(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'number of secondary emails received',
	`leak_count` INT(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'number of times the users email address was leaked',
	`tp_leak_count` INT(11) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'number of times the users email address was leaked to a third party',
	PRIMARY KEY (`id`)
) ENGINE=InnoDB; -- MyISAM;

--
-- Table structure for table `inbox`
--
CREATE TABLE IF NOT EXISTS `inbox` (
	`recipient` VARCHAR(254) COMMENT 'email recipient',
	`sender` VARCHAR(254) NOT NULL COMMENT 'email sender',
	`sent_date` DATETIME COMMENT 'email sent date',
	`subject` TEXT COMMENT 'email subject',
	`filename` VARCHAR(255) NOT NULL COMMENT 'file location',
	`format` VARCHAR(254) COMMENT 'email format (e.g. html, other)',
	`comment` VARCHAR(254) COMMENT 'field for any comments',
	PRIMARY KEY (`recipient`, `filename`)
) ENGINE=InnoDB; -- MyISAM;

--
-- Table structure for table `inbox2` (stores information about emails sent to secondary email addresses)
--
CREATE TABLE IF NOT EXISTS `inbox2` (
	`recipient` VARCHAR(254) COMMENT 'email recipient',
	`sender` VARCHAR(254) NOT NULL COMMENT 'email sender',
	`sent_date` DATETIME COMMENT 'email sent date',
	`subject` TEXT COMMENT 'email subject',
	`filename` VARCHAR(255) NOT NULL COMMENT 'file location',
	`format` VARCHAR(254) COMMENT 'email format (e.g. html, other)',
	`comment` VARCHAR(254) COMMENT 'field for any comments',
	PRIMARY KEY (`recipient`, `filename`)
) ENGINE=InnoDB;

--
-- Table structure for table `leaked_emails`
--
CREATE TABLE IF NOT EXISTS `leaked_emails` (
	`sender_domain` VARCHAR(255) COMMENT 'sender domain',
	`sender_address` VARCHAR(254) COMMENT 'sender email',
	`recipient_id` INT(11) COMMENT 'recipient user id',
	`encoding` VARCHAR(254) COMMENT 'encoding name (e.g. md5, sha1, crc32, mmh1)',
	`url` VARCHAR(2048) COMMENT 'link url',
	`url_domain` VARCHAR(255) COMMENT 'link url domain',
	`organization` VARCHAR(254) COMMENT 'organisation for link url domain',
	`type` VARCHAR(254) COMMENT 'link type',
	`is_redirect` BOOLEAN COMMENT 'redirection info',
	`is_intentional` BOOLEAN COMMENT 'email leakage intention',
	PRIMARY KEY (`recipient_id`, `url`)
) ENGINE=InnoDB;