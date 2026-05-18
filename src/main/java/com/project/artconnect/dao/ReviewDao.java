package com.project.artconnect.dao;

import com.project.artconnect.model.Review;
import com.project.artconnect.model.Artwork;

import java.util.List;

public interface ReviewDao {
    List<Review> findByArtwork(Artwork artwork);
    void save(Review review);
}

