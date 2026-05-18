package com.project.artconnect.service.impl;

import com.project.artconnect.model.Review;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ReviewService;

import java.util.ArrayList;
import java.util.List;

public class InMemoryReviewService implements ReviewService {
    private final List<Review> list = new ArrayList<>();

    @Override
    public List<Review> findByArtwork(Artwork artwork) {
        List<Review> res = new ArrayList<>();
        if (artwork == null) return res;
        for (Review r : list) {
            if (r.getArtwork() != null && artwork.getId() != null && artwork.getId().equals(r.getArtwork().getId())) res.add(r);
        }
        return res;
    }

    @Override
    public void createReview(Review review) {
        list.add(review);
    }
}

