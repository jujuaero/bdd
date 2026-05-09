# Section normalisation (prete a coller)

## Objectif
Verifier que le MLD ArtConnect est normalise jusqu'a la 3e forme normale (3FN), afin de limiter les redondances, eviter les anomalies de mise a jour et garantir la coherence des donnees.

## Hypotheses de depart
- Chaque table possede une cle primaire (`id` ou cle composite pour les tables d'association).
- Les relations N-N sont materialisees par des tables d'association.
- Les regles metier sont portees par des contraintes (`NOT NULL`, `UNIQUE`, `CHECK`, `FOREIGN KEY`).

## Verification de la 1FN (forme normale 1)
Une relation est en 1FN si tous les attributs sont atomiques et sans groupe repete.

Dans ArtConnect, chaque colonne contient une valeur simple (texte, nombre, date, booleen) :
- `artist(name, bio, birth_year, ...)`
- `artwork(title, creation_year, status, ...)`
- `booking(booking_date, payment_status)`
- `review(rating, comment, review_date)`

Les anciennes listes conceptuelles (`disciplines`, `favoriteDisciplines`, `artworks`) ont ete externalisees en relations dediees (`artist_discipline`, `member_favorite_discipline`, `exhibition_artwork`).

**Conclusion 1FN : OK.**

## Verification de la 2FN (forme normale 2)
Une relation est en 2FN si elle est en 1FN et si tout attribut non-cle depend de la cle entiere.

- Tables a cle simple (`artist`, `artwork`, `gallery`, `exhibition`, `workshop`, `community_member`, `booking`, `review`, `discipline`, `artwork_tag`) : les attributs non-cles dependent de `id`.
- Tables a cle composee (`artist_discipline`, `exhibition_artwork`, `member_favorite_discipline`) : elles ne contiennent que des cles, donc aucune dependance partielle possible.

**Conclusion 2FN : OK.**

## Verification de la 3FN (forme normale 3)
Une relation est en 3FN si elle est en 2FN et sans dependance transitive (attribut non-cle -> attribut non-cle).

Constat sur ArtConnect :
- Les informations metier sont placees dans la table de leur entite d'origine (ex. donnees de galerie dans `gallery`, donnees d'artiste dans `artist`).
- Les dependances inter-entites sont gerees par des cles etrangeres, pas par duplication de colonnes descriptives.
- Les contraintes de coherence renforcent la qualite sans casser la 3FN :
  - `artwork.status` borne par CHECK (`FOR_SALE`, `SOLD`, `EXHIBITED`)
  - `review.rating` borne entre 1 et 5
  - unicites (`community_member.email`, `artist.contact_email`, `review(reviewer_id, artwork_id)`, `artwork_tag(artwork_id, name)`).

**Conclusion 3FN : OK.**

## Bilan
Le schema relationnel ArtConnect respecte la 3FN :
- pas d'attribut multivalue dans une cellule ;
- pas de dependance partielle sur une partie de cle composee ;
- pas de dependance transitive interne explicite entre attributs non-cles.

Le modele est donc adapte a une base relationnelle evolutive et maintenable.

