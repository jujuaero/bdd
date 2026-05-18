-- ArtConnect Pro - advanced database features
-- Run after schema_artconnect.sql

USE artconnect_db;

-- Journal des alertes métiers / triggers
CREATE TABLE IF NOT EXISTS alert_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(500) NOT NULL,
    source_table VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

DROP TRIGGER IF EXISTS trg_exhibition_after_insert;
DROP TRIGGER IF EXISTS trg_exhibition_after_update;
DROP TRIGGER IF EXISTS trg_gallery_rating_alert;
DROP TRIGGER IF EXISTS trg_booking_capacity_check;
DROP TRIGGER IF EXISTS trg_exhibition_dates_check;
DROP TRIGGER IF EXISTS trg_exhibition_dates_check_update;

-- ---------------------------------------------------------
-- 0) TRIGGERS
-- ---------------------------------------------------------
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
CREATE TRIGGER trg_exhibition_dates_check_update
BEFORE UPDATE ON exhibition
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
CREATE TRIGGER trg_exhibition_after_update
AFTER UPDATE ON exhibition
FOR EACH ROW
BEGIN
    IF OLD.title <> NEW.title
       OR OLD.start_date <> NEW.start_date
       OR OLD.end_date <> NEW.end_date
       OR IFNULL(OLD.description, '') <> IFNULL(NEW.description, '')
       OR IFNULL(OLD.curator_name, '') <> IFNULL(NEW.curator_name, '')
       OR IFNULL(OLD.theme, '') <> IFNULL(NEW.theme, '')
       OR OLD.gallery_id <> NEW.gallery_id THEN
        INSERT INTO alert_log (message, source_table)
        VALUES (CONCAT('Exposition modifiée : ', NEW.title), 'exhibition');
    END IF;
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

-- ---------------------------------------------------------
-- 1) VUES
-- ---------------------------------------------------------
-- Vue de simplification de requête pour les expositions
CREATE OR REPLACE VIEW vw_exhibition_summary AS
SELECT
    e.id AS exhibition_id,
    e.title AS exhibition_title,
    e.start_date,
    e.end_date,
    e.curator_name,
    e.theme,
    g.id AS gallery_id,
    g.name AS gallery_name,
    g.address AS gallery_address
FROM exhibition e
JOIN gallery g ON g.id = e.gallery_id;

-- Vue publique / masquage d’attributs sensibles pour le catalogue d’œuvres
CREATE OR REPLACE VIEW vw_public_artwork_catalog AS
SELECT
    a.id AS artwork_id,
    a.title,
    a.type,
    a.medium,
    a.dimensions,
    a.price,
    a.status,
    ar.id AS artist_id,
    ar.name AS artist_name,
    ar.city AS artist_city
FROM artwork a
JOIN artist ar ON ar.id = a.artist_id;

-- Vue de sécurité : profil membre public, sans email ni téléphone
CREATE OR REPLACE VIEW vw_public_member_profile AS
SELECT
    id AS member_id,
    name,
    city,
    membership_type
FROM community_member;

-- Vue d’agrégation utile pour le suivi des ateliers
CREATE OR REPLACE VIEW vw_workshop_participant_count AS
SELECT
    w.id AS workshop_id,
    w.title,
    w.date_time,
    w.max_participants,
    COUNT(b.id) AS participant_count,
    (w.max_participants - COUNT(b.id)) AS remaining_places
FROM workshop w
LEFT JOIN booking b ON b.workshop_id = w.id
GROUP BY w.id, w.title, w.date_time, w.max_participants;

-- ---------------------------------------------------------
-- 2) INDEX
-- ---------------------------------------------------------
-- Optimise les recherches fréquentes par artiste, date, statut et galerie
CREATE INDEX idx_artwork_artist_status ON artwork(artist_id, status);
CREATE INDEX idx_workshop_date ON workshop(date_time);
CREATE INDEX idx_booking_workshop_member ON booking(workshop_id, member_id);
CREATE INDEX idx_booking_member_date ON booking(member_id, booking_date);
CREATE INDEX idx_exhibition_gallery_dates ON exhibition(gallery_id, start_date, end_date);
CREATE INDEX idx_gallery_rating ON gallery(rating);
CREATE INDEX idx_review_artwork_date ON review(artwork_id, review_date);
CREATE INDEX idx_artist_city ON artist(city);

-- ---------------------------------------------------------
-- 3) PROCEDURES STOCKEES
-- ---------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_create_exhibition(
    IN p_gallery_id BIGINT,
    IN p_title VARCHAR(200),
    IN p_start_date DATE,
    IN p_end_date DATE,
    IN p_description TEXT,
    IN p_curator_name VARCHAR(150),
    IN p_theme VARCHAR(150)
)
BEGIN
    INSERT INTO exhibition (gallery_id, title, start_date, end_date, description, curator_name, theme)
    VALUES (p_gallery_id, p_title, p_start_date, p_end_date, p_description, p_curator_name, p_theme);
END//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_book_workshop(
    IN p_workshop_id BIGINT,
    IN p_member_id BIGINT,
    IN p_payment_status VARCHAR(50)
)
BEGIN
    INSERT INTO booking (workshop_id, member_id, booking_date, payment_status)
    VALUES (p_workshop_id, p_member_id, CURRENT_TIMESTAMP, p_payment_status);
END//
DELIMITER ;

DELIMITER //
CREATE PROCEDURE sp_add_artwork_to_exhibition(
    IN p_exhibition_id BIGINT,
    IN p_artwork_id BIGINT
)
BEGIN
    INSERT INTO exhibition_artwork (exhibition_id, artwork_id)
    VALUES (p_exhibition_id, p_artwork_id);
END//
DELIMITER ;

-- ---------------------------------------------------------
-- 4) FONCTIONS STOCKEES
-- ---------------------------------------------------------
DELIMITER //
CREATE FUNCTION fn_workshop_participant_count(p_workshop_id BIGINT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt
    FROM booking
    WHERE workshop_id = p_workshop_id;
    RETURN cnt;
END//
DELIMITER ;

DELIMITER //
CREATE FUNCTION fn_gallery_exhibition_count(p_gallery_id BIGINT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE cnt INT;
    SELECT COUNT(*) INTO cnt
    FROM exhibition
    WHERE gallery_id = p_gallery_id;
    RETURN cnt;
END//
DELIMITER ;

DELIMITER //
CREATE FUNCTION fn_remaining_workshop_places(p_workshop_id BIGINT)
RETURNS INT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE max_cap INT;
    DECLARE booked INT;
    SELECT max_participants INTO max_cap FROM workshop WHERE id = p_workshop_id;
    SELECT COUNT(*) INTO booked FROM booking WHERE workshop_id = p_workshop_id;
    RETURN max_cap - booked;
END//
DELIMITER ;

-- ---------------------------------------------------------
-- 5) Exemples d’utilisation (à exécuter manuellement si besoin)
-- ---------------------------------------------------------
-- CALL sp_create_exhibition(1, 'Nouvelle exposition', '2026-06-01', '2026-08-01', 'Description...', 'Curator', 'Theme');
-- CALL sp_book_workshop(1, 2, 'PENDING');
-- SELECT fn_workshop_participant_count(1);
-- SELECT fn_remaining_workshop_places(1);

