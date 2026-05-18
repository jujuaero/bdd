package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtworkDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.persistence.JdbcArtworkDao;

import java.util.List;
import java.util.Optional;

public class JdbcArtworkService implements ArtworkService {
    private final ArtworkDao artworkDao = new JdbcArtworkDao();

    @Override
    public List<Artwork> getAllArtworks() {
        return artworkDao.findAll();
    }

    @Override
    public Optional<Artwork> getArtworkByTitle(String title) {
        return artworkDao.findAll().stream().filter(a -> a.getTitle().equals(title)).findFirst();
    }

    @Override
    public List<Artwork> getArtworksByArtist(Artist artist) {
        if (artist == null) return java.util.Collections.emptyList();
        return artworkDao.findByArtistName(artist.getName());
    }

    @Override
    public void createArtwork(Artwork artwork) {
        artworkDao.save(artwork);
    }

    @Override
    public void updateArtwork(Artwork artwork) {
        artworkDao.update(artwork);
    }

    public void deleteArtwork(String title) {
        throw new UnsupportedOperationException("Use deleteArtwork(Long id)");
    }

    @Override
    public void deleteArtwork(Long id) {
        artworkDao.delete(id);
    }
}

