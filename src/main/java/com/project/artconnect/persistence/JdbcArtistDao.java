package com.project.artconnect.persistence;

import com.project.artconnect.dao.ArtistDao;
import com.project.artconnect.model.Artist;
import com.project.artconnect.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.List;

/**
 * JDBC implementation for ArtistDao.
 * TODO: Students must implement this using JDBC and SQL.
 */
public class JdbcArtistDao implements ArtistDao {

    @Override
    public List<Artist> findAll() {
        List<Artist> result = new ArrayList<>();
        String sql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Artist a = new Artist();
                a.setName(rs.getString("name"));
                a.setBio(rs.getString("bio"));
                int by = rs.getInt("birth_year");
                if (!rs.wasNull()) a.setBirthYear(by);
                a.setContactEmail(rs.getString("contact_email"));
                a.setPhone(rs.getString("phone"));
                a.setCity(rs.getString("city"));
                a.setWebsite(rs.getString("website"));
                a.setSocialMedia(rs.getString("social_media"));
                a.setActive(rs.getBoolean("is_active"));
                result.add(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reading artists from database", e);
        }
        return result;
    }

    @Override
    public void save(Artist artist) {
        String sql = "INSERT INTO artist (name, bio, birth_year, contact_email, phone, city, website, social_media, is_active) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artist.getName());
            ps.setString(2, artist.getBio());
            if (artist.getBirthYear() != null) ps.setInt(3, artist.getBirthYear()); else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setString(4, artist.getContactEmail());
            ps.setString(5, artist.getPhone());
            ps.setString(6, artist.getCity());
            ps.setString(7, artist.getWebsite());
            ps.setString(8, artist.getSocialMedia());
            ps.setBoolean(9, artist.isActive());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving artist", e);
        }
    }

    @Override
    public void update(Artist artist) {
        String sql = "UPDATE artist SET bio=?, birth_year=?, contact_email=?, phone=?, city=?, website=?, social_media=?, is_active=? WHERE name=?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artist.getBio());
            if (artist.getBirthYear() != null) ps.setInt(2, artist.getBirthYear()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, artist.getContactEmail());
            ps.setString(4, artist.getPhone());
            ps.setString(5, artist.getCity());
            ps.setString(6, artist.getWebsite());
            ps.setString(7, artist.getSocialMedia());
            ps.setBoolean(8, artist.isActive());
            ps.setString(9, artist.getName());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating artist", e);
        }
    }

    @Override
    public void delete(String artistName) {
        String sql = "DELETE FROM artist WHERE name = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, artistName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting artist", e);
        }
    }

    @Override
    public List<Artist> findByCity(String city) {
        List<Artist> result = new ArrayList<>();
        String sql = "SELECT name, bio, birth_year, contact_email, phone, city, website, social_media, is_active FROM artist WHERE city = ?";
        try (Connection conn = ConnectionManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Artist a = new Artist();
                    a.setName(rs.getString("name"));
                    a.setBio(rs.getString("bio"));
                    int by = rs.getInt("birth_year");
                    if (!rs.wasNull()) a.setBirthYear(by);
                    a.setContactEmail(rs.getString("contact_email"));
                    a.setPhone(rs.getString("phone"));
                    a.setCity(rs.getString("city"));
                    a.setWebsite(rs.getString("website"));
                    a.setSocialMedia(rs.getString("social_media"));
                    a.setActive(rs.getBoolean("is_active"));
                    result.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding artists by city", e);
        }
        return result;
    }
}
