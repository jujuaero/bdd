-- Script de données de test pour ArtConnect
-- Compatible MySQL 8.x | UTF8MB4

USE artconnect_db;

-- ---------------------------------------------------------
-- 0. RÉINITIALISATION DE LA BASE
-- ---------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE member_favorite_discipline;
TRUNCATE TABLE review;
TRUNCATE TABLE booking;
TRUNCATE TABLE artwork_tag;
TRUNCATE TABLE exhibition_artwork;
TRUNCATE TABLE artist_discipline;
TRUNCATE TABLE workshop;
TRUNCATE TABLE exhibition;
TRUNCATE TABLE artwork;
TRUNCATE TABLE community_member;
TRUNCATE TABLE gallery;
TRUNCATE TABLE artist;
TRUNCATE TABLE discipline;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------
-- 1. DISCIPLINES
-- ---------------------------------------------------------
INSERT INTO discipline (name) VALUES 
('Peinture'), ('Sculpture'), ('Street Art'), ('Photographie'), 
('Art Numérique'), ('Céramique'), ('Installation'), ('Dessin');

-- ---------------------------------------------------------
-- 2. ARTISTES (Célèbres et Contemporains)
-- ---------------------------------------------------------
INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES
('Leonardo da Vinci', 'Génie de la Renaissance, peintre et inventeur.', 1452, 'leo@renaissance.it', '+39 055 123 4567', 'Vinci', 'https://example.com/leonardo', '@leonardo_renaissance', FALSE),
('Vincent van Gogh', 'Peintre post-impressionniste célèbre pour ses tournesols.', 1853, 'vincent@arles.fr', '+31 20 555 0101', 'Auvers-sur-Oise', 'https://example.com/vangogh', '@vangogh_museum', FALSE),
('Banksy', 'Artiste de rue anonyme basé en Angleterre.', 1974, 'pestcontrol@banksy.co.uk', '+44 20 7946 0958', 'Bristol', 'https://example.com/banksy', '@banksy', TRUE),
('Elena Martinez', 'Spécialiste du surréalisme numérique et de la photo.', 1992, 'elena.mtz@art.com', '+34 93 555 2048', 'Barcelona', 'https://example.com/elena-martinez', '@elena_martinez_art', TRUE),
('Marcus Thorne', 'Sculpteur de métaux recyclés et installations urbaines.', 1985, 'm.thorne@metal.uk', '+44 20 7111 3344', 'London', 'https://example.com/marcus-thorne', '@marcus_thorne_studio', TRUE),
('Yuki Tanaka', 'Exploratrice de la céramique traditionnelle et moderne.', 1990, 'yuki@kyoto.jp', '+81 75 555 7788', 'Kyoto', 'https://example.com/yuki-tanaka', '@yuki_tanaka_ceramics', TRUE);

-- ---------------------------------------------------------
-- 3. GALLERIES
-- ---------------------------------------------------------
INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES
('Musée du Louvre', 'Rue de Rivoli, Paris', 'Laurence des Cars', '09:00-18:00', '+33 1 40 20 50 50', 4.9, 'https://example.com/louvre'),
('Tate Modern', 'Bankside, London', 'Maria Balshaw', '10:00-18:00', '+44 20 7887 8888', 4.8, 'https://example.com/tate-modern'),
('Urban Pulse Gallery', 'Shoreditch, London', 'Simon Grey', '11:00-20:00', '+44 20 7010 4000', 4.5, 'https://example.com/urban-pulse-gallery'),
('Horizon Numérique', 'Sentier, Paris', 'Alice Durand', '14:00-19:00', '+33 1 53 24 00 00', 4.2, 'https://example.com/horizon-numerique');

