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
        String sql = "SELECT name FROM discipline";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res.add(new Discipline(rs.getString("name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading disciplines", e);
        }
        return res;
    }

    @Override
    public void save(Discipline discipline) {
        String sql = "INSERT INTO discipline (name) VALUES (?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, discipline.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            // duplicate or other constraint -> wrap
            throw new RuntimeException("Error saving discipline", e);
        }
    }
}

