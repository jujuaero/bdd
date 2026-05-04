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
    public List<Exhibition> getExhibitionsByGallery(Gallery gallery) {
        if (gallery == null) return Collections.emptyList();
        List<Exhibition> res = new ArrayList<>();
        String sql = "SELECT title, start_date, end_date, description, curator_name, theme FROM exhibition WHERE gallery_id = (SELECT id FROM gallery WHERE name = ?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, gallery.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Exhibition ex = new Exhibition();
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
}

