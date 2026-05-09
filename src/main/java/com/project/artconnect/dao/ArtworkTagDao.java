package com.project.artconnect.dao;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;

import java.util.List;

public interface ArtworkTagDao {
    List<ArtworkTag> findByArtwork(Artwork artwork);
    void save(ArtworkTag tag, Artwork artwork);
}

