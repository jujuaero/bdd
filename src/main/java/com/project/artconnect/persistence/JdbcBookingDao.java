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
    public List<Booking> findByWorkshopTitle(String workshopTitle) {
        List<Booking> res = new ArrayList<>();
        String sql = "SELECT b.booking_date, b.payment_status, w.title as wtitle, m.name as mname, m.email as memail " +
                "FROM booking b JOIN workshop w ON b.workshop_id = w.id JOIN community_member m ON b.member_id = m.id WHERE w.title = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, workshopTitle);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Workshop w = new Workshop();
                    w.setTitle(rs.getString("wtitle"));
                    CommunityMember m = new CommunityMember();
                    m.setName(rs.getString("mname"));
                    m.setEmail(rs.getString("memail"));
                    Booking b = new Booking();
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
        String sqlFindWorkshop = "SELECT id FROM workshop WHERE title = ? LIMIT 1";
        String sqlFindMember = "SELECT id FROM community_member WHERE email = ? LIMIT 1";
        String sqlInsert = "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            long workshopId;
            try (PreparedStatement psW = conn.prepareStatement(sqlFindWorkshop)) {
                psW.setString(1, booking.getWorkshop().getTitle());
                try (ResultSet rs = psW.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Workshop not found: " + booking.getWorkshop().getTitle());
                    workshopId = rs.getLong(1);
                }
            }
            long memberId;
            try (PreparedStatement psM = conn.prepareStatement(sqlFindMember)) {
                psM.setString(1, booking.getMember().getEmail());
                try (ResultSet rs = psM.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Member not found: " + booking.getMember().getEmail());
                    memberId = rs.getLong(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setLong(1, workshopId);
                ps.setLong(2, memberId);
                ps.setTimestamp(3, booking.getBookingDate() == null ? new Timestamp(System.currentTimeMillis()) : Timestamp.valueOf(booking.getBookingDate()));
                ps.setString(4, booking.getPaymentStatus());
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving booking", e);
        }
    }
}

