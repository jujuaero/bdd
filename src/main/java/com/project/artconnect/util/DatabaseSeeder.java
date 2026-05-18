package com.project.artconnect.util;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Review;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.impl.InMemoryArtistService;
import com.project.artconnect.service.impl.InMemoryArtworkService;
import com.project.artconnect.service.impl.InMemoryCommunityService;
import com.project.artconnect.service.impl.InMemoryGalleryService;
import com.project.artconnect.service.impl.InMemoryWorkshopService;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copies the demo data currently present in the in-memory services into MySQL.
 *
 * This is intended for the student workflow: start once in JDBC mode and the
 * database will be populated from the RAM demo dataset if it is empty.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seedIfEmpty() {
        if (!DatabaseConfig.USE_PERSISTENCE) {
            return;
        }

        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            if (!isDatabaseEmpty(conn)) {
                System.out.println("[Seeder] Database already contains data; skipping RAM import.");
                return;
            }
            seed(conn);
            System.out.println("[Seeder] Database seeded from in-memory demo data.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed database from in-memory data", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    private static boolean isDatabaseEmpty(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM artist";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() && rs.getLong(1) == 0;
        }
    }

    private static void seed(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            ensureCompatibleSchema(conn);
            truncateAll(conn);

            InMemoryArtistService artistService = new InMemoryArtistService();
            InMemoryArtworkService artworkService = new InMemoryArtworkService();
            artworkService.initData(artistService);
            InMemoryGalleryService galleryService = new InMemoryGalleryService();
            galleryService.initData(artworkService);
            InMemoryWorkshopService workshopService = new InMemoryWorkshopService();
            workshopService.initData(artistService);
            InMemoryCommunityService communityService = new InMemoryCommunityService();
            communityService.initData(artworkService);

            Map<String, Long> disciplineIds = new LinkedHashMap<>();
            Map<String, Long> artistIds = new LinkedHashMap<>();
            Map<String, Long> artworkIds = new LinkedHashMap<>();
            Map<String, Long> workshopIds = new LinkedHashMap<>();

            // 1) Disciplines
            for (Discipline d : artistService.getAllDisciplines()) {
                if (d != null && d.getName() != null && !disciplineIds.containsKey(d.getName())) {
                    disciplineIds.put(d.getName(), insertAndReturnId(conn,
                            "INSERT INTO discipline (name) VALUES (?)",
                            stmt -> stmt.setString(1, d.getName())));
                }
            }

            // 2) Artists
            for (Artist a : artistService.getAllArtists()) {
                if (a == null || a.getName() == null) continue;
                long artistId = insertAndReturnId(conn,
                        "INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES (?,?,?,?,?,?,?,?,?)",
                        stmt -> {
                            stmt.setString(1, a.getName());
                            stmt.setString(2, a.getBio());
                            if (a.getBirthYear() != null) stmt.setInt(3, a.getBirthYear()); else stmt.setNull(3, java.sql.Types.INTEGER);
                            stmt.setString(4, a.getContactEmail());
                            stmt.setString(5, a.getPhone());
                            stmt.setString(6, a.getCity());
                            stmt.setString(7, a.getWebsite());
                            stmt.setString(8, a.getSocialMedia());
                            stmt.setBoolean(9, a.isActive());
                        });
                artistIds.put(a.getName(), artistId);

                for (Discipline d : a.getDisciplines()) {
                    if (d != null && d.getName() != null) {
                        Long did = disciplineIds.get(d.getName());
                        if (did != null) {
                            insert(conn, "INSERT INTO artist_discipline (artist_id, discipline_id) VALUES (?,?)", stmt -> {
                                stmt.setLong(1, artistId);
                                stmt.setLong(2, did);
                            });
                        }
                    }
                }
            }

            // 3) Artworks
            for (Artwork aw : artworkService.getAllArtworks()) {
                if (aw == null || aw.getTitle() == null || aw.getArtist() == null || aw.getArtist().getName() == null) continue;
                Long artistId = artistIds.get(aw.getArtist().getName());
                if (artistId == null) continue;
                long artworkId = insertAndReturnId(conn,
                        "INSERT INTO artwork (artist_id, title, creation_year, type, medium, dimensions, description, price, status) VALUES (?,?,?,?,?,?,?,?,?)",
                        stmt -> {
                            stmt.setLong(1, artistId);
                            stmt.setString(2, aw.getTitle());
                            if (aw.getCreationYear() != null) stmt.setInt(3, aw.getCreationYear()); else stmt.setNull(3, java.sql.Types.INTEGER);
                            stmt.setString(4, aw.getType());
                            stmt.setString(5, aw.getMedium());
                            stmt.setString(6, aw.getDimensions());
                            stmt.setString(7, aw.getDescription());
                            stmt.setDouble(8, aw.getPrice());
                            stmt.setString(9, aw.getStatus() != null ? aw.getStatus().name() : null);
                        });
                artworkIds.put(aw.getTitle(), artworkId);
                for (var tag : aw.getTags()) {
                    if (tag != null && tag.getName() != null) {
                        insert(conn, "INSERT INTO artwork_tag (artwork_id, name) VALUES (?,?)", stmt -> {
                            stmt.setLong(1, artworkId);
                            stmt.setString(2, tag.getName());
                        });
                    }
                }
            }

            // 4) Galleries and Exhibitions
            for (Gallery g : galleryService.getAllGalleries()) {
                if (g == null || g.getName() == null) continue;
                long galleryId = insertAndReturnId(conn,
                        "INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES (?,?,?,?,?,?,?)",
                        stmt -> {
                            stmt.setString(1, g.getName());
                            stmt.setString(2, g.getAddress());
                            stmt.setString(3, g.getOwnerName());
                            stmt.setString(4, g.getOpeningHours());
                            stmt.setString(5, g.getContactPhone());
                            if (g.getRating() == 0.0) stmt.setNull(6, java.sql.Types.DECIMAL); else stmt.setDouble(6, g.getRating());
                            stmt.setString(7, g.getWebsite());
                        });
                for (Exhibition ex : g.getExhibitions()) {
                    if (ex == null || ex.getTitle() == null) continue;
                    long exhibitionId = insertAndReturnId(conn,
                            "INSERT INTO exhibition (gallery_id, title, start_date, end_date, description, curator_name, theme) VALUES (?,?,?,?,?,?,?)",
                            stmt -> {
                                stmt.setLong(1, galleryId);
                                stmt.setString(2, ex.getTitle());
                                if (ex.getStartDate() != null) stmt.setDate(3, Date.valueOf(ex.getStartDate())); else stmt.setNull(3, java.sql.Types.DATE);
                                if (ex.getEndDate() != null) stmt.setDate(4, Date.valueOf(ex.getEndDate())); else stmt.setNull(4, java.sql.Types.DATE);
                                stmt.setString(5, ex.getDescription());
                                stmt.setString(6, ex.getCuratorName());
                                stmt.setString(7, ex.getTheme());
                            });
                    for (Artwork aw : ex.getArtworks()) {
                        if (aw != null && aw.getTitle() != null) {
                            Long awId = artworkIds.get(aw.getTitle());
                            if (awId != null) {
                                insert(conn, "INSERT INTO exhibition_artwork (exhibition_id, artwork_id) VALUES (?,?)", stmt -> {
                                    stmt.setLong(1, exhibitionId);
                                    stmt.setLong(2, awId);
                                });
                            }
                        }
                    }
                }
            }

            // 5) Workshops
            for (Workshop w : workshopService.getAllWorkshops()) {
                if (w == null || w.getTitle() == null || w.getInstructor() == null || w.getInstructor().getName() == null) continue;
                Long artistId = artistIds.get(w.getInstructor().getName());
                if (artistId == null) continue;
                long workshopId = insertAndReturnId(conn,
                        "INSERT INTO workshop (instructor_id, title, date_time, duration_minutes, max_participants, price, location, description, level) VALUES (?,?,?,?,?,?,?,?,?)",
                        stmt -> {
                            stmt.setLong(1, artistId);
                            stmt.setString(2, w.getTitle());
                            if (w.getDate() != null) stmt.setTimestamp(3, Timestamp.valueOf(w.getDate())); else stmt.setNull(3, java.sql.Types.TIMESTAMP);
                            stmt.setInt(4, w.getDurationMinutes());
                            stmt.setInt(5, w.getMaxParticipants());
                            stmt.setDouble(6, w.getPrice());
                            stmt.setString(7, w.getLocation());
                            stmt.setString(8, w.getDescription());
                            stmt.setString(9, w.getLevel());
                        });
                workshopIds.put(w.getTitle(), workshopId);
            }

            // 6) Community members, reviews, bookings
            for (CommunityMember m : communityService.getAllMembers()) {
                if (m == null || m.getName() == null) continue;
                long memberId = insertAndReturnId(conn,
                        "INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES (?,?,?,?,?,?)",
                        stmt -> {
                            stmt.setString(1, m.getName());
                            stmt.setString(2, m.getEmail());
                            if (m.getBirthYear() != null) stmt.setInt(3, m.getBirthYear()); else stmt.setNull(3, java.sql.Types.INTEGER);
                            stmt.setString(4, m.getPhone());
                            stmt.setString(5, m.getCity());
                            stmt.setString(6, m.getMembershipType());
                        });
                for (Discipline d : m.getFavoriteDisciplines()) {
                    if (d != null && d.getName() != null) {
                        Long did = disciplineIds.get(d.getName());
                        if (did != null) {
                            insert(conn, "INSERT INTO member_favorite_discipline (member_id, discipline_id) VALUES (?,?)", stmt -> {
                                stmt.setLong(1, memberId);
                                stmt.setLong(2, did);
                            });
                        }
                    }
                }

                for (Review r : communityService.getReviewsByMember(m)) {
                    if (r == null || r.getArtwork() == null || r.getArtwork().getTitle() == null) continue;
                    Long awId = artworkIds.get(r.getArtwork().getTitle());
                    if (awId == null) continue;
                    insert(conn, "INSERT INTO review (reviewer_id, artwork_id, rating, comment, review_date) VALUES (?,?,?,?,?)", stmt -> {
                        stmt.setLong(1, memberId);
                        stmt.setLong(2, awId);
                        stmt.setInt(3, r.getRating());
                        stmt.setString(4, r.getComment());
                        stmt.setDate(5, r.getReviewDate() != null ? Date.valueOf(r.getReviewDate()) : Date.valueOf(LocalDate.now()));
                    });
                }

                for (Booking b : m.getBookings()) {
                    if (b == null || b.getWorkshop() == null || b.getWorkshop().getTitle() == null) continue;
                    Long wId = workshopIds.get(b.getWorkshop().getTitle());
                    if (wId == null) continue;
                    insert(conn, "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES (?,?,?,?)", stmt -> {
                        stmt.setLong(1, wId);
                        stmt.setLong(2, memberId);
                        stmt.setTimestamp(3, b.getBookingDate() != null ? Timestamp.valueOf(b.getBookingDate()) : Timestamp.valueOf(LocalDateTime.now()));
                        stmt.setString(4, b.getPaymentStatus());
                    });
                }
            }

            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw e;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private static void ensureCompatibleSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE artwork MODIFY price DECIMAL(15,2)");
        }
    }

    private static void truncateAll(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS=0");
            st.execute("TRUNCATE TABLE review");
            st.execute("TRUNCATE TABLE booking");
            st.execute("TRUNCATE TABLE member_favorite_discipline");
            st.execute("TRUNCATE TABLE exhibition_artwork");
            st.execute("TRUNCATE TABLE artist_discipline");
            st.execute("TRUNCATE TABLE artwork_tag");
            st.execute("TRUNCATE TABLE exhibition");
            st.execute("TRUNCATE TABLE workshop");
            st.execute("TRUNCATE TABLE community_member");
            st.execute("TRUNCATE TABLE artwork");
            st.execute("TRUNCATE TABLE gallery");
            st.execute("TRUNCATE TABLE artist");
            st.execute("TRUNCATE TABLE discipline");
            st.execute("SET FOREIGN_KEY_CHECKS=1");
        }
    }

    private static long insertAndReturnId(Connection conn, String sql, SqlConsumer<PreparedStatement> binder) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            binder.accept(ps);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("No generated key returned for: " + sql);
    }

    private static void insert(Connection conn, String sql, SqlConsumer<PreparedStatement> binder) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            binder.accept(ps);
            ps.executeUpdate();
        }
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws SQLException;
    }
}