-- ---------------------------------------------------------
-- 4. MEMBRES DE LA COMMUNAUTÉ
-- ---------------------------------------------------------
INSERT INTO community_member (name, email, password, birth_year, phone, city, membership_type) VALUES
('Alice Dubois', 'alice.d@gmail.com', 'userpass', 1995, '+33 6 11 22 33 44', 'Paris', 'VIP'),
('Jean Dupont', 'j.dupont@outlook.fr', 'userpass', 1988, '+33 6 55 44 33 22', 'Lyon', 'Standard'),
('Sarah Connor', 's.connor@sky.net', 'userpass', 1984, '+1 213 555 0199', 'Los Angeles', 'Premium'),
('Marc Lévy', 'm.levy@free.fr', 'userpass', 2000, '+33 6 77 88 99 00', 'Bordeaux', 'Standard'),
('Sophie Morel', 's.morel@gmail.com', 'userpass', 1992, '+33 6 21 43 65 87', 'Marseille', 'Premium'),
('Thomas Wright', 't.wright@web.uk', 'userpass', 1997, '+44 20 7946 0110', 'London', 'VIP');


-- ---------------------------------------------------------
-- 5. ARTIST_DISCIPLINE (Relations Croisées)
-- ---------------------------------------------------------
INSERT INTO artist_discipline (artist_id, discipline_id) VALUES 
(1, 1), (1, 2), -- Da Vinci : Peinture & Sculpture
(2, 1),         -- Van Gogh : Peinture
(3, 3), (3, 7), -- Banksy : Street Art & Installation
(4, 5), (4, 4), -- Elena : Numérique & Photo
(5, 2), (5, 7), -- Marcus : Sculpture & Installation
(6, 6);         -- Yuki : Céramique

-- ---------------------------------------------------------
-- 6. ARTWORKS (Œuvres)
-- ---------------------------------------------------------
INSERT INTO artwork (artist_id, title, creation_year, type, medium, dimensions, description, price, status) VALUES
(1, 'La Joconde', 1503, 'Peinture', 'Huile sur bois', '77 cm × 53 cm', 'Portrait emblématique de la Renaissance italienne.', 85000000.00, 'EXHIBITED'),
(1, 'La Cène', 1498, 'Peinture', 'Fresque', '460 cm × 880 cm', 'Fresque monumentale représentant le dernier repas du Christ.', 0.00, 'EXHIBITED'),
(2, 'La Nuit étoilée', 1889, 'Peinture', 'Huile sur toile', '73.7 cm × 92.1 cm', 'Vue nocturne expressive peinte depuis l’asile de Saint-Rémy.', 99000000.00, 'EXHIBITED'),
(3, 'Girl with Balloon', 2002, 'Street Art', 'Pochoir', 'Variable', 'Œuvre iconique de Banksy sur l’espoir et la fragilité.', 1200000.00, 'SOLD'),
(3, 'Love is in the Bin', 2018, 'Installation', 'Toile lacérée', 'Variable', 'Installation née de la destruction partielle de Girl with Balloon.', 20000000.00, 'EXHIBITED'),
(4, 'Neon Dreams', 2024, 'Digital Art', 'NFT / PNG', '3840 × 2160 px', 'Composition numérique aux couleurs fluorescentes.', 1500.00, 'FOR_SALE'),
(4, 'Cyber Silence', 2025, 'Photography', 'Impression Giclée', '60 cm × 40 cm', 'Photographie contemporaine à l’esthétique cybernétique.', 450.00, 'FOR_SALE'),
(5, 'Rust & Glory', 2023, 'Sculpture', 'Acier recyclé', '210 cm × 140 cm × 120 cm', 'Sculpture industrielle en matériaux récupérés.', 12000.00, 'SOLD'),
(6, 'Earth Spiral', 2024, 'Céramique', 'Grès émaillé', '35 cm × 35 cm × 55 cm', 'Pièce céramique inspirée des formes organiques.', 800.00, 'FOR_SALE');

