package com.project.artconnect.ui;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.BookingService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class WorkshopController {
    @FXML
    public TableView<Workshop> workshopTable;
    @FXML
    public TableColumn<Workshop, String> titleColumn;
    @FXML
    public TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML
    public TableColumn<Workshop, String> instructorColumn;
    @FXML
    public TableColumn<Workshop, Double> priceColumn;
    @FXML
    public TableColumn<Workshop, String> levelColumn;

    @FXML
    public TableView<Booking> bookingsTable;
    @FXML
    public TableColumn<Booking, String> memberNameColumn;
    @FXML
    public TableColumn<Booking, LocalDateTime> bookingDateColumn;
    @FXML
    public TableColumn<Booking, String> paymentStatusColumn;
    @FXML
    private HBox crudToolbar;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final BookingService bookingService = ServiceProvider.getBookingService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));

        instructorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstructor() != null ? cellData.getValue().getInstructor().getName()
                        : "Unknown"));

        UiPermissions.applyCrudToolbar(crudToolbar, Permissions.Resource.WORKSHOPS);
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));

        // Setup bookings table
        memberNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMember() != null ? cellData.getValue().getMember().getName() : "Unknown"));
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        // Listen to selection changes
        workshopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateBookings(newVal);
            }
        });
    }

    @FXML
    private void handleRefresh() { refreshData(); }

    @FXML
    private void handleAddWorkshop() {
        if (!UiPermissions.checkCreate(Permissions.Resource.WORKSHOPS)) return;
        Workshop draft = new Workshop();
        if (showWorkshopDialog("Add Workshop", draft, false)) {
            try { workshopService.createWorkshop(draft); refreshData(); }
            catch (Exception e) { showError("Add workshop failed", e.getMessage()); }
        }
    }

    @FXML
    private void handleEditWorkshop() {
        if (!UiPermissions.checkUpdate(Permissions.Resource.WORKSHOPS)) return;
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select a workshop to edit."); return; }
        Workshop edited = new Workshop();
        edited.setTitle(selected.getTitle());
        edited.setDate(selected.getDate());
        edited.setDurationMinutes(selected.getDurationMinutes());
        edited.setMaxParticipants(selected.getMaxParticipants());
        edited.setPrice(selected.getPrice());
        edited.setInstructor(selected.getInstructor());
        edited.setLocation(selected.getLocation());
        edited.setDescription(selected.getDescription());
        edited.setLevel(selected.getLevel());
        if (showWorkshopDialog("Edit Workshop", edited, true)) {
            try { workshopService.updateWorkshop(edited); refreshData(); }
            catch (Exception e) { showError("Edit workshop failed", e.getMessage()); }
        }
    }

    @FXML
    private void handleDeleteWorkshop() {
        if (!UiPermissions.checkDelete(Permissions.Resource.WORKSHOPS)) return;
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select a workshop to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Workshop");
        confirm.setHeaderText("Delete workshop: " + selected.getTitle());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try { workshopService.deleteWorkshop(selected.getTitle()); refreshData(); }
            catch (Exception e) { showError("Delete workshop failed", e.getMessage()); }
        }
    }

    private void refreshData() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    private boolean showWorkshopDialog(String title, Workshop workshop, boolean editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(workshop.getTitle());
        TextField dateField = new TextField(workshop.getDate() == null ? "" : workshop.getDate().toString());
        TextField durationField = new TextField(workshop.getDurationMinutes() == 0 ? "" : Integer.toString(workshop.getDurationMinutes()));
        TextField maxField = new TextField(workshop.getMaxParticipants() == 0 ? "" : Integer.toString(workshop.getMaxParticipants()));
        TextField priceField = new TextField(workshop.getPrice() == 0.0 ? "" : Double.toString(workshop.getPrice()));
        TextField locationField = new TextField(workshop.getLocation());
        TextField descField = new TextField(workshop.getDescription());
        TextField levelField = new TextField(workshop.getLevel());
        ComboBox<String> artistBox = new ComboBox<>(FXCollections.observableArrayList(
                artistService.getAllArtists().stream().map(Artist::getName).toList()));
        if (workshop.getInstructor() != null) artistBox.setValue(workshop.getInstructor().getName());
        if (editing) titleField.setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Instructor:"), 0, 1); grid.add(artistBox, 1, 1);
        grid.add(new Label("DateTime (yyyy-MM-ddTHH:mm):"), 0, 2); grid.add(dateField, 1, 2);
        InputValidation.ValidatedField durationValidation =
                InputValidation.attach(durationField, InputValidation.Type.POSITIVE_INTEGER, "Duration");
        InputValidation.ValidatedField maxValidation =
                InputValidation.attach(maxField, InputValidation.Type.POSITIVE_INTEGER, "Max Participants");
        InputValidation.ValidatedField priceValidation =
                InputValidation.attach(priceField, InputValidation.Type.POSITIVE_DOUBLE, "Price");
        InputValidation.addRow(grid, 3, "Duration (min):", durationField, durationValidation);
        InputValidation.addRow(grid, 4, "Max Participants:", maxField, maxValidation);
        InputValidation.addRow(grid, 5, "Price:", priceField, priceValidation);
        grid.add(new Label("Location:"), 0, 6); grid.add(locationField, 1, 6);
        grid.add(new Label("Description:"), 0, 7); grid.add(descField, 1, 7);
        grid.add(new Label("Level:"), 0, 8); grid.add(levelField, 1, 8);
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(dateField, Priority.ALWAYS);
        GridPane.setHgrow(locationField, Priority.ALWAYS);
        GridPane.setHgrow(descField, Priority.ALWAYS);
        GridPane.setHgrow(levelField, Priority.ALWAYS);
        GridPane.setHgrow(artistBox, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        InputValidation.bindOkButton(dialog, durationValidation, maxValidation, priceValidation);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;

        String t = titleField.getText() == null ? "" : titleField.getText().trim();
        if (t.isEmpty() || artistBox.getValue() == null) { showError("Validation", "Title and Instructor are required."); return false; }
        try {
            workshop.setDate(LocalDateTime.parse(dateField.getText().trim()));
        } catch (DateTimeParseException e) {
            showError("Validation", "DateTime must be in format yyyy-MM-ddTHH:mm.");
            return false;
        }
        workshop.setDurationMinutes(InputValidation.parseRequiredInt(durationField));
        workshop.setMaxParticipants(InputValidation.parseRequiredInt(maxField));
        workshop.setPrice(InputValidation.parseRequiredDouble(priceField));
        Artist artist = artistService.getArtistByName(artistBox.getValue()).orElse(null);
        if (artist == null) { showError("Validation", "Selected instructor does not exist."); return false; }
        workshop.setTitle(t);
        workshop.setInstructor(artist);
        workshop.setLocation(locationField.getText());
        workshop.setDescription(descField.getText());
        workshop.setLevel(levelField.getText());
        return true;
    }

    private void showError(String header, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(header);
        a.setContentText(message);
        a.showAndWait();
    }

    private void refreshBookingsForSelection() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected != null) updateBookings(selected);
    }

    private void updateBookings(Workshop workshop) {
        bookingsTable.setItems(FXCollections.observableArrayList(bookingService.findByWorkshopTitle(workshop.getTitle())));
    }
}

