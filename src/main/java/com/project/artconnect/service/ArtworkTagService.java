package com.project.artconnect.service;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;

import java.util.List;

public interface ArtworkTagService {
    List<ArtworkTag> getTagsFor(Artwork artwork);
    void addTag(Artwork artwork, ArtworkTag tag);
}

