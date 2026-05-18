package com.project.artconnect.service.impl;

import com.project.artconnect.dao.CommunityMemberDao;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.persistence.JdbcCommunityMemberDao;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JdbcCommunityService implements CommunityService {
    private final CommunityMemberDao memberDao = new JdbcCommunityMemberDao();

    @Override
    public List<CommunityMember> getAllMembers() {
        return memberDao.findAll();
    }

    @Override
    public Optional<CommunityMember> getMemberByName(String name) {
        return memberDao.findAll().stream().filter(m -> m.getName().equals(name)).findFirst();
    }

    @Override
    public List<Review> getReviewsByMember(CommunityMember member) {
        if (member == null) return Collections.emptyList();
        List<Review> res = new ArrayList<>();
        String sql = "SELECT r.id, r.rating, r.comment, r.review_date, aw.id as artwork_id, aw.title FROM review r JOIN artwork aw ON r.artwork_id = aw.id "
                + "JOIN community_member cm ON r.reviewer_id = cm.id WHERE cm.id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, member.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review rev = new Review();
                    rev.setId(rs.getLong("id"));
                    rev.setReviewer(member);
                    Artwork aw = new Artwork();
                    aw.setId(rs.getLong("artwork_id"));
                    aw.setTitle(rs.getString("title"));
                    rev.setArtwork(aw);
                    rev.setRating(rs.getInt("rating"));
                    rev.setComment(rs.getString("comment"));
                    Date rd = rs.getDate("review_date"); if (rd != null) rev.setReviewDate(rd.toLocalDate());
                    res.add(rev);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reviews by member", e);
        }
        return res;
    }

    @Override
    public void createMember(CommunityMember member) {
        String sql = "INSERT INTO community_member (name, email, birth_year, phone, city, membership_type) VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            if (member.getBirthYear() != null) ps.setInt(3, member.getBirthYear()); else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, member.getPhone());
            ps.setString(5, member.getCity());
            ps.setString(6, member.getMembershipType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) member.setId(keys.getLong(1)); }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving community member", e);
        }
    }

    @Override
    public void updateMember(CommunityMember member) {
        String sql = "UPDATE community_member SET name=?, email=?, birth_year=?, phone=?, city=?, membership_type=? WHERE id=?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            if (member.getBirthYear() != null) ps.setInt(3, member.getBirthYear()); else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, member.getPhone());
            ps.setString(5, member.getCity());
            ps.setString(6, member.getMembershipType());
            ps.setLong(7, member.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating community member", e);
        }
    }

    @Override
    public void deleteMember(Long id) {
        String sql = "DELETE FROM community_member WHERE id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting community member", e);
        }
    }
}

