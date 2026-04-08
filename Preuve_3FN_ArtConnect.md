# Preuve de normalisation en 3FN — ArtConnect

## 1) Objectif
Ce document justifie que le MLD de `ArtConnect` est en troisième forme normale (3FN), à partir du schéma défini dans `MLD_ArtConnect.md`.

## 2) Rappels
Une relation est en 3FN si :
- elle est en 1FN (attributs atomiques, pas de groupes répétés) ;
- elle est en 2FN (tout attribut non-clé dépend de toute la clé primaire) ;
- elle n'a pas de dépendance transitive d'un attribut non-clé vers la clé.

Autrement dit, pour toute DF `X -> A` (A attribut non premier), soit `X` est une super-clé, soit `A` est un attribut premier.

## 3) Vérification de la 1FN
Toutes les relations du MLD sont définies avec des attributs simples (id, texte, dates, numériques), sans attribut multivalué stocké dans une cellule.

Conclusion 1FN : OK.

## 4) Vérification de la 2FN
- Les tables à clé simple (`artist`, `artwork`, `gallery`, `exhibition`, `workshop`, `community_member`, `booking`, `review`, `discipline`, `artwork_tag`) satisfont la 2FN par construction : tous les attributs non-clés dépendent de la clé `id`.
- Les tables à clé composée :
  - `artist_discipline(id_artist, id_discipline)`
  - `exhibition_artwork(id_exhibition, id_artwork)`
  - `member_favorite_discipline(id_member, id_discipline)`
  ne contiennent pas d'attribut non-clé ; il n'existe donc pas de dépendance partielle possible.

Conclusion 2FN : OK.

## 5) Vérification de la 3FN par relation

### `discipline(id, name)`
- DF principale : `id -> name`.
- Pas de dépendance transitive interne.

### `artist(id, name, bio, birth_year, contact_email, phone, city, website, social_media, is_active)`
- DF principale : `id -> {name, bio, birth_year, contact_email, phone, city, website, social_media, is_active}`.
- Aucune DF non-clé vers non-clé spécifiée.

### `artwork(id, artist_id, title, creation_year, type, medium, dimensions, description, price, status)`
- DF principale : `id -> {artist_id, title, creation_year, type, medium, dimensions, description, price, status}`.
- `artist_id` est une FK ; pas de dépendance transitive interne déclarée.

### `artwork_tag(id, artwork_id, name)`
- DF principale : `id -> {artwork_id, name}`.
- La contrainte d'unicité (`artwork_id`, `name`) évite les doublons de tag sur une même œuvre.

### `gallery(id, name, address, owner_name, opening_hours, contact_phone, rating, website)`
- DF principale : `id -> {name, address, owner_name, opening_hours, contact_phone, rating, website}`.
- Pas de DF transitive interne.

### `exhibition(id, gallery_id, title, start_date, end_date, description, curator_name, theme)`
- DF principale : `id -> {gallery_id, title, start_date, end_date, description, curator_name, theme}`.
- `gallery_id` est une FK ; pas de DF non-clé -> non-clé.

### `workshop(id, instructor_id, title, date_time, duration_minutes, max_participants, price, location, description, level)`
- DF principale : `id -> {instructor_id, title, date_time, duration_minutes, max_participants, price, location, description, level}`.
- Pas de dépendance transitive interne définie.

### `community_member(id, name, email, birth_year, phone, city, membership_type)`
- DF principale : `id -> {name, email, birth_year, phone, city, membership_type}`.
- `email` est unique (clé candidate métier) ; pas de DF non-clé -> non-clé.

### `booking(id, workshop_id, member_id, booking_date, payment_status)`
- DF principale : `id -> {workshop_id, member_id, booking_date, payment_status}`.
- Les attributs référencés sont des FK ; pas de dépendance transitive interne exprimée.

### `review(id, reviewer_id, artwork_id, rating, comment, review_date)`
- DF principale : `id -> {reviewer_id, artwork_id, rating, comment, review_date}`.
- L'unicité (`reviewer_id`, `artwork_id`) impose au plus un avis par couple membre/œuvre.
- Pas de dépendance transitive interne.

### Tables d'association
- `artist_discipline(id_artist, id_discipline)`
- `exhibition_artwork(id_exhibition, id_artwork)`
- `member_favorite_discipline(id_member, id_discipline)`

Ces tables ne portent que des clés (PK composées + FK). Elles sont donc en 3FN (et même BCNF).

## 6) Conclusion
Sous les hypothèses du MLD fourni, le schéma `ArtConnect` est en 3FN :
- pas d'attribut non atomique ;
- pas de dépendance partielle sur une clé composée ;
- pas de dépendance transitive interne explicitée entre attributs non-clés.

Le MLD peut donc être défendu comme normalisé en 3FN dans le cadre du projet.

## 7) Point de vigilance (méthodologique)
Si des règles métier supplémentaires apparaissent plus tard (ex. `city -> postal_code`, `membership_type -> avantages` stockés dans la même table), il faudra réévaluer la 3FN et éventuellement extraire des tables de référence.

