package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.service.ArtworkTagService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryArtworkTagService implements ArtworkTagService {
    private final Map<String, List<ArtworkTag>> map = new HashMap<>();

    @Override
    public List<ArtworkTag> getTagsFor(Artwork artwork) {
        return new ArrayList<>(map.getOrDefault(artwork.getTitle(), new ArrayList<>()));
    }

    @Override
    public void addTag(Artwork artwork, ArtworkTag tag) {
        map.computeIfAbsent(artwork.getTitle(), k -> new ArrayList<>()).add(tag);
    }
}

