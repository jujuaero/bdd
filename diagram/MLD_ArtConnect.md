# Modèle logique de données (MLD) — ArtConnect

## Schéma relationnel

discipline(i̲d̲, name)
artist(i̲d̲, name, bio, birth_year, contact_email, phone, city, website, social_media, is_active)
artist_discipline(i̲d̲_artist, i̲d̲_discipline)
artwork(i̲d̲, artist_id, title, creation_year, type, medium, dimensions, description, price, status)
artwork_tag(i̲d̲, artwork_id, name)
gallery(i̲d̲, name, address, owner_name, opening_hours, contact_phone, rating, website)
exhibition(i̲d̲, gallery_id, title, start_date, end_date, description, curator_name, theme)
exhibition_artwork(i̲d̲_exhibition, i̲d̲_artwork)
workshop(i̲d̲, instructor_id, title, date_time, duration_minutes, max_participants, price, location, description, level)
community_member(i̲d̲, name, email, birth_year, phone, city, membership_type)
member_favorite_discipline(i̲d̲_member, i̲d̲_discipline)
booking(i̲d̲, workshop_id, member_id, booking_date, payment_status)
review(i̲d̲, reviewer_id, artwork_id, rating, comment, review_date)

## Clés étrangères

- `artist_discipline.artist_id` → `artist.id`
- `artist_discipline.discipline_id` → `discipline.id`
- `artwork.artist_id` → `artist.id`
- `artwork_tag.artwork_id` → `artwork.id`
- `exhibition.gallery_id` → `gallery.id`
- `exhibition_artwork.exhibition_id` → `exhibition.id`
- `exhibition_artwork.artwork_id` → `artwork.id`
- `workshop.instructor_id` → `artist.id`
- `member_favorite_discipline.member_id` → `community_member.id`
- `member_favorite_discipline.discipline_id` → `discipline.id`
- `booking.workshop_id` → `workshop.id`
- `booking.member_id` → `community_member.id`
- `review.reviewer_id` → `community_member.id`
- `review.artwork_id` → `artwork.id`

## Contraintes principales

- `artist.contact_email` est unique.
- `community_member.email` est unique.
- `artwork.status` prend les valeurs `FOR_SALE`, `SOLD` ou `EXHIBITED`.
- `artwork_tag` est rattaché à une seule œuvre via `artwork_id`.
- `review` possède une contrainte d’unicité sur (`reviewer_id`, `artwork_id`).
- Les tables `artist_discipline`, `exhibition_artwork` et `member_favorite_discipline` sont des tables d’association.

## Lecture métier

- Un artiste peut être lié à plusieurs disciplines.
- Un artiste peut créer plusieurs œuvres.
- Une œuvre peut avoir plusieurs tags.
- Une galerie peut organiser plusieurs expositions.
- Une exposition peut présenter plusieurs œuvres.
- Un artiste peut animer plusieurs ateliers.
- Un membre peut avoir plusieurs disciplines favorites.
- Un membre peut effectuer plusieurs réservations.
- Un atelier peut recevoir plusieurs réservations.
- Un membre peut rédiger plusieurs avis.
- Une œuvre peut recevoir plusieurs avis.

