package com.project.artconnect.service.impl;

import com.project.artconnect.model.Review;
import com.project.artconnect.service.ReviewService;

import java.util.ArrayList;
import java.util.List;

public class InMemoryReviewService implements ReviewService {
    private final List<Review> list = new ArrayList<>();

    @Override
    public List<Review> findByArtworkTitle(String artworkTitle) {
        List<Review> res = new ArrayList<>();
        for (Review r : list) {
            if (r.getArtwork() != null && artworkTitle.equals(r.getArtwork().getTitle())) res.add(r);
        }
        return res;
    }

    @Override
    public void createReview(Review review) {
        list.add(review);
    }
}

