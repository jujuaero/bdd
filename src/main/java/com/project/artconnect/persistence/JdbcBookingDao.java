package com.project.artconnect.persistence;

import com.project.artconnect.dao.BookingDao;
import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.util.ConnectionManager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;
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
    public List<Booking> findAll() {
        List<Booking> res = new ArrayList<>();
        String sql = "SELECT b.id, b.booking_date, b.payment_status, w.id as workshop_id, w.title as wtitle, m.id as member_id, m.name as mname, m.email as memail " +
                "FROM booking b JOIN workshop w ON b.workshop_id = w.id JOIN community_member m ON b.member_id = m.id";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
        String call = "{call sp_book_workshop(?, ?, ?)}";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (booking.getWorkshop() == null || booking.getWorkshop().getId() == null) {
                throw new RuntimeException("Workshop id is required to save booking");
            }
            if (booking.getMember() == null || booking.getMember().getId() == null) {
                throw new RuntimeException("Member id is required to save booking");
            }
            try {
                try (CallableStatement cs = conn.prepareCall(call)) {
                    cs.setLong(1, booking.getWorkshop().getId());
                    cs.setLong(2, booking.getMember().getId());
                    cs.setString(3, booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "PENDING");
                    cs.executeUpdate();
                }
                String idQuery = "SELECT id FROM booking WHERE workshop_id = ? AND member_id = ? ORDER BY id DESC LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(idQuery)) {
                    ps.setLong(1, booking.getWorkshop().getId());
                    ps.setLong(2, booking.getMember().getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) booking.setId(rs.getLong("id"));
                    }
                }
            } catch (SQLException procEx) {
                // Try fallback: direct insert if stored procedure doesn't exist or fails.
                StringBuilder combined = new StringBuilder();
                combined.append(procEx.getMessage());
                boolean inserted = false;
                try {
                    String insertSql = "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES (?,?,?,?)";
                    try (PreparedStatement ips = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        ips.setLong(1, booking.getWorkshop().getId());
                        ips.setLong(2, booking.getMember().getId());
                        ips.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                        ips.setString(4, booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "PENDING");
                        ips.executeUpdate();
                        try (ResultSet rs = ips.getGeneratedKeys()) { if (rs.next()) booking.setId(rs.getLong(1)); }
                    }
                    inserted = true;
                    combined.append("; fallback insert succeeded");
                } catch (SQLException insertEx) {
                    combined.append("; fallback insert failed: ").append(insertEx.getMessage());
                }
                if (!inserted) {
                    throw new SQLException(combined.toString(), procEx);
                }
            }
            conn.commit();
        } catch (SQLException e) {
            String extra = "";
            try {
                extra = " SQLState=" + e.getSQLState() + " ErrorCode=" + e.getErrorCode();
            } catch (Throwable ignored) {}
            throw new RuntimeException("Error saving booking via stored procedure: " + e.getMessage() + extra, e);
        }
    }

    @Override
    public void update(Booking booking) {
        String sqlUpdate = "UPDATE booking SET workshop_id = ?, member_id = ?, booking_date = ?, payment_status = ? WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (booking.getId() == null) {
                throw new RuntimeException("Booking id is required to update");
            }
            if (booking.getWorkshop() == null || booking.getWorkshop().getId() == null) {
                throw new RuntimeException("Workshop id is required to update booking");
            }
            if (booking.getMember() == null || booking.getMember().getId() == null) {
                throw new RuntimeException("Member id is required to update booking");
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setLong(1, booking.getWorkshop().getId());
                ps.setLong(2, booking.getMember().getId());
                ps.setTimestamp(3, booking.getBookingDate() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.valueOf(booking.getBookingDate()));
                ps.setString(4, booking.getPaymentStatus());
                ps.setLong(5, booking.getId());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating booking", e);
        }
    }

    @Override
    public void delete(Long id) {
        String sqlDelete = "DELETE FROM booking WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
                ps.setLong(1, id);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting booking", e);
        }
    }
}

