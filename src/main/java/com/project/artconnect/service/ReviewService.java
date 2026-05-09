package com.project.artconnect.service;

import com.project.artconnect.model.Review;

import java.util.List;

public interface ReviewService {
    List<Review> findByArtworkTitle(String artworkTitle);
    void createReview(Review review);
}

