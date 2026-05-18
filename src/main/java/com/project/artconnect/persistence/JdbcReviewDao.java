package com.project.artconnect.persistence;

import com.project.artconnect.dao.ReviewDao;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Review;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcReviewDao implements ReviewDao {

    @Override
    public List<Review> findByArtworkTitle(String artworkTitle) {
        List<Review> res = new ArrayList<>();
        String sql = "SELECT r.rating, r.comment, r.review_date, m.name as reviewer_name, m.email as reviewer_email " +
                "FROM review r JOIN artwork a ON r.artwork_id = a.id JOIN community_member m ON r.reviewer_id = m.id WHERE a.title = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artworkTitle);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommunityMember reviewer = new CommunityMember();
                    reviewer.setName(rs.getString("reviewer_name"));
                    reviewer.setEmail(rs.getString("reviewer_email"));
                    Artwork artwork = new Artwork();
                    artwork.setTitle(artworkTitle);
                    Review r = new Review();
                    r.setReviewer(reviewer);
                    r.setArtwork(artwork);
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    Date d = rs.getDate("review_date");
                    if (d != null) r.setReviewDate(d.toLocalDate());
                    res.add(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading reviews", e);
        }
        return res;
    }

    @Override
    public void save(Review review) {
        String sqlFindMember = "SELECT id FROM community_member WHERE email = ? LIMIT 1";
        String sqlFindArtwork = "SELECT id FROM artwork WHERE title = ? LIMIT 1";
        String sqlInsert = "INSERT INTO review (reviewer_id, artwork_id, rating, comment, review_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            long memberId;
            try (PreparedStatement psM = conn.prepareStatement(sqlFindMember)) {
                psM.setString(1, review.getReviewer().getEmail());
                try (ResultSet rs = psM.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Reviewer not found: " + review.getReviewer().getEmail());
                    memberId = rs.getLong(1);
                }
            }
            long artworkId;
            try (PreparedStatement psA = conn.prepareStatement(sqlFindArtwork)) {
                psA.setString(1, review.getArtwork().getTitle());
                try (ResultSet rs = psA.executeQuery()) {
                    if (!rs.next()) throw new RuntimeException("Artwork not found: " + review.getArtwork().getTitle());
                    artworkId = rs.getLong(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setLong(1, memberId);
                ps.setLong(2, artworkId);
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getComment());
                ps.setDate(5, review.getReviewDate() == null ? Date.valueOf(java.time.LocalDate.now()) : Date.valueOf(review.getReviewDate()));
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving review", e);
        }
    }
}

