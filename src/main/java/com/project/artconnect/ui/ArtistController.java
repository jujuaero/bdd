package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;
    @FXML
    private HBox crudToolbar;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        UiPermissions.applyCrudToolbar(crudToolbar, Permissions.Resource.ARTISTS);
        refreshTable();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    @FXML
    private void handleAddArtist() {
        if (!UiPermissions.checkCreate(Permissions.Resource.ARTISTS)) return;
        Artist draft = new Artist();
        if (showArtistDialog("Add Artist", draft, false)) {
            try {
                artistService.createArtist(draft);
                refreshTable();
            } catch (Exception e) {
                showError("Add artist failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditArtist() {
        if (!UiPermissions.checkUpdate(Permissions.Resource.ARTISTS)) return;
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select an artist to edit.");
            return;
        }
        Artist edited = new Artist();
        edited.setName(selected.getName());
        edited.setBio(selected.getBio());
        edited.setBirthYear(selected.getBirthYear());
        edited.setContactEmail(selected.getContactEmail());
        edited.setPhone(selected.getPhone());
        edited.setCity(selected.getCity());
        edited.setWebsite(selected.getWebsite());
        edited.setSocialMedia(selected.getSocialMedia());
        edited.setActive(selected.isActive());

        if (showArtistDialog("Edit Artist", edited, true)) {
            try {
                artistService.updateArtist(edited);
                refreshTable();
            } catch (Exception e) {
                showError("Edit artist failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteArtist() {
        if (!UiPermissions.checkDelete(Permissions.Resource.ARTISTS)) return;
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select an artist to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Artist");
        confirm.setHeaderText("Delete artist: " + selected.getName());
        confirm.setContentText("This action cannot be undone.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                artistService.deleteArtist(selected.getName());
                refreshTable();
            } catch (Exception e) {
                showError("Delete artist failed", e.getMessage());
            }
        }
    }

    private boolean showArtistDialog(String title, Artist artist, boolean editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(artist.getName());
        TextField cityField = new TextField(artist.getCity());
        TextField emailField = new TextField(artist.getContactEmail());
        TextField yearField = new TextField(artist.getBirthYear() == null ? "" : artist.getBirthYear().toString());
        TextArea bioArea = new TextArea(artist.getBio());
        bioArea.setPrefRowCount(3);

        if (editing) {
            nameField.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("City:"), 0, 1);
        grid.add(cityField, 1, 1);
        InputValidation.ValidatedField emailValidation =
                InputValidation.attach(emailField, InputValidation.Type.OPTIONAL_EMAIL, "Email");
        InputValidation.ValidatedField yearValidation =
                InputValidation.attach(yearField, InputValidation.Type.OPTIONAL_INTEGER, "Birth Year");
        InputValidation.addRow(grid, 2, "Email:", emailField, emailValidation);
        InputValidation.addRow(grid, 3, "Birth Year:", yearField, yearValidation);
        grid.add(new Label("Bio:"), 0, 4);
        grid.add(bioArea, 1, 4);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(cityField, Priority.ALWAYS);
        GridPane.setHgrow(bioArea, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        InputValidation.bindOkButton(dialog, emailValidation, yearValidation);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return false;
        }

        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Validation", "Name is required.");
            return false;
        }
        artist.setName(name);
        artist.setCity(cityField.getText());
        artist.setContactEmail(InputValidation.parseOptionalEmail(emailField));
        artist.setBio(bioArea.getText());
        artist.setBirthYear(InputValidation.parseOptionalInt(yearField));
        if (!editing && artist.getWebsite() == null) artist.setWebsite("");
        if (!editing && artist.getPhone() == null) artist.setPhone("");
        if (!editing && artist.getSocialMedia() == null) artist.setSocialMedia("");
        if (!editing) artist.setActive(true);
        return true;
    }

    private void showError(String header, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(message);
        a.showAndWait();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }
}
