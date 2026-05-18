package com.project.artconnect.service.impl;

import com.project.artconnect.model.Review;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.persistence.JdbcReviewDao;
import com.project.artconnect.service.ReviewService;

import java.util.List;

public class JdbcReviewService implements ReviewService {
    private final JdbcReviewDao dao = new JdbcReviewDao();

    @Override
    public List<Review> findByArtwork(Artwork artwork) {
        return dao.findByArtwork(artwork);
    }

    @Override
    public void createReview(Review review) {
        dao.save(review);
    }
}

