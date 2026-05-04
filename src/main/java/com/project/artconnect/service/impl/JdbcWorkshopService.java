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
        String findWorkshop = "SELECT id FROM workshop WHERE title = ?";
        String findMember = "SELECT id FROM community_member WHERE name = ?";
        String insert = "INSERT INTO booking (workshop_id, member_id, booking_date, payment_status) VALUES (?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            long workshopId, memberId;
            try (PreparedStatement psW = conn.prepareStatement(findWorkshop)) {
                psW.setString(1, workshop.getTitle());
                try (ResultSet rs = psW.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Workshop not found: " + workshop.getTitle());
                    workshopId = rs.getLong("id");
                }
            }
            try (PreparedStatement psM = conn.prepareStatement(findMember)) {
                psM.setString(1, member.getName());
                try (ResultSet rs = psM.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Member not found: " + member.getName());
                    memberId = rs.getLong("id");
                }
            }
            try (PreparedStatement psIns = conn.prepareStatement(insert)) {
                psIns.setLong(1, workshopId);
                psIns.setLong(2, memberId);
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
        String sql = "SELECT w.title, b.booking_date, b.payment_status FROM booking b JOIN workshop w ON b.workshop_id = w.id "
                + "JOIN community_member cm ON b.member_id = cm.id WHERE cm.name = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Booking b = new Booking();
                    Workshop w = new Workshop();
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
}

