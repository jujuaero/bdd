package com.project.artconnect.persistence;

import com.project.artconnect.dao.BookingDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcBookingDao implements BookingDao {

    @Override
    public List<Booking> findByWorkshop(Workshop workshop) {
        List<Booking> res = new ArrayList<>();
        if (workshop == null || workshop.getId() == null) return res;
        String sql = "SELECT b.id, b.booking_date, b.payment_status, w.id as workshop_id, w.title as wtitle, m.id as member_id, m.name as mname, m.email as memail " +
                "FROM booking b JOIN workshop w ON b.workshop_id = w.id JOIN community_member m ON b.member_id = m.id WHERE w.id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, workshop.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workshop w = new Workshop();
                    w.setId(rs.getLong("workshop_id"));
                    w.setTitle(rs.getString("wtitle"));
                    CommunityMember m = new CommunityMember();
                    m.setId(rs.getLong("member_id"));
                    m.setName(rs.getString("mname"));
                    m.setEmail(rs.getString("memail"));
                    Booking b = new Booking();
                    b.setId(rs.getLong("id"));
                    b.setWorkshop(w);
                    b.setMember(m);
                    Timestamp ts = rs.getTimestamp("booking_date");
                    if (ts != null) b.setBookingDate(ts.toLocalDateTime());
                    b.setPaymentStatus(rs.getString("payment_status"));
                    res.add(b);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading bookings", e);
        }
        return res;
    }

    @Override
    public void save(Booking booking) {
        String sqlInsert = "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (booking.getWorkshop() == null || booking.getWorkshop().getId() == null) {
                throw new RuntimeException("Workshop id is required to save booking");
            }
            if (booking.getMember() == null || booking.getMember().getId() == null) {
                throw new RuntimeException("Member id is required to save booking");
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, booking.getWorkshop().getId());
                ps.setLong(2, booking.getMember().getId());
                ps.setTimestamp(3, booking.getBookingDate() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.valueOf(booking.getBookingDate()));
                ps.setString(4, booking.getPaymentStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) booking.setId(keys.getLong(1)); }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving booking", e);
        }
    }
}

