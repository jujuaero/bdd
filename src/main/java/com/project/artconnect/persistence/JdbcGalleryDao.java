package com.project.artconnect.persistence;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryDao implements GalleryDao {

    @Override
    public Optional<Gallery> findById(Long id) {
        String sql = "SELECT id, name, address, owner_name, opening_hours, contact_phone, rating, website FROM gallery WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Gallery g = new Gallery();
                    g.setId(rs.getLong("id"));
                    g.setName(rs.getString("name"));
                    g.setAddress(rs.getString("address"));
                    g.setOwnerName(rs.getString("owner_name"));
                    g.setOpeningHours(rs.getString("opening_hours"));
                    g.setContactPhone(rs.getString("contact_phone"));
                    g.setRating(rs.getDouble("rating"));
                    g.setWebsite(rs.getString("website"));
                    return Optional.of(g);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding gallery by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Gallery> findAll() {
        List<Gallery> res = new ArrayList<>();
        String sql = "SELECT id, name, address, owner_name, opening_hours, contact_phone, rating, website FROM gallery";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Gallery g = new Gallery();
                g.setId(rs.getLong("id"));
                g.setName(rs.getString("name"));
                g.setAddress(rs.getString("address"));
                g.setOwnerName(rs.getString("owner_name"));
                g.setOpeningHours(rs.getString("opening_hours"));
                g.setContactPhone(rs.getString("contact_phone"));
                g.setRating(rs.getDouble("rating"));
                g.setWebsite(rs.getString("website"));
                res.add(g);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading galleries", e);
        }
        return res;
    }
}

