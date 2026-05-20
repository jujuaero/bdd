package com.project.artconnect.persistence;

import com.project.artconnect.dao.ExhibitionDao;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.util.ConnectionManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        // Use stored procedure sp_create_exhibition
        String call = "{call sp_create_exhibition(?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (exhibition.getGallery() == null || exhibition.getGallery().getId() == null) {
                throw new RuntimeException("Gallery id is required to save exhibition");
            }
            try (CallableStatement cs = conn.prepareCall(call)) {
                cs.setLong(1, exhibition.getGallery().getId());
                cs.setString(2, exhibition.getTitle());
                cs.setDate(3, exhibition.getStartDate() != null ? Date.valueOf(exhibition.getStartDate()) : null);
                cs.setDate(4, exhibition.getEndDate() != null ? Date.valueOf(exhibition.getEndDate()) : null);
                cs.setString(5, exhibition.getDescription());
                cs.setString(6, exhibition.getCuratorName());
                cs.setString(7, exhibition.getTheme());
                cs.executeUpdate();
                
                // Retrieve generated ID via query
                String idQuery = "SELECT id FROM exhibition WHERE title = ? AND gallery_id = ? ORDER BY id DESC LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(idQuery)) {
                    ps.setString(1, exhibition.getTitle());
                    ps.setLong(2, exhibition.getGallery().getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) exhibition.setId(rs.getLong("id"));
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving exhibition via stored procedure", e);
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

