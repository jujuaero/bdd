package com.project.artconnect.persistence;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCommunityMemberDao implements CommunityMemberDao {

    @Override
    public Optional<CommunityMember> findById(Long id) {
        String sql = "SELECT name, email, birth_year, phone, city, membership_type FROM community_member WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CommunityMember m = new CommunityMember();
                    m.setName(rs.getString("name"));
                    m.setEmail(rs.getString("email"));
                    int by = rs.getInt("birth_year"); if (!rs.wasNull()) m.setBirthYear(by);
                    m.setPhone(rs.getString("phone"));
                    m.setCity(rs.getString("city"));
                    m.setMembershipType(rs.getString("membership_type"));
                    // favoriteDisciplines, bookings, reviews are not loaded here
                    return Optional.of(m);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding community member by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<CommunityMember> findAll() {
        List<CommunityMember> res = new ArrayList<>();
        String sql = "SELECT id, name, email, birth_year, phone, city, membership_type FROM community_member";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CommunityMember m = new CommunityMember();
                m.setName(rs.getString("name"));
                m.setEmail(rs.getString("email"));
                int by = rs.getInt("birth_year"); if (!rs.wasNull()) m.setBirthYear(by);
                m.setPhone(rs.getString("phone"));
                m.setCity(rs.getString("city"));
                m.setMembershipType(rs.getString("membership_type"));
                res.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading community members", e);
        }
        return res;
    }
}

