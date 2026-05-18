package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtworkTagDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcArtworkTagDao implements ArtworkTagDao {

    @Override
    public List<ArtworkTag> findByArtwork(Artwork artwork) {
        List<ArtworkTag> res = new ArrayList<>();
        if (artwork == null || artwork.getId() == null) return res;
        String sqlFind = "SELECT id FROM artwork WHERE id = ? LIMIT 1";
        String sqlTags = "SELECT name FROM artwork_tag WHERE artwork_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement psFind = conn.prepareStatement(sqlFind)) {
            psFind.setLong(1, artwork.getId());
            try (ResultSet rs = psFind.executeQuery()) {
                if (rs.next()) {
                    long artworkId = rs.getLong(1);
                    try (PreparedStatement psTags = conn.prepareStatement(sqlTags)) {
                        psTags.setLong(1, artworkId);
                        try (ResultSet r2 = psTags.executeQuery()) {
                            while (r2.next()) {
                                res.add(new ArtworkTag(r2.getString("name")));
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading artwork tags", e);
        }
        return res;
    }

    @Override
    public void save(ArtworkTag tag, Artwork artwork) {
        String sqlInsert = "INSERT INTO artwork_tag (artwork_id, name) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (artwork.getId() == null) throw new RuntimeException("Artwork id is required to save artwork tag");
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, artwork.getId());
                ps.setString(2, tag.getName());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) tag.setId(keys.getLong(1)); }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artwork tag", e);
        }
    }
}

