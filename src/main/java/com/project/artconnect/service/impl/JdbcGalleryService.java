package com.project.artconnect.service.impl;

import com.project.artconnect.dao.GalleryDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.persistence.JdbcGalleryDao;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcGalleryService implements GalleryService {
    private final GalleryDao galleryDao = new JdbcGalleryDao();

    @Override
    public List<Gallery> getAllGalleries() {
        return galleryDao.findAll();
    }

    @Override
    public Optional<Gallery> getGalleryByName(String name) {
        return galleryDao.findAll().stream().filter(g -> g.getName().equals(name)).findFirst();
    }

    @Override
    public List<Exhibition> getAllExhibitions() {
        List<Exhibition> res = new ArrayList<>();
        for (Gallery gallery : getAllGalleries()) {
            res.addAll(getExhibitionsByGallery(gallery));
        }
        return res;
    }

    @Override
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null || gallery.getId() == null) return Collections.emptyList();
        List<Exhibition> res = new ArrayList<>();
        String sql = "SELECT id, title, start_date, end_date, description, curator_name, theme FROM exhibition WHERE gallery_id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, gallery.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exhibition ex = new Exhibition();
                    ex.setId(rs.getLong("id"));
                    ex.setTitle(rs.getString("title"));
                    Date sd = rs.getDate("start_date"); if (sd != null) ex.setStartDate(sd.toLocalDate());
                    Date ed = rs.getDate("end_date"); if (ed != null) ex.setEndDate(ed.toLocalDate());
                    ex.setDescription(rs.getString("description"));
                    ex.setCuratorName(rs.getString("curator_name"));
                    ex.setTheme(rs.getString("theme"));
                    ex.setGallery(gallery);
                    res.add(ex);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding exhibitions by gallery", e);
        }
        return res;
    }

    @Override
    public void createGallery(Gallery gallery) {
        String sql = "INSERT INTO gallery (name, address, owner_name, opening_hours, contact_phone, rating, website) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gallery.getName());
            ps.setString(2, gallery.getAddress());
            ps.setString(3, gallery.getOwnerName());
            ps.setString(4, gallery.getOpeningHours());
            ps.setString(5, gallery.getContactPhone());
            if (gallery.getRating() == 0.0) ps.setNull(6, java.sql.Types.DECIMAL); else ps.setDouble(6, gallery.getRating());
            ps.setString(7, gallery.getWebsite());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) gallery.setId(keys.getLong(1)); }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving gallery", e);
        }
    }

    @Override
    public void updateGallery(Gallery gallery) {
        String sql = "UPDATE gallery SET name=?, address=?, owner_name=?, opening_hours=?, contact_phone=?, rating=?, website=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gallery.getName());
            ps.setString(2, gallery.getAddress());
            ps.setString(3, gallery.getOwnerName());
            ps.setString(4, gallery.getOpeningHours());
            ps.setString(5, gallery.getContactPhone());
            if (gallery.getRating() == 0.0) ps.setNull(6, java.sql.Types.DECIMAL); else ps.setDouble(6, gallery.getRating());
            ps.setString(7, gallery.getWebsite());
            ps.setLong(8, gallery.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating gallery", e);
        }
    }

    @Override
    public void deleteGallery(Long id) {
        String sql = "DELETE FROM gallery WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting gallery", e);
        }
    }

    @Override
    public void createExhibition(Exhibition exhibition) {
        String sql = "INSERT INTO exhibition (gallery_id, title, start_date, end_date, description, curator_name, theme) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            if (exhibition.getGallery() == null || exhibition.getGallery().getId() == null) {
                throw new RuntimeException("Gallery id is required to save exhibition");
            }
            ps.setLong(1, exhibition.getGallery().getId());
            ps.setString(2, exhibition.getTitle());
            ps.setDate(3, exhibition.getStartDate() != null ? java.sql.Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(4, exhibition.getEndDate() != null ? java.sql.Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(5, exhibition.getDescription());
            ps.setString(6, exhibition.getCuratorName());
            ps.setString(7, exhibition.getTheme());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) exhibition.setId(keys.getLong(1)); }
            com.project.artconnect.util.TriggerAlertService.showPendingAlerts();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving exhibition", e);
        }
    }

    @Override
    public void updateExhibition(Exhibition exhibition) {
        String sql = "UPDATE exhibition SET gallery_id=?, start_date=?, end_date=?, description=?, curator_name=?, theme=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (exhibition.getGallery() == null || exhibition.getGallery().getId() == null || exhibition.getId() == null) {
                throw new RuntimeException("Exhibition and gallery ids are required to update exhibition");
            }
            ps.setLong(1, exhibition.getGallery().getId());
            ps.setDate(2, exhibition.getStartDate() != null ? java.sql.Date.valueOf(exhibition.getStartDate()) : null);
            ps.setDate(3, exhibition.getEndDate() != null ? java.sql.Date.valueOf(exhibition.getEndDate()) : null);
            ps.setString(4, exhibition.getDescription());
            ps.setString(5, exhibition.getCuratorName());
            ps.setString(6, exhibition.getTheme());
            ps.setLong(7, exhibition.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating exhibition", e);
        }
    }

    @Override
    public void deleteExhibition(Long id) {
        String sql = "DELETE FROM exhibition WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting exhibition", e);
        }
    }
}

