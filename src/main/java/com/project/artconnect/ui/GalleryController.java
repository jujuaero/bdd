package com.project.artconnect.ui;

import com.project.artconnect.model.Gallery;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class GalleryController {
    @FXML
    private ListView<Gallery> galleryList;
    @FXML
    private HBox crudToolbar;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        UiPermissions.applyCrudToolbar(crudToolbar, Permissions.Resource.GALLERIES);
        refreshData();

        // Custom cell factory to show more info
        galleryList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Gallery item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getAddress() + " (" + item.getRating() + "/5.0)");
                }
            }
        });
    }

    @FXML
    public void handleRefresh() {
        refreshData();
    }

    @FXML
    public void handleAddGallery() {
        if (!UiPermissions.checkCreate(Permissions.Resource.GALLERIES)) return;
        Gallery draft = new Gallery();
        if (showGalleryDialog("Add Gallery", draft, false)) {
            try {
                galleryService.createGallery(draft);
                refreshData();
            } catch (Exception e) {
                showError("Add gallery failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handleEditGallery() {
        if (!UiPermissions.checkUpdate(Permissions.Resource.GALLERIES)) return;
        Gallery selected = galleryList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select a gallery to edit.");
            return;
        }
        Gallery edited = new Gallery();
        edited.setId(selected.getId());
        edited.setName(selected.getName());
        edited.setAddress(selected.getAddress());
        edited.setOwnerName(selected.getOwnerName());
        edited.setOpeningHours(selected.getOpeningHours());
        edited.setContactPhone(selected.getContactPhone());
        edited.setRating(selected.getRating());
        edited.setWebsite(selected.getWebsite());
        if (showGalleryDialog("Edit Gallery", edited, true)) {
            try {
                galleryService.updateGallery(edited);
                refreshData();
            } catch (Exception e) {
                showError("Edit gallery failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handleDeleteGallery() {
        if (!UiPermissions.checkDelete(Permissions.Resource.GALLERIES)) return;
        Gallery selected = galleryList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select a gallery to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Gallery");
        confirm.setHeaderText("Delete gallery: " + selected.getName());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                galleryService.deleteGallery(selected.getId());
                refreshData();
            } catch (Exception e) {
                showError("Delete gallery failed", e.getMessage());
            }
        }
    }

    private void refreshData() {
        galleryList.setItems(FXCollections.observableArrayList(galleryService.getAllGalleries()));
    }

    private boolean showGalleryDialog(String title, Gallery gallery, boolean editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(gallery.getName());
        TextField addressField = new TextField(gallery.getAddress());
        TextField ownerField = new TextField(gallery.getOwnerName());
        TextField hoursField = new TextField(gallery.getOpeningHours());
        TextField phoneField = new TextField(gallery.getContactPhone());
        TextField ratingField = new TextField(gallery.getRating() == 0.0 ? "" : Double.toString(gallery.getRating()));
        TextField websiteField = new TextField(gallery.getWebsite());

        if (editing) nameField.setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1); grid.add(addressField, 1, 1);
        grid.add(new Label("Owner:"), 0, 2); grid.add(ownerField, 1, 2);
        grid.add(new Label("Opening Hours:"), 0, 3); grid.add(hoursField, 1, 3);
        grid.add(new Label("Contact Phone:"), 0, 4); grid.add(phoneField, 1, 4);
        InputValidation.ValidatedField ratingValidation =
                InputValidation.attach(ratingField, InputValidation.Type.OPTIONAL_DOUBLE, "Rating");
        InputValidation.addRow(grid, 5, "Rating:", ratingField, ratingValidation);
        grid.add(new Label("Website:"), 0, 6); grid.add(websiteField, 1, 6);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(addressField, Priority.ALWAYS);
        GridPane.setHgrow(ownerField, Priority.ALWAYS);
        GridPane.setHgrow(hoursField, Priority.ALWAYS);
        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(websiteField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        InputValidation.bindOkButton(dialog, ratingValidation);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) { showError("Validation", "Name is required."); return false; }
        gallery.setName(name);
        gallery.setAddress(addressField.getText());
        gallery.setOwnerName(ownerField.getText());
        gallery.setOpeningHours(hoursField.getText());
        gallery.setContactPhone(phoneField.getText());
        String rate = ratingField.getText() == null ? "" : ratingField.getText().trim();
        gallery.setRating(rate.isEmpty() ? 0.0 : InputValidation.parseRequiredDouble(ratingField));
        gallery.setWebsite(websiteField.getText());
        return true;
    }

    private void showError(String header, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(message);
        a.showAndWait();
    }
}
