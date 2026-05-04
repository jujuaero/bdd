package com.project.artconnect.util;

import com.project.artconnect.service.*;
import com.project.artconnect.service.impl.*;

/**
 * Service Provider to manage singleton instances of services and handle their
 * initialization.
 */
public class ServiceProvider {
    // By default we keep the in-memory services. When USE_PERSISTENCE=true in
    // database.properties, the application will attempt to use JDBC-backed
    // services (to be implemented). Until then, we keep InMemory implementations
    // to ensure the app runs.
    private static final InMemoryArtistService artistService = new InMemoryArtistService();
    private static final InMemoryArtworkService artworkService = new InMemoryArtworkService();
    private static final InMemoryGalleryService galleryService = new InMemoryGalleryService();
    private static final InMemoryWorkshopService workshopService = new InMemoryWorkshopService();
    private static final InMemoryCommunityService communityService = new InMemoryCommunityService();

    static {
        // Initialize services with their dependencies
        artworkService.initData(artistService);
        galleryService.initData(artworkService);
        workshopService.initData(artistService);
        communityService.initData(artworkService);

        if (DatabaseConfig.USE_PERSISTENCE) {
            // Persistence mode requested; instantiate JDBC-backed services when available.
            try {
                // Replace artist and artwork services with JDBC implementations
                com.project.artconnect.service.ArtistService jdbcArtist = new com.project.artconnect.service.impl.JdbcArtistService();
                com.project.artconnect.service.ArtworkService jdbcArtwork = new com.project.artconnect.service.impl.JdbcArtworkService();
                // Note: other services (Gallery, Workshop, Community) still use InMemory for now.
                // If you want full persistence, implement JdbcGalleryService, JdbcWorkshopService, JdbcCommunityService.
                // Assign to the in-memory service fields is not possible (they're final), so we will expose getters that return
                // JDBC instances when USE_PERSISTENCE is true by changing the getters below.
                System.out.println("ServiceProvider: USE_PERSISTENCE=true - JDBC services available for Artist and Artwork.");
            } catch (Throwable t) {
                System.err.println("ServiceProvider: failed to initialize JDBC services, falling back to InMemory: " + t.getMessage());
            }
        }
    }

    public static ArtistService getArtistService() {
        if (DatabaseConfig.USE_PERSISTENCE) {
            try {
                return new com.project.artconnect.service.impl.JdbcArtistService();
            } catch (Throwable t) {
                System.err.println("Failed to create JdbcArtistService, using InMemory: " + t.getMessage());
            }
        }
        return artistService;
    }

    public static ArtworkService getArtworkService() {
        if (DatabaseConfig.USE_PERSISTENCE) {
            try {
                return new com.project.artconnect.service.impl.JdbcArtworkService();
            } catch (Throwable t) {
                System.err.println("Failed to create JdbcArtworkService, using InMemory: " + t.getMessage());
            }
        }
        return artworkService;
    }

    public static GalleryService getGalleryService() {
        if (DatabaseConfig.USE_PERSISTENCE) {
            try {
                return new com.project.artconnect.service.impl.JdbcGalleryService();
            } catch (Throwable t) {
                System.err.println("Failed to create JdbcGalleryService, using InMemory: " + t.getMessage());
            }
        }
        return galleryService;
    }

    public static WorkshopService getWorkshopService() {
        if (DatabaseConfig.USE_PERSISTENCE) {
            try {
                return new com.project.artconnect.service.impl.JdbcWorkshopService();
            } catch (Throwable t) {
                System.err.println("Failed to create JdbcWorkshopService, using InMemory: " + t.getMessage());
            }
        }
        return workshopService;
    }

    public static CommunityService getCommunityService() {
        if (DatabaseConfig.USE_PERSISTENCE) {
            try {
                return new com.project.artconnect.service.impl.JdbcCommunityService();
            } catch (Throwable t) {
                System.err.println("Failed to create JdbcCommunityService, using InMemory: " + t.getMessage());
            }
        }
        return communityService;
    }
}
