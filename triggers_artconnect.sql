-- ArtConnect Pro — triggers & alert_log
-- Run after schema_artconnect.sql:
--   mysql -u <user> -p artconnect_db < triggers_artconnect.sql

USE artconnect_db;

CREATE TABLE IF NOT EXISTS alert_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(500) NOT NULL,
    source_table VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TRIGGER IF EXISTS trg_exhibition_after_insert;
DROP TRIGGER IF EXISTS trg_gallery_rating_alert;
DROP TRIGGER IF EXISTS trg_booking_capacity_check;
DROP TRIGGER IF EXISTS trg_exhibition_dates_check;

-- Block invalid date ranges before insert/update (popup via SQLSTATE 45000)
DELIMITER //
CREATE TRIGGER trg_exhibition_dates_check
BEFORE INSERT ON exhibition
FOR EACH ROW
BEGIN
    IF NEW.end_date < NEW.start_date THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'La date de fin doit être postérieure ou égale à la date de début.';
    END IF;
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_exhibition_after_insert
AFTER INSERT ON exhibition
FOR EACH ROW
BEGIN
    INSERT INTO alert_log (message, source_table)
    VALUES (CONCAT('Nouvelle exposition créée : ', NEW.title), 'exhibition');
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_gallery_rating_alert
AFTER UPDATE ON gallery
FOR EACH ROW
BEGIN
    IF NEW.rating IS NOT NULL AND NEW.rating >= 4.5
       AND (OLD.rating IS NULL OR OLD.rating < 4.5) THEN
        INSERT INTO alert_log (message, source_table)
        VALUES (CONCAT('Galerie très bien notée (', NEW.rating, '/5) : ', NEW.name), 'gallery');
    END IF;
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trg_booking_capacity_check
BEFORE INSERT ON booking
FOR EACH ROW
BEGIN
    DECLARE current_count INT;
    DECLARE max_cap INT;
    SELECT COUNT(*) INTO current_count FROM booking WHERE workshop_id = NEW.workshop_id;
    SELECT max_participants INTO max_cap FROM workshop WHERE id = NEW.workshop_id;
    IF current_count >= max_cap THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'L''atelier est complet : aucune réservation supplémentaire possible.';
    END IF;
END//
DELIMITER ;
