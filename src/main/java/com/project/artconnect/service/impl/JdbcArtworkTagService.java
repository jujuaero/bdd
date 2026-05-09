package com.project.artconnect.service.impl;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.persistence.JdbcArtworkTagDao;
import com.project.artconnect.service.ArtworkTagService;

import java.util.List;

public class JdbcArtworkTagService implements ArtworkTagService {
    private final JdbcArtworkTagDao dao = new JdbcArtworkTagDao();

    @Override
    public List<ArtworkTag> getTagsFor(Artwork artwork) {
        return dao.findByArtwork(artwork);
    }

    @Override
    public void addTag(Artwork artwork, ArtworkTag tag) {
        dao.save(tag, artwork);
    }
}

