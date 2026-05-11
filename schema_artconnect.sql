-- Schema SQL MySQL 8.x pour ArtConnect
-- Source: MLD_ArtConnect.md

CREATE DATABASE IF NOT EXISTS artconnect_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE artconnect_db;

CREATE TABLE IF NOT EXISTS discipline (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_discipline_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS artist (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    bio TEXT,
    birth_year INT,
    contact_email VARCHAR(255),
    phone VARCHAR(30),
    city VARCHAR(120),
    website VARCHAR(255),
    social_media VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_artist_contact_email (contact_email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS gallery (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255),
    owner_name VARCHAR(150),
    opening_hours VARCHAR(120),
    contact_phone VARCHAR(30),
    rating DECIMAL(2,1),
    website VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT chk_gallery_rating CHECK (rating IS NULL OR (rating >= 0.0 AND rating <= 5.0))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS community_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birth_year INT,
    phone VARCHAR(30),
    city VARCHAR(120),
    membership_type VARCHAR(80),
    PRIMARY KEY (id),
    UNIQUE KEY uq_community_member_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS artwork (
    id BIGINT NOT NULL AUTO_INCREMENT,
    artist_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    creation_year INT,
    type VARCHAR(100),
    medium VARCHAR(100),
    dimensions VARCHAR(100),
    description TEXT,
    price DECIMAL(15,2),
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_artwork_artist_id (artist_id),
    CONSTRAINT chk_artwork_status CHECK (status IN ('FOR_SALE', 'SOLD', 'EXHIBITED')),
    CONSTRAINT fk_artwork_artist FOREIGN KEY (artist_id)
        REFERENCES artist(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exhibition (
    id BIGINT NOT NULL AUTO_INCREMENT,
    gallery_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    description TEXT,
    curator_name VARCHAR(150),
    theme VARCHAR(150),
    PRIMARY KEY (id),
    KEY idx_exhibition_gallery_id (gallery_id),
    CONSTRAINT chk_exhibition_dates CHECK (end_date >= start_date),
    CONSTRAINT fk_exhibition_gallery FOREIGN KEY (gallery_id)
        REFERENCES gallery(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS workshop (
    id BIGINT NOT NULL AUTO_INCREMENT,
    instructor_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    max_participants INT NOT NULL,
    price DECIMAL(10,2),
    location VARCHAR(255),
    description TEXT,
    level VARCHAR(80),
    PRIMARY KEY (id),
    KEY idx_workshop_instructor_id (instructor_id),
    CONSTRAINT chk_workshop_duration CHECK (duration_minutes > 0),
    CONSTRAINT chk_workshop_max_participants CHECK (max_participants > 0),
    CONSTRAINT fk_workshop_instructor FOREIGN KEY (instructor_id)
        REFERENCES artist(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS artwork_tag (
    id BIGINT NOT NULL AUTO_INCREMENT,
    artwork_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_artwork_tag_artwork_name (artwork_id, name),
    KEY idx_artwork_tag_artwork_id (artwork_id),
    CONSTRAINT fk_artwork_tag_artwork FOREIGN KEY (artwork_id)
        REFERENCES artwork(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT NOT NULL AUTO_INCREMENT,
    workshop_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    booking_date TIMESTAMP NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_booking_workshop_id (workshop_id),
    KEY idx_booking_member_id (member_id),
    CONSTRAINT fk_booking_workshop FOREIGN KEY (workshop_id)
        REFERENCES workshop(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_booking_member FOREIGN KEY (member_id)
        REFERENCES community_member(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reviewer_id BIGINT NOT NULL,
    artwork_id BIGINT NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    review_date DATE NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_review_reviewer_artwork (reviewer_id, artwork_id),
    KEY idx_review_reviewer_id (reviewer_id),
    KEY idx_review_artwork_id (artwork_id),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES community_member(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_review_artwork FOREIGN KEY (artwork_id)
        REFERENCES artwork(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS artist_discipline (
    artist_id BIGINT NOT NULL,
    discipline_id BIGINT NOT NULL,
    PRIMARY KEY (artist_id, discipline_id),
    KEY idx_artist_discipline_discipline_id (discipline_id),
    CONSTRAINT fk_artist_discipline_artist FOREIGN KEY (artist_id)
        REFERENCES artist(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_artist_discipline_discipline FOREIGN KEY (discipline_id)
        REFERENCES discipline(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS exhibition_artwork (
    exhibition_id BIGINT NOT NULL,
    artwork_id BIGINT NOT NULL,
    PRIMARY KEY (exhibition_id, artwork_id),
    KEY idx_exhibition_artwork_artwork_id (artwork_id),
    CONSTRAINT fk_exhibition_artwork_exhibition FOREIGN KEY (exhibition_id)
        REFERENCES exhibition(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_exhibition_artwork_artwork FOREIGN KEY (artwork_id)
        REFERENCES artwork(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS member_favorite_discipline (
    member_id BIGINT NOT NULL,
    discipline_id BIGINT NOT NULL,
    PRIMARY KEY (member_id, discipline_id),
    KEY idx_member_favorite_discipline_discipline_id (discipline_id),
    CONSTRAINT fk_member_favorite_discipline_member FOREIGN KEY (member_id)
        REFERENCES community_member(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_member_favorite_discipline_discipline FOREIGN KEY (discipline_id)
        REFERENCES discipline(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