-- ---------------------------------------------------------
-- 7. EXHIBITIONS
-- ---------------------------------------------------------
INSERT INTO exhibition (gallery_id, title, start_date, end_date, description, curator_name, theme) VALUES
(1, 'La Renaissance Italienne', '2026-01-10', '2026-06-10', 'Exposition consacrée aux grands maîtres de la Renaissance italienne.', 'Isabelle Laurent', 'Maîtres du 16ème siècle'),
(2, 'Post-Impressionnisme', '2026-03-01', '2026-05-30', 'Sélection d’œuvres célébrant la lumière, la couleur et la touche libre.', 'Thomas Reed', 'La lumière et la couleur'),
(3, 'Urban Rebellions', '2026-04-15', '2026-07-15', 'Parcours autour des gestes artistiques urbains et contestataires.', 'Maya Brooks', 'Street Art engagé'),
(4, 'Pixels & Pinceaux', '2026-05-01', '2026-08-01', 'Dialogue entre pratiques traditionnelles et création numérique.', 'Alice Durand', 'L''art à l''ère du numérique');

-- ---------------------------------------------------------
-- 8. EXHIBITION_ARTWORK (Une œuvre dans plusieurs expos)
-- ---------------------------------------------------------
INSERT INTO exhibition_artwork (exhibition_id, artwork_id) VALUES 
(1, 1), (1, 2), -- La Joconde et La Cène au Louvre
(2, 3),         -- Van Gogh à la Tate
(3, 4), (3, 5), -- Banksy à l'expo Urban
(4, 5), (4, 6); -- Banksy (Installation) et Elena à l'expo Numérique

-- ---------------------------------------------------------
-- 9. WORKSHOPS (Ateliers)
-- ---------------------------------------------------------
INSERT INTO workshop (instructor_id, title, date_time, duration_minutes, max_participants, price, location, description, level) VALUES
(4, 'Initiation Art Numérique', '2026-05-15 14:00:00', 120, 10, 50.00, 'Horizon Numérique, Paris', 'Atelier d’initiation aux outils et techniques de création numérique.', 'Débutant'),
(5, 'Sculpture sur métal', '2026-06-10 10:00:00', 240, 5, 120.00, 'Urban Pulse Gallery, London', 'Travail pratique autour du métal recyclé et des assemblages.', 'Avancé'),
(6, 'Tour de potier traditionnel', '2026-05-20 09:00:00', 180, 8, 75.00, 'Atelier Kyoto Céramique, Kyoto', 'Découverte du tournage et des finitions traditionnelles.', 'Intermédiaire');

-- ---------------------------------------------------------
-- 10. BOOKINGS (Cohérence temporelle : avant l'atelier)
-- ---------------------------------------------------------
INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES 
(1, 1, '2026-04-01 10:00:00', 'PAID'),
(1, 2, '2026-04-02 11:30:00', 'PAID'),
(2, 1, '2026-04-10 09:00:00', 'PAID'), -- Alice réserve un 2ème atelier
(3, 3, '2026-05-01 15:20:00', 'PENDING'),
(3, 6, '2026-05-02 08:45:00', 'PAID');

-- ---------------------------------------------------------
-- 11. REVIEWS & TAGS
-- ---------------------------------------------------------
INSERT INTO review (reviewer_id, artwork_id, rating, comment, review_date) VALUES 
(1, 1, 5, 'Une expérience transcendante de voir la Joconde en vrai.', '2026-02-15'),
(2, 4, 4, 'Très beau pochoir, iconique.', '2026-04-20'),
(6, 6, 2, 'Un peu trop abstrait pour moi.', '2026-05-10');

INSERT INTO artwork_tag (artwork_id, name) VALUES 
(1, 'Renaissance'), (1, 'Chef-d''oeuvre'), (3, 'Etoiles'), (4, 'Politique'), (6, 'Cyberpunk');

-- ---------------------------------------------------------
-- 12. FAVORITES
-- ---------------------------------------------------------
INSERT INTO member_favorite_discipline (member_id, discipline_id) VALUES 
(1, 1), (1, 5), -- Alice aime Peinture et Numérique
(3, 3), (3, 7), -- Sarah aime Street Art et Installation
(6, 6);         -- Thomas aime Céramique