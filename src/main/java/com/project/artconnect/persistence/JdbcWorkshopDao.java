package com.project.artconnect.persistence;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopDao implements WorkshopDao {

    @Override
    public Optional<Workshop> findById(Long id) {
        String sql = "SELECT w.id, w.title, w.date_time, w.duration_minutes, w.max_participants, w.price, w.location, w.description, w.level, a.id as instructor_id, a.name as instructor_name "
                + "FROM workshop w JOIN artist a ON w.instructor_id = a.id WHERE w.id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Workshop w = new Workshop();
                    w.setId(rs.getLong("id"));
                    w.setTitle(rs.getString("title"));
                    Timestamp dt = rs.getTimestamp("date_time"); if (dt != null) w.setDate(dt.toLocalDateTime());
                    w.setDurationMinutes(rs.getInt("duration_minutes"));
                    w.setMaxParticipants(rs.getInt("max_participants"));
                    w.setPrice(rs.getDouble("price"));
                    w.setLocation(rs.getString("location"));
                    w.setDescription(rs.getString("description"));
                    w.setLevel(rs.getString("level"));
                    String instr = rs.getString("instructor_name");
                    if (instr != null) { Artist a = new Artist(); a.setId(rs.getLong("instructor_id")); a.setName(instr); w.setInstructor(a); }
                    return Optional.of(w);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding workshop by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Workshop> findAll() {
        List<Workshop> res = new ArrayList<>();
        String sql = "SELECT w.id, w.title, w.date_time, w.duration_minutes, w.max_participants, w.price, w.location, w.description, w.level, a.id as instructor_id, a.name as instructor_name "
                + "FROM workshop w JOIN artist a ON w.instructor_id = a.id";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Workshop w = new Workshop();
                w.setId(rs.getLong("id"));
                w.setTitle(rs.getString("title"));
                Timestamp dt = rs.getTimestamp("date_time"); if (dt != null) w.setDate(dt.toLocalDateTime());
                w.setDurationMinutes(rs.getInt("duration_minutes"));
                w.setMaxParticipants(rs.getInt("max_participants"));
                w.setPrice(rs.getDouble("price"));
                w.setLocation(rs.getString("location"));
                w.setDescription(rs.getString("description"));
                w.setLevel(rs.getString("level"));
                String instr = rs.getString("instructor_name"); if (instr != null) { Artist a = new Artist(); a.setId(rs.getLong("instructor_id")); a.setName(instr); w.setInstructor(a); }
                res.add(w);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading workshops", e);
        }
        return res;
    }
}

