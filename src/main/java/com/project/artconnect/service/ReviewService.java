package com.project.artconnect.service;

import com.project.artconnect.model.Review;
import com.project.artconnect.model.Artwork;

import java.util.List;

public interface ReviewService {
    List<Review> findByArtwork(Artwork artwork);
    void createReview(Review review);
}

