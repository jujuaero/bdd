package com.project.artconnect.service.impl;

import com.project.artconnect.dao.WorkshopDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.persistence.JdbcWorkshopDao;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcWorkshopService implements WorkshopService {
    private final WorkshopDao workshopDao = new JdbcWorkshopDao();

    @Override
    public List<Workshop> getAllWorkshops() {
        return workshopDao.findAll();
    }

    @Override
    public Optional<Workshop> getWorkshopByTitle(String title) {
        return workshopDao.findAll().stream().filter(w -> w.getTitle().equals(title)).findFirst();
    }

    @Override
    public void bookWorkshop(Workshop workshop, CommunityMember member) {
        if (workshop == null || member == null) return;
        String insert = "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES (?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (workshop.getId() == null || member.getId() == null) {
                throw new RuntimeException("Workshop and member ids are required for booking");
            }
            try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                psIns.setLong(1, workshop.getId());
                psIns.setLong(2, member.getId());
                psIns.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                psIns.setString(4, "PENDING");
                psIns.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error booking workshop", e);
        }
    }

    @Override
    public List<Booking> getBookingsByMember(CommunityMember member) {
        if (member == null) return Collections.emptyList();
        List<Booking> res = new ArrayList<>();
        String sql = "SELECT w.id as workshop_id, w.title, b.booking_date, b.payment_status FROM booking b JOIN workshop w ON b.workshop_id = w.id "
                + "JOIN community_member cm ON b.member_id = cm.id WHERE cm.id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    Workshop w = new Workshop();
                    w.setId(rs.getLong("workshop_id"));
                    w.setTitle(rs.getString("title"));
                    b.setWorkshop(w);
                    b.setMember(member);
                    Timestamp bt = rs.getTimestamp("booking_date"); if (bt != null) b.setBookingDate(bt.toLocalDateTime());
                    b.setPaymentStatus(rs.getString("payment_status"));
                    res.add(b);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding bookings by member", e);
        }
        return res;
    }

    @Override
    public void createWorkshop(Workshop workshop) {
        String insert = "INSERT INTO workshop (instructor_id, title, date_time, duration_minutes, max_participants, price, location, description, level) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (workshop.getInstructor() == null || workshop.getInstructor().getId() == null) {
                throw new RuntimeException("Instructor id is required to create workshop");
            }
            try (PreparedStatement ps = conn.prepareStatement(insert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, workshop.getInstructor().getId());
                ps.setString(2, workshop.getTitle());
                ps.setTimestamp(3, workshop.getDate() != null ? Timestamp.valueOf(workshop.getDate()) : null);
                ps.setInt(4, workshop.getDurationMinutes());
                ps.setInt(5, workshop.getMaxParticipants());
                ps.setDouble(6, workshop.getPrice());
                ps.setString(7, workshop.getLocation());
                ps.setString(8, workshop.getDescription());
                ps.setString(9, workshop.getLevel());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) workshop.setId(keys.getLong(1)); }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving workshop", e);
        }
    }

    @Override
    public void updateWorkshop(Workshop workshop) {
        String update = "UPDATE workshop SET instructor_id=?, date_time=?, duration_minutes=?, max_participants=?, price=?, location=?, description=?, level=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (workshop.getId() == null) {
                throw new RuntimeException("Workshop id is required to update workshop");
            }
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                if (workshop.getInstructor() == null || workshop.getInstructor().getId() == null) {
                    throw new RuntimeException("Instructor id is required to update workshop");
                }
                ps.setLong(1, workshop.getInstructor().getId());
                ps.setTimestamp(2, workshop.getDate() != null ? Timestamp.valueOf(workshop.getDate()) : null);
                ps.setInt(3, workshop.getDurationMinutes());
                ps.setInt(4, workshop.getMaxParticipants());
                ps.setDouble(5, workshop.getPrice());
                ps.setString(6, workshop.getLocation());
                ps.setString(7, workshop.getDescription());
                ps.setString(8, workshop.getLevel());
                ps.setLong(9, workshop.getId());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating workshop", e);
        }
    }

    @Override
    public void deleteWorkshop(Long id) {
        String sql = "DELETE FROM workshop WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting workshop", e);
        }
    }
}

