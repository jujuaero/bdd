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
        String sqlFind = "SELECT id FROM artwork WHERE title = ? LIMIT 1";
        String sqlTags = "SELECT name FROM artwork_tag WHERE artwork_id = ?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement psFind = conn.prepareStatement(sqlFind)) {
            psFind.setString(1, artwork.getTitle());
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
        String sqlFind = "SELECT id FROM artwork WHERE title = ? LIMIT 1";
        String sqlInsert = "INSERT INTO artwork_tag (artwork_id, name) VALUES (?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psFind = conn.prepareStatement(sqlFind)) {
                psFind.setString(1, artwork.getTitle());
                try (ResultSet rs = psFind.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Artwork not found: " + artwork.getTitle());
                    long artworkId = rs.getLong(1);
                    try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                        ps.setLong(1, artworkId);
                        ps.setString(2, tag.getName());
                        ps.executeUpdate();
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artwork tag", e);
        }
    }
}

