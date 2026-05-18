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
    public List<Review> findByArtwork(Artwork artwork) {
        List<Review> res = new ArrayList<>();
        if (artwork == null || artwork.getId() == null) return res;
        String sql = "SELECT r.id, r.rating, r.comment, r.review_date, m.id as reviewer_id, m.name as reviewer_name, m.email as reviewer_email, a.id as artwork_id " +
                "FROM review r JOIN artwork a ON r.artwork_id = a.id JOIN community_member m ON r.reviewer_id = m.id WHERE a.id = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, artwork.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CommunityMember reviewer = new CommunityMember();
                    reviewer.setId(rs.getLong("reviewer_id"));
                    reviewer.setName(rs.getString("reviewer_name"));
                    reviewer.setEmail(rs.getString("reviewer_email"));
                    Artwork reviewArtwork = new Artwork();
                    reviewArtwork.setId(rs.getLong("artwork_id"));
                    reviewArtwork.setTitle(artwork.getTitle());
                    Review r = new Review();
                    r.setId(rs.getLong("id"));
                    r.setReviewer(reviewer);
                    r.setArtwork(reviewArtwork);
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
        String sqlInsert = "INSERT INTO review (reviewer_id, artwork_id, rating, comment, review_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionManager.getConnection()) {
            conn.setAutoCommit(false);
            if (review.getReviewer() == null || review.getReviewer().getId() == null) {
                throw new RuntimeException("Reviewer id is required to save review");
            }
            if (review.getArtwork() == null || review.getArtwork().getId() == null) {
                throw new RuntimeException("Artwork id is required to save review");
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, review.getReviewer().getId());
                ps.setLong(2, review.getArtwork().getId());
                ps.setInt(3, review.getRating());
                ps.setString(4, review.getComment());
                ps.setDate(5, review.getReviewDate() == null ? Date.valueOf(java.time.LocalDate.now()) : Date.valueOf(review.getReviewDate()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) review.setId(keys.getLong(1)); }
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving review", e);
        }
    }
}

