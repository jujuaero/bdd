package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    @Override
    public List<Artwork> findAll() {
        List<Artwork> result = new ArrayList<>();
        String sql = "SELECT aw.title, aw.creation_year, aw.type, aw.medium, aw.dimensions, aw.description, aw.price, aw.status, a.name as artist_name "
                + "FROM artwork aw JOIN artist a ON aw.artist_id = a.id";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artwork art = new Artwork();
                art.setTitle(rs.getString("title"));
                int cy = rs.getInt("creation_year");
                if (!rs.wasNull()) art.setCreationYear(cy);
                art.setType(rs.getString("type"));
                art.setMedium(rs.getString("medium"));
                art.setDimensions(rs.getString("dimensions"));
                art.setDescription(rs.getString("description"));
                art.setPrice(rs.getDouble("price"));
                String status = rs.getString("status");
                if (status != null) {
                    try {
                        art.setStatus(Artwork.Status.valueOf(status));
                    } catch (IllegalArgumentException e) {
                        art.setStatus(null);
                    }
                }
                String artistName = rs.getString("artist_name");
                if (artistName != null) {
                    Artist a = new Artist();
                    a.setName(artistName);
                    art.setArtist(a);
                }
                result.add(art);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading artworks", e);
        }
        return result;
    }

    @Override
    public void save(Artwork artwork) {
        String findArtistId = "SELECT id FROM artist WHERE name = ?";
        String insert = "INSERT INTO artwork (artist_id, title, creation_year, type, medium, dimensions, description, price, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psFind = conn.prepareStatement(findArtistId)) {
                psFind.setString(1, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Artist not found: " + (artwork.getArtist() != null ? artwork.getArtist().getName() : "null"));
                    }
                    long artistId = rs.getLong("id");
                    try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                        psIns.setLong(1, artistId);
                        psIns.setString(2, artwork.getTitle());
                        if (artwork.getCreationYear() != null) psIns.setInt(3, artwork.getCreationYear()); else psIns.setNull(3, java.sql.Types.INTEGER);
                        psIns.setString(4, artwork.getType());
                        psIns.setString(5, artwork.getMedium());
                        psIns.setString(6, artwork.getDimensions());
                        psIns.setString(7, artwork.getDescription());
                        psIns.setDouble(8, artwork.getPrice());
                        psIns.setString(9, artwork.getStatus() != null ? artwork.getStatus().name() : null);
                        psIns.executeUpdate();
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artwork", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String findArtistId = "SELECT id FROM artist WHERE name = ?";
        String update = "UPDATE artwork SET creation_year=?, type=?, medium=?, dimensions=?, description=?, price=?, status=?, artist_id=? WHERE title=?";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            long artistId;
            try (PreparedStatement psFind = conn.prepareStatement(findArtistId)) {
                psFind.setString(1, artwork.getArtist() != null ? artwork.getArtist().getName() : null);
                try (ResultSet rs = psFind.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Artist not found: " + (artwork.getArtist() != null ? artwork.getArtist().getName() : "null"));
                    }
                    artistId = rs.getLong("id");
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                if (artwork.getCreationYear() != null) ps.setInt(1, artwork.getCreationYear()); else ps.setNull(1, java.sql.Types.INTEGER);
                ps.setString(2, artwork.getType());
                ps.setString(3, artwork.getMedium());
                ps.setString(4, artwork.getDimensions());
                ps.setString(5, artwork.getDescription());
                ps.setDouble(6, artwork.getPrice());
                ps.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : null);
                ps.setLong(8, artistId);
                ps.setString(9, artwork.getTitle());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating artwork", e);
        }
    }

    @Override
    public void delete(String title) {
        String sql = "DELETE FROM artwork WHERE title = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting artwork", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> result = new ArrayList<>();
        String sql = "SELECT aw.title, aw.creation_year, aw.type, aw.medium, aw.dimensions, aw.description, aw.price, aw.status, a.name as artist_name "
                + "FROM artwork aw JOIN artist a ON aw.artist_id = a.id WHERE a.name = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork art = new Artwork();
                    art.setTitle(rs.getString("title"));
                    int cy = rs.getInt("creation_year");
                    if (!rs.wasNull()) art.setCreationYear(cy);
                    art.setType(rs.getString("type"));
                    art.setMedium(rs.getString("medium"));
                    art.setDimensions(rs.getString("dimensions"));
                    art.setDescription(rs.getString("description"));
                    art.setPrice(rs.getDouble("price"));
                    String status = rs.getString("status");
                    if (status != null) {
                        try {
                            art.setStatus(Artwork.Status.valueOf(status));
                        } catch (IllegalArgumentException e) {
                            art.setStatus(null);
                        }
                    }
                    String aName = rs.getString("artist_name");
                    if (aName != null) {
                        Artist a = new Artist();
                        a.setName(aName);
                        art.setArtist(a);
                    }
                    result.add(art);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding artworks by artist", e);
        }
        return result;
    }
}
