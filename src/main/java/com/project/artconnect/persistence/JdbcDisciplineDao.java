package com.project.artconnect.persistence;

import com.project.artconnect.dao.DisciplineDao;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcDisciplineDao implements DisciplineDao {

    @Override
    public List<Discipline> findAll() {
        List<Discipline> res = new ArrayList<>();
        String sql = "SELECT id, name FROM discipline";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Discipline d = new Discipline(rs.getString("name"));
                d.setId(rs.getLong("id"));
                res.add(d);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading disciplines", e);
        }
        return res;
    }

    @Override
    public void save(Discipline discipline) {
        String sql = "INSERT INTO discipline (name) VALUES (?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, discipline.getName());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) discipline.setId(keys.getLong(1)); }
        } catch (SQLException e) {
            // duplicate or other constraint -> wrap
            throw new RuntimeException("Error saving discipline", e);
        }
    }
}

