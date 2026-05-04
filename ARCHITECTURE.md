# Architecture ArtConnect - Couche de Persistance JDBC

## Vue d'ensemble

L'application ArtConnect utilise une architecture en couches avec une séparation claire entre :
1. **Couche de persistance (DAO)** : Accès direct à la base de données
2. **Couche métier (Services)** : Logique applicative
3. **Couche UI (Controllers)** : Interfaces utilisateur JavaFX
4. **Couche de configuration** : Gestion de la connexion JDBC

## Diagramme des couches

```
┌─────────────────────────────────────────┐
│     Couche UI (Controllers)             │
│  ArtistController, ArtworkController... │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Couche Services (Interfaces)           │
│ ┌─────────────────────────────────────┐ │
│ │ ArtistService                       │ │
│ │ ArtworkService                      │ │
│ │ CommunityService                    │ │
│ │ GalleryService                      │ │
│ │ WorkshopService                     │ │
│ └─────────────────────────────────────┘ │
└────────────────┬────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
    ▼                         ▼
┌───────────────┐    ┌────────────────────┐
│InMemory impls │    │  JDBC impls        │
│(test/dev)     │    │ (production)       │
└───────────────┘    └────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────┐
│  Couche DAO JDBC (Data Access)          │
│ ┌─────────────────────────────────────┐ │
│ │ JdbcArtistDao                       │ │
│ │ JdbcArtworkDao                      │ │
│ │ JdbcGalleryDao                      │ │
│ │ JdbcExhibitionDao                   │ │
│ │ JdbcWorkshopDao                     │ │
│ │ JdbcCommunityMemberDao              │ │
│ └─────────────────────────────────────┘ │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  Couche Configuration                   │
│ ┌─────────────────────────────────────┐ │
│ │ DatabaseConfig                      │ │
│ │ ConnectionManager                   │ │
│ │ ServiceProvider                     │ │
│ └─────────────────────────────────────┘ │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│  MySQL Database (artconnect_db)         │
│  Tables: artist, artwork, gallery...    │
└─────────────────────────────────────────┘
```

## Composants principaux

### 1. Configuration (com.project.artconnect.util)

#### DatabaseConfig
- Lit les propriétés depuis `src/main/resources/database.properties`
- Expose constantes publiques : `URL`, `USER`, `PASSWORD`, `USE_PERSISTENCE`
- Permet basculement facile entre environnements (dev/test/prod)

#### ConnectionManager
- Fournit une connexion MySQL via `DriverManager.getConnection()`
- Utilise les propriétés de `DatabaseConfig`
- Jetable (.close()) via try-with-resources

#### ServiceProvider
- Singleton qui fournit les instances de service
- Sélectionne implémentation (InMemory ou JDBC) selon `DatabaseConfig.USE_PERSISTENCE`
- Fallback automatique si JDBC échoue

### 2. DAOs (com.project.artconnect.persistence)

Chaque DAO implémente une interface `*Dao` et utilise :
- **PreparedStatement** pour éviter injections SQL
- **Try-with-resources** pour fermeture propre des ressources
- **Transactions** pour opérations multi-étapes (INSERT + lien FK)
- **Gestion d'erreurs** : SQLException → RuntimeException

#### JdbcArtistDao
```java
findAll() → SELECT * FROM artist
save(artist) → INSERT INTO artist (...)
update(artist) → UPDATE artist SET ... WHERE name = ?
delete(name) → DELETE FROM artist WHERE name = ?
findByCity(city) → SELECT ... FROM artist WHERE city = ?
```

#### JdbcArtworkDao
```java
findAll() → SELECT aw.*, a.name FROM artwork aw JOIN artist a
save(artwork) → INSERT INTO artwork (..., artist_id, ...)
update(artwork) → UPDATE artwork SET ... artist_id = ... WHERE title = ?
delete(title) → DELETE FROM artwork WHERE title = ?
findByArtistName(name) → SELECT ... FROM artwork aw JOIN artist a WHERE a.name = ?
```

#### JdbcGalleryDao, JdbcExhibitionDao, JdbcWorkshopDao, JdbcCommunityMemberDao
- Opérations CRUD simples ou partielles selon interface
- Gestion des FK via recherche par nom/titre

### 3. Services Métier (com.project.artconnect.service.impl)

Chaque service `Jdbc*Service` :
- Délègue opérations CRUD au DAO correspondant
- Ajoute requêtes spécifiques (ex : requêtes de recherche, jointures)
- Implémente interface `*Service` publique

#### JdbcArtistService
```
getAllArtists() → artistDao.findAll()
getArtistByName(name) → artistDao.findAll().stream().filter(...)
createArtist() → artistDao.save()
getAllDisciplines() → SELECT FROM discipline
searchArtists() → filtrage + jointure artist_discipline
```

