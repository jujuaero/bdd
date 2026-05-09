package com.project.artconnect;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.project.artconnect.util.ServiceProvider;
import com.project.artconnect.util.DatabaseConfig;
import com.project.artconnect.util.DatabaseSeeder;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Log startup information
        System.out.println("=== ArtConnect Pro Starting ===");
        System.out.println("Mode: " + (DatabaseConfig.USE_PERSISTENCE ? "JDBC (Database)" : "In-Memory"));
        System.out.println("Database URL: " + DatabaseConfig.URL);

        // When running in JDBC mode, populate MySQL from the in-memory demo data
        DatabaseSeeder.seedIfEmpty();

        // Load and display startup data counts
        try {
            int artistCount = ServiceProvider.getArtistService().getAllArtists().size();
            int artworkCount = ServiceProvider.getArtworkService().getAllArtworks().size();
            int workshopCount = ServiceProvider.getWorkshopService().getAllWorkshops().size();
            int galleryCount = ServiceProvider.getGalleryService().getAllGalleries().size();
            int memberCount = ServiceProvider.getCommunityService().getAllMembers().size();

            System.out.println("Data loaded:");
            System.out.println("  - Artists: " + artistCount);
            System.out.println("  - Artworks: " + artworkCount);
            System.out.println("  - Workshops: " + workshopCount);
            System.out.println("  - Galleries: " + galleryCount);
            System.out.println("  - Community Members: " + memberCount);
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        System.out.println("==============================\n");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/project/artconnect/ui/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        stage.setTitle("ArtConnect Pro - Local Art Community Platform");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
