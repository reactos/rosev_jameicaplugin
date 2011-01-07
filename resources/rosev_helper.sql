--
-- Database "rosev_helper"
-- Import this on the command-line, phpMyAdmin will fail!
--

-- Table "additional_donations"
CREATE TABLE `additional_donations` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `name` varchar(100) NOT NULL,
  `anonymous` char(1) NOT NULL,
  `amount` double NOT NULL,
  `currency` char(3) NOT NULL,
  `comment` varchar(1000) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- Table "version"
CREATE TABLE `version` (
  `version` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `version` VALUES (1);


-- Stored function "get_jverein_amount"
DELIMITER ;;
CREATE FUNCTION `get_jverein_amount`(art VARCHAR(100)) RETURNS double DETERMINISTIC RETURN art ;;
DELIMITER ;


-- Stored function "get_jverein_anonymous"
DELIMITER ;;
CREATE FUNCTION `get_jverein_anonymous`(comment VARCHAR(1000)) RETURNS char(1) CHARSET latin1 DETERMINISTIC RETURN LOCATE('Anonym', comment) > 0 ;;
DELIMITER ;


-- Stored function "get_jverein_currency"
DELIMITER ;;
CREATE FUNCTION `get_jverein_currency`(art VARCHAR(100)) RETURNS char(3) CHARSET latin1 DETERMINISTIC
BEGIN
    DECLARE b INT;

		SET b = LOCATE(' ', art);
		IF b = 0 THEN
				RETURN '';
		END IF;

		RETURN SUBSTR(art, b+1);
END ;;
DELIMITER ;


-- View "all_donations"
CREATE VIEW `all_donations` AS
	select
		`rosev_helper`.`additional_donations`.`id` AS `id`,
		0 AS `jverein_id`,
		`rosev_helper`.`additional_donations`.`date` AS `date`,
		`rosev_helper`.`additional_donations`.`name` AS `name`,
		`rosev_helper`.`additional_donations`.`anonymous` AS `anonymous`,
		`rosev_helper`.`additional_donations`.`amount` AS `amount`,
		`rosev_helper`.`additional_donations`.`currency` AS `currency`,
		`rosev_helper`.`additional_donations`.`comment` AS `comment`
	from `rosev_helper`.`additional_donations`
	union select
		(`rosev_jverein`.`buchung`.`id` + 10000) AS `id`,
		`rosev_jverein`.`buchung`.`id` AS `jverein_id`,
		`rosev_jverein`.`buchung`.`datum` AS `date`,
		`rosev_jverein`.`buchung`.`name` AS `name`,
		`get_jverein_anonymous`(`rosev_jverein`.`buchung`.`kommentar`) AS `anonymous`,
		`get_jverein_amount`(`rosev_jverein`.`buchung`.`art`) AS `amount`,
		`get_jverein_currency`(`rosev_jverein`.`buchung`.`art`) AS `currency`,
		`rosev_jverein`.`buchung`.`kommentar` AS `comment`
	from `rosev_jverein`.`buchung`
	where (`rosev_jverein`.`buchung`.`buchungsart` in (1,2));
