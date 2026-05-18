package com.project.artconnect.ui;

import com.project.artconnect.model.Artwork;
import com.project.artconnect.model.ArtworkTag;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Review;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.service.ArtworkTagService;
import com.project.artconnect.service.ReviewService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ArtworkController {
    @FXML
    private TableView<Artwork> artworkTable;
    @FXML
    private TableColumn<Artwork, String> titleColumn;
    @FXML
    private TableColumn<Artwork, String> typeColumn;
    @FXML
    private TableColumn<Artwork, Double> priceColumn;
    @FXML
    private TableColumn<Artwork, String> statusColumn;
    @FXML
    private TableColumn<Artwork, String> artistColumn;

    @FXML
    private TableView<ArtworkTag> tagsTable;
    @FXML
    private TableColumn<ArtworkTag, String> tagNameColumn;

    @FXML
    private TableView<Review> reviewsTable;
    @FXML
    private TableColumn<Review, String> reviewerColumn;
    @FXML
    private TableColumn<Review, Integer> ratingColumn;
    @FXML
    private TableColumn<Review, String> commentColumn;
    @FXML
    private HBox crudToolbar;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();
    private final ArtworkTagService tagService = ServiceProvider.getArtworkTagService();
    private final ReviewService reviewService = ServiceProvider.getReviewService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        artistColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getArtist() != null ? cellData.getValue().getArtist().getName() : "Unknown"));

        UiPermissions.applyCrudToolbar(crudToolbar, Permissions.Resource.ARTWORKS);
        refreshTable();

        // Setup tags table
        tagNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Setup reviews table
        reviewerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReviewer() != null ? cellData.getValue().getReviewer().getName() : "Unknown"));
        ratingColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRating()).asObject());
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        // Listen to selection changes
        artworkTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTagsAndReviews(newVal);
            }
        });
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    @FXML
    private void handleAddArtwork() {
        if (!UiPermissions.checkCreate(Permissions.Resource.ARTWORKS)) return;
        Artwork draft = new Artwork();
        draft.setStatus(Artwork.Status.FOR_SALE);
        if (showArtworkDialog("Add Artwork", draft)) {
            try {
                artworkService.createArtwork(draft);
                refreshTable();
            } catch (Exception e) {
                showError("Add artwork failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditArtwork() {
        if (!UiPermissions.checkUpdate(Permissions.Resource.ARTWORKS)) return;
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select an artwork to edit.");
            return;
        }
        Artwork edited = new Artwork();
        edited.setTitle(selected.getTitle());
        edited.setCreationYear(selected.getCreationYear());
        edited.setType(selected.getType());
        edited.setMedium(selected.getMedium());
        edited.setDimensions(selected.getDimensions());
        edited.setDescription(selected.getDescription());
        edited.setPrice(selected.getPrice());
        edited.setStatus(selected.getStatus());
        edited.setArtist(selected.getArtist());

        if (showArtworkDialog("Edit Artwork", edited)) {
            try {
                artworkService.updateArtwork(edited);
                refreshTable();
            } catch (Exception e) {
                showError("Edit artwork failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteArtwork() {
        if (!UiPermissions.checkDelete(Permissions.Resource.ARTWORKS)) return;
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select an artwork to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Artwork");
        confirm.setHeaderText("Delete artwork: " + selected.getTitle());
        confirm.setContentText("This action cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                artworkService.deleteArtwork(selected.getTitle());
                refreshTable();
            } catch (Exception e) {
                showError("Delete artwork failed", e.getMessage());
            }
        }
    }

    private boolean showArtworkDialog(String title, Artwork artwork) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(artwork.getTitle());
        TextField typeField = new TextField(artwork.getType());
        TextField priceField = new TextField(artwork.getPrice() == 0.0 ? "" : Double.toString(artwork.getPrice()));
        ComboBox<Artwork.Status> statusBox = new ComboBox<>(FXCollections.observableArrayList(Artwork.Status.values()));
        statusBox.setValue(artwork.getStatus() == null ? Artwork.Status.FOR_SALE : artwork.getStatus());

        ComboBox<String> artistBox = new ComboBox<>();
        artistBox.setItems(FXCollections.observableArrayList(
                artistService.getAllArtists().stream().map(Artist::getName).toList()));
        if (artwork.getArtist() != null) {
            artistBox.setValue(artwork.getArtist().getName());
        }

        if (artwork.getTitle() != null && !artwork.getTitle().isBlank()) {
            // Keep title immutable in UI to match DAO update by title.
            titleField.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Artist:"), 0, 1);
        grid.add(artistBox, 1, 1);
        grid.add(new Label("Type:"), 0, 2);
        grid.add(typeField, 1, 2);
        InputValidation.ValidatedField priceValidation =
                InputValidation.attach(priceField, InputValidation.Type.POSITIVE_DOUBLE, "Price");
        InputValidation.addRow(grid, 3, "Price:", priceField, priceValidation);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusBox, 1, 4);
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(artistBox, Priority.ALWAYS);
        GridPane.setHgrow(typeField, Priority.ALWAYS);
        GridPane.setHgrow(statusBox, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        InputValidation.bindOkButton(dialog, priceValidation);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return false;
        }

        String t = titleField.getText() == null ? "" : titleField.getText().trim();
        String aName = artistBox.getValue();
        if (t.isEmpty() || aName == null || aName.isBlank()) {
            showError("Validation", "Title and Artist are required.");
            return false;
        }
        double price = InputValidation.parseRequiredDouble(priceField);

        Artist artist = artistService.getArtistByName(aName).orElse(null);
        if (artist == null) {
            showError("Validation", "Selected artist does not exist.");
            return false;
        }

        artwork.setTitle(t);
        artwork.setArtist(artist);
        artwork.setType(typeField.getText());
        artwork.setPrice(price);
        artwork.setStatus(statusBox.getValue());
        return true;
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
        tagsTable.setItems(FXCollections.observableArrayList());
        reviewsTable.setItems(FXCollections.observableArrayList());
    }

    private void showError(String header, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(message);
        a.showAndWait();
    }

    private void updateTagsAndReviews(Artwork artwork) {
        // Load tags for selected artwork
        tagsTable.setItems(FXCollections.observableArrayList(tagService.getTagsFor(artwork)));

        // Load reviews for selected artwork
        reviewsTable.setItems(FXCollections.observableArrayList(reviewService.findByArtworkTitle(artwork.getTitle())));
    }
}

