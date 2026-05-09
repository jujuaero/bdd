package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.List;

public class DiscoverController {
    @FXML
    private FlowPane discoverPane;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();

    @FXML
    public void initialize() {
        // Collect some exhibitions from galleries
        List<Exhibition> featuredExhibitions = new ArrayList<>();
        List<Gallery> galleries = galleryService.getAllGalleries();
        System.out.println("[Discover] Galleries loaded: " + galleries.size());

        for (Gallery g : galleries) {
            System.out.println("  - Gallery: " + g.getName() + " with " + g.getExhibitions().size() + " exhibitions");
            featuredExhibitions.addAll(g.getExhibitions());
            if (featuredExhibitions.size() >= 3)
                break;
        }

        System.out.println("[Discover] Total exhibitions: " + featuredExhibitions.size());
        featuredExhibitions.stream().limit(3).forEach(this::addExhibitionCard);

        List<Workshop> workshops = workshopService.getAllWorkshops();
        System.out.println("[Discover] Workshops loaded: " + workshops.size());
        workshops.stream().limit(3).forEach(this::addWorkshopCard);

        System.out.println("[Discover] UI updated with " + discoverPane.getChildren().size() + " cards");
    }

    private void addExhibitionCard(Exhibition e) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("FEATURED EXHIBITION"),
                new Label(e.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Theme: " + e.getTheme()),
                new Label("Gallery: " + (e.getGallery() != null ? e.getGallery().getName() : "Unknown")));
        discoverPane.getChildren().add(card);
    }

    private void addWorkshopCard(Workshop w) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: #f1f8e9; -fx-border-color: #4caf50; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.getChildren().addAll(
                new Label("UPCOMING WORKSHOP"),
                new Label(w.getTitle()) {
                    {
                        setStyle("-fx-font-weight: bold;");
                    }
                },
                new Label("Instructor: " + (w.getInstructor() != null ? w.getInstructor().getName() : "Unknown")),
                new Label("Price: $" + w.getPrice()));
        discoverPane.getChildren().add(card);
    }
}
