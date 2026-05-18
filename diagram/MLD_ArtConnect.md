# Modele logique de donnees (MLD) - ArtConnect

Ce MLD est derive de `diagram/model_class_diagram.puml`.

## 1) Schema relationnel (PK/FK)

- `discipline`(
  `id` PK,
  `name` UNIQUE NOT NULL
)

- `artist`(
  `id` PK,
  `name` NOT NULL,
  `bio`,
  `birth_year`,
  `contact_email` UNIQUE,
  `phone`,
  `city`,
  `website`,
  `social_media`,
  `is_active` NOT NULL
)

- `artist_discipline`(
  `artist_id` PK FK -> `artist.id`,
  `discipline_id` PK FK -> `discipline.id`
)

- `artwork`(
  `id` PK,
  `artist_id` FK -> `artist.id` NOT NULL,
  `title` NOT NULL,
  `creation_year`,
  `type`,
  `medium`,
  `dimensions`,
  `description`,
  `price`,
  `status` NOT NULL CHECK (`status` IN ('FOR_SALE','SOLD','EXHIBITED'))
)

- `artwork_tag`(
  `id` PK,
  `artwork_id` FK -> `artwork.id` NOT NULL,
  `name` NOT NULL,
  UNIQUE (`artwork_id`, `name`)
)

- `gallery`(
  `id` PK,
  `name` NOT NULL,
  `address`,
  `owner_name`,
  `opening_hours`,
  `contact_phone`,
  `rating` CHECK (`rating` IS NULL OR (`rating` >= 0 AND `rating` <= 5)),
  `website`
)

- `exhibition`(
  `id` PK,
  `gallery_id` FK -> `gallery.id` NOT NULL,
  `title` NOT NULL,
  `start_date` NOT NULL,
  `end_date` NOT NULL,
  `description`,
  `curator_name`,
  `theme`,
  CHECK (`end_date` >= `start_date`)
)

- `exhibition_artwork`(
  `exhibition_id` PK FK -> `exhibition.id`,
  `artwork_id` PK FK -> `artwork.id`
)

- `workshop`(
  `id` PK,
  `instructor_id` FK -> `artist.id` NOT NULL,
  `title` NOT NULL,
  `date_time` NOT NULL,
  `duration_minutes` NOT NULL CHECK (`duration_minutes` > 0),
  `max_participants` NOT NULL CHECK (`max_participants` > 0),
  `price`,
  `location`,
  `description`,
  `level`
)

- `community_member`(
  `id` PK,
  `name` NOT NULL,
  `email` UNIQUE NOT NULL,
  `birth_year`,
  `phone`,
  `city`,
  `membership_type`
)

- `member_favorite_discipline`(
  `member_id` PK FK -> `community_member.id`,
  `discipline_id` PK FK -> `discipline.id`
)

- `booking`(
  `id` PK,
  `workshop_id` FK -> `workshop.id` NOT NULL,
  `member_id` FK -> `community_member.id` NOT NULL,
  `booking_date` NOT NULL,
  `payment_status` NOT NULL
)

- `review`(
  `id` PK,
  `reviewer_id` FK -> `community_member.id` NOT NULL,
  `artwork_id` FK -> `artwork.id` NOT NULL,
  `rating` NOT NULL CHECK (`rating` BETWEEN 1 AND 5),
  `comment`,
  `review_date` NOT NULL,
  UNIQUE (`reviewer_id`, `artwork_id`)
)

## 2) Transformation UML -> relationnel

- `Artist 1,N Artwork` -> FK `artwork.artist_id`
- `Gallery 1,N Exhibition` -> FK `exhibition.gallery_id`
- `Artist 1,N Workshop` -> FK `workshop.instructor_id`
- `Workshop 1,N Booking` + `CommunityMember 1,N Booking` -> FKs `booking.workshop_id`, `booking.member_id`
- `CommunityMember 1,N Review` + `Artwork 1,N Review` -> FKs `review.reviewer_id`, `review.artwork_id`
- `Artist N,N Discipline` -> table d'association `artist_discipline`
- `Exhibition N,N Artwork` -> table d'association `exhibition_artwork`
- `CommunityMember N,N Discipline` -> table d'association `member_favorite_discipline`
- `Artwork 1,N ArtworkTag` -> FK `artwork_tag.artwork_id`

## 3) Regles de gestion importantes

- Un email artiste (si renseigne) est unique.
- Un email membre est obligatoire et unique.
- Une oeuvre a un statut parmi: `FOR_SALE`, `SOLD`, `EXHIBITED`.
- Un membre ne peut laisser qu'un seul avis par oeuvre.
- Une exposition ne peut pas finir avant sa date de debut.
- Duree et capacite d'un workshop sont strictement positives.

