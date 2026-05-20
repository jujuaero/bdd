package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for ArtworkDao.
 */
public class JdbcArtworkDao implements ArtworkDao {

    @Override
    public List<Artwork> findAll() {
        List<Artwork> result = new ArrayList<>();
        String sql = "SELECT aw.id, aw.title, aw.creation_year, aw.type, aw.medium, aw.dimensions, aw.description, aw.price, aw.status, a.id as artist_id, a.name as artist_name "
                + "FROM artwork aw JOIN artist a ON aw.artist_id = a.id";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artwork art = new Artwork();
                art.setId(rs.getLong("id"));
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
                    a.setId(rs.getLong("artist_id"));
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
        String insert = "INSERT INTO artwork (artist_id, title, creation_year, type, medium, dimensions, description, price, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (artwork.getArtist() == null || artwork.getArtist().getId() == null) {
                throw new RuntimeException("Artist id is required to save artwork");
            }
            try (PreparedStatement psIns = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                psIns.setLong(1, artwork.getArtist().getId());
                psIns.setString(2, artwork.getTitle());
                if (artwork.getCreationYear() != null) psIns.setInt(3, artwork.getCreationYear()); else psIns.setNull(3, java.sql.Types.INTEGER);
                psIns.setString(4, artwork.getType());
                psIns.setString(5, artwork.getMedium());
                psIns.setString(6, artwork.getDimensions());
                psIns.setString(7, artwork.getDescription());
                psIns.setDouble(8, artwork.getPrice());
                psIns.setString(9, artwork.getStatus() != null ? artwork.getStatus().name() : null);
                psIns.executeUpdate();
                try (ResultSet keys = psIns.getGeneratedKeys()) {
                    if (keys.next()) artwork.setId(keys.getLong(1));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artwork", e);
        }
    }

    @Override
    public void update(Artwork artwork) {
        String update = "UPDATE artwork SET creation_year=?, type=?, medium=?, dimensions=?, description=?, price=?, status=?, artist_id=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (artwork.getId() == null) {
                throw new RuntimeException("Artwork id is required to update artwork");
            }
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                if (artwork.getArtist() == null || artwork.getArtist().getId() == null) {
                    throw new RuntimeException("Artist id is required to update artwork");
                }
                if (artwork.getCreationYear() != null) ps.setInt(1, artwork.getCreationYear()); else ps.setNull(1, java.sql.Types.INTEGER);
                ps.setString(2, artwork.getType());
                ps.setString(3, artwork.getMedium());
                ps.setString(4, artwork.getDimensions());
                ps.setString(5, artwork.getDescription());
                ps.setDouble(6, artwork.getPrice());
                ps.setString(7, artwork.getStatus() != null ? artwork.getStatus().name() : null);
                ps.setLong(8, artwork.getArtist().getId());
                ps.setLong(9, artwork.getId());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating artwork", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM artwork WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting artwork", e);
        }
    }

    @Override
    public List<Artwork> findByArtistName(String artistName) {
        List<Artwork> result = new ArrayList<>();
        String sql = "SELECT aw.id, aw.title, aw.creation_year, aw.type, aw.medium, aw.dimensions, aw.description, aw.price, aw.status, a.id as artist_id, a.name as artist_name "
                + "FROM artwork aw JOIN artist a ON aw.artist_id = a.id WHERE a.name = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artistName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artwork art = new Artwork();
                    art.setId(rs.getLong("id"));
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
                        a.setId(rs.getLong("artist_id"));
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

    public void addArtworkToExhibition(Long exhibitionId, Long artworkId) {
        String call = "{call sp_add_artwork_to_exhibition(?, ?)}";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (exhibitionId == null || artworkId == null) {
                throw new RuntimeException("Exhibition id and artwork id are required");
            }
            try (CallableStatement cs = conn.prepareCall(call)) {
                cs.setLong(1, exhibitionId);
                cs.setLong(2, artworkId);
                cs.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding artwork to exhibition via stored procedure", e);
        }
    }
}
