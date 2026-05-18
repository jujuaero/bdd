package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.List;

public class DiscoverController {
    @FXML
    private FlowPane discoverPane;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();

    @FXML
    public void initialize() {
        galleryService.getAllExhibitions().stream().limit(3).forEach(this::addExhibitionCard);

        List<Workshop> workshops = workshopService.getAllWorkshops();
        workshops.stream().limit(3).forEach(this::addWorkshopCard);
    }

    private void addExhibitionCard(Exhibition e) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("discover-card", "discover-card-exhibition");
        card.setPadding(new Insets(14));
        card.setPrefWidth(260);

        Label badge = new Label("FEATURED EXHIBITION");
        badge.getStyleClass().add("discover-badge");

        Label title = new Label(e.getTitle());
        title.getStyleClass().add("discover-card-title");

        Label theme = new Label("Theme: " + e.getTheme());
        theme.getStyleClass().add("discover-card-detail");

        Label gallery = new Label("Gallery: " + (e.getGallery() != null ? e.getGallery().getName() : "Unknown"));
        gallery.getStyleClass().add("discover-card-detail");

        card.getChildren().addAll(badge, title, theme, gallery);
        discoverPane.getChildren().add(card);
    }

    private void addWorkshopCard(Workshop w) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("discover-card", "discover-card-workshop");
        card.setPadding(new Insets(14));
        card.setPrefWidth(260);

        Label badge = new Label("UPCOMING WORKSHOP");
        badge.getStyleClass().addAll("discover-badge", "discover-badge-workshop");

        Label title = new Label(w.getTitle());
        title.getStyleClass().add("discover-card-title");

        Label instructor = new Label("Instructor: " + (w.getInstructor() != null ? w.getInstructor().getName() : "Unknown"));
        instructor.getStyleClass().add("discover-card-detail");

        Label price = new Label("Price: $" + w.getPrice());
        price.getStyleClass().add("discover-card-detail");

        card.getChildren().addAll(badge, title, instructor, price);
        discoverPane.getChildren().add(card);
    }
}