#### JdbcCommunityService, JdbcGalleryService, JdbcWorkshopService
- Même pattern : délégation DAO + requêtes complémentaires
- Gestion des relations N:1 et 1:N

### 4. ServiceProvider (Orchestration)

```java
public static ArtistService getArtistService() {
    if (USE_PERSISTENCE) {
        try { return new JdbcArtistService(); }
        catch (Exception e) { return fallback InMemory; }
    }
    return InMemory;
}
```

Permet changement d'implémentation sans modification du code chamelle (controllers).

## Modèle de données (ER simplifié)

```
┌────────────────┐         ┌──────────────┐
│    artist      │1      ∞ │   artwork    │
│ (id, name,..)  ├────────→│ (id, title) │
└────────────────┘         └──────────────┘

┌─────────────────┐     ┌────────────────┐
│   gallery       │1   ∞ │   exhibition  │
│ (id, name, ..)  ├────→│ (id, title,..)│
└─────────────────┘     └────────────────┘

┌──────────────┐     ┌────────────┐
│   workshop   │1   ∞ │   booking  │
│ (id, title)  ├────→│ (id, ...)  │
└──────────────┘     └────────────┘

┌──────────────────┐     ┌──────────┐
│ community_member │1   ∞ │ booking  │
│  (id, name, ..)  ├────→│ (id,..)  │
└──────────────────┘     └──────────┘

┌──────────────┐     ┌────────┐
│   artwork    │1   ∞│ review │
│ (id, title)  ├────→│(id,..)│
└──────────────┘     └────────┘

┌──────────────────┐       ┌──────────┐
│ community_member │    N M│discipline│
│  (id, name)      │◆──────◆│(id,name)│
└──────────────────┘       └──────────┘
    (N:M via member_favorite_discipline)
```

## Flux d'opération typique (exemple : créer un artiste)

1. **UI (ArtistController)** 
   ```
   utilisateur clique "Créer" 
   → appelle artistService.createArtist(artist)
   ```

2. **ServiceProvider**
   ```
   appelle getArtistService()
   → renvoie JdbcArtistService si USE_PERSISTENCE=true
   ```

3. **JdbcArtistService**
   ```
   createArtist(artist)
   → appelle artistDao.save(artist)
   ```

4. **JdbcArtistDao**
   ```
   save(artist)
   → prépare INSERT avec PreparedStatement
   → exécute executeUpdate()
   → ferme automatiquement (try-with-resources)
   ```

5. **MySQL**
   ```
   reçoit INSERT INTO artist VALUES (...)
   → stocke dans la table artist
   → retourne confirmation
   ```

6. **Retour à l'UI**
   ```
   artist créé en base
   → refresh la liste (reload via getArtistService().getAllArtists())
   ```

## Configuration

### Fichier: `database.properties`

```properties
# URL de connexion MySQL
url=jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC

# Authentification
user=artconnect_user
password=TonMotDePasse

# Mode persistance (true = JDBC, false = InMemory)
usePersistence=true
```

### Fichier: `DatabaseConfig.java`

Lit le fichier `database.properties` lors du chargement statique.  
Fournit les valeurs via constantes publiques.

## Avantages de cette architecture

1. **Séparation des préoccupations** : DAO ↔ Service ↔ UI cleanement séparés
2. **Testabilité** : Possibilité de tester services avec DAO mock ou H2 en mémoire
3. **Flexibilité** : Basculement InMemory ↔ JDBC via configuration (1 ligne)
4. **Évolutivité** : Ajout de nouveaux DAOs/Services sans impact sur l'existant
5. **Sécurité** : PreparedStatement par défaut (prévention injections SQL)
6. **Ressources** : Try-with-resources pour fermeture automatique

## Gestion des transactions

Les DAO implémentent transactions simples pour opérations multi-étapes :

```java
try (Connection conn = ConnectionManager.getConnection()) {
    conn.setAutoCommit(false);
    
    // Étape 1: Chercher l'artiste
    // Étape 2: Insérer l'artwork avec FK
    
    conn.commit();
} catch (SQLException e) {
    // Rollback automatique en cas d'exception
}
```

## Points d'améliorations futures

1. **Pool de connexions** (HikariCP) pour meilleure performance
2. **Gestion complète des N:M** (artist_discipline, exhibition_artwork...)
3. **Migrations DB** (Flyway/Liquibase)
4. **Cache** (Redis/Ehcache) pour données fréquemment lues
5. **Tests d'intégration** complets (JUnit + H2)
6. **Logging** structuré (SLF4J) pour debug

## Résumé

ArtConnect utilise maintenant une **architecture 3-tiers avec couche DAO JDBC**,  
permettant une vraie **persistance en base de données**, tout en conservant  
la **flexibilité de passer aux services InMemory** pour tests/développement.

Le **basculement se fait par configuration** (1 propriété dans `database.properties`),  
sans modification du code source des contrôleurs.

---
*Document généré pour le projet ArtConnect - Mai 2026*

