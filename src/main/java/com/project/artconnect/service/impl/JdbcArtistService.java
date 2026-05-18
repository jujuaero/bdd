package com.project.artconnect.service.impl;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.persistence.JdbcArtistDao;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JdbcArtistService implements ArtistService {
    private final ArtistDao artistDao = new JdbcArtistDao();

    @Override
    public List<Artist> getAllArtists() {
        return artistDao.findAll();
    }

    @Override
    public Optional<Artist> getArtistByName(String name) {
        return artistDao.findAll().stream().filter(a -> a.getName().equals(name)).findFirst();
    }

    @Override
    public void createArtist(Artist artist) {
        artistDao.save(artist);
    }

    @Override
    public void updateArtist(Artist artist) {
        artistDao.update(artist);
    }

    public void deleteArtist(String name) {
        throw new UnsupportedOperationException("Use deleteArtist(Long id)");
    }

    @Override
    public void deleteArtist(Long id) {
        artistDao.delete(id);
    }

    @Override
    public List<Discipline> getAllDisciplines() {
        List<Discipline> res = new ArrayList<>();
        String sql = "SELECT id, name FROM discipline";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Discipline d = new Discipline(rs.getString("name"));
                d.setId(rs.getLong("id"));
                res.add(d);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading disciplines", e);
        }
        return res;
    }

    @Override
    public List<Artist> searchArtists(String query, String disciplineName, String city) {
        List<Artist> base = artistDao.findAll();
        Set<String> artistsWithDiscipline = null;
        if (disciplineName != null && !disciplineName.isEmpty()) {
            String sql = "SELECT ar.name FROM artist ar JOIN artist_discipline ad ON ar.id = ad.artist_id JOIN discipline d ON d.id = ad.discipline_id WHERE d.name = ?";
            try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, disciplineName);
                try (ResultSet rs = ps.executeQuery()) {
                    artistsWithDiscipline = new java.util.HashSet<>();
                    while (rs.next()) {
                        artistsWithDiscipline.add(rs.getString("name"));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error finding artists by discipline", e);
            }
        }
        final Set<String> namesFilter = artistsWithDiscipline;
        return base.stream()
                .filter(a -> query == null || query.isEmpty() || a.getName().toLowerCase().contains(query.toLowerCase()))
                .filter(a -> city == null || city.isEmpty() || (a.getCity() != null && a.getCity().equalsIgnoreCase(city)))
                .filter(a -> namesFilter == null || namesFilter.contains(a.getName()))
                .collect(Collectors.toList());
    }
}

