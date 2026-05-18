package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JdbcExhibitionDao implements ExhibitionDao {

    @Override
    public List<Exhibition> findAll() {
        List<Exhibition> res = new ArrayList<>();
        String sql = "SELECT e.id, e.title, e.start_date, e.end_date, e.description, e.curator_name, e.theme, g.id as gallery_id, g.name as gallery_name "
                + "FROM exhibition e JOIN gallery g ON e.gallery_id = g.id";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Exhibition ex = new Exhibition();
                ex.setId(rs.getLong("id"));
                ex.setTitle(rs.getString("title"));
                Date sd = rs.getDate("start_date"); if (sd != null) ex.setStartDate(sd.toLocalDate());
                Date ed = rs.getDate("end_date"); if (ed != null) ex.setEndDate(ed.toLocalDate());
                ex.setDescription(rs.getString("description"));
                ex.setCuratorName(rs.getString("curator_name"));
                ex.setTheme(rs.getString("theme"));
                String gName = rs.getString("gallery_name");
                if (gName != null) {
                    Gallery g = new Gallery();
                    g.setId(rs.getLong("gallery_id"));
                    g.setName(gName);
                    ex.setGallery(g);
                }
                res.add(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading exhibitions", e);
        }
        return res;
    }

    @Override
    public void save(Exhibition exhibition) {
        String insert = "INSERT INTO exhibition (gallery_id, title, start_date, end_date, description, curator_name, theme) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (exhibition.getGallery() == null || exhibition.getGallery().getId() == null) {
                throw new RuntimeException("Gallery id is required to save exhibition");
            }
            try (PreparedStatement psIns = conn.prepareStatement(insert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                psIns.setLong(1, exhibition.getGallery().getId());
                psIns.setString(2, exhibition.getTitle());
                psIns.setDate(3, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
                psIns.setDate(4, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
                psIns.setString(5, exhibition.getDescription());
                psIns.setString(6, exhibition.getCuratorName());
                psIns.setString(7, exhibition.getTheme());
                psIns.executeUpdate();
                try (ResultSet keys = psIns.getGeneratedKeys()) {
                    if (keys.next()) exhibition.setId(keys.getLong(1));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving exhibition", e);
        }
    }

    @Override
    public void update(Exhibition exhibition) {
        String update = "UPDATE exhibition SET start_date=?, end_date=?, description=?, curator_name=?, theme=?, gallery_id=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (exhibition.getId() == null) {
                throw new RuntimeException("Exhibition id is required to update exhibition");
            }
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                if (exhibition.getGallery() == null || exhibition.getGallery().getId() == null) {
                    throw new RuntimeException("Gallery id is required to update exhibition");
                }
                ps.setDate(1, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
                ps.setDate(2, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
                ps.setString(3, exhibition.getDescription());
                ps.setString(4, exhibition.getCuratorName());
                ps.setString(5, exhibition.getTheme());
                ps.setLong(6, exhibition.getGallery().getId());
                ps.setLong(7, exhibition.getId());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating exhibition", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM exhibition WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting exhibition", e);
        }
    }
}

