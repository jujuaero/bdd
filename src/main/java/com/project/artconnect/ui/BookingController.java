package com.project.artconnect.ui;

import com.project.artconnect.model.Booking;
import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.service.BookingService;
import com.project.artconnect.service.CommunityService;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import java.time.LocalDateTime;

public class BookingController {
    @FXML
    private TableView<Booking> bookingTable;
    @FXML
    private TableColumn<Booking, String> memberNameColumn;
    @FXML
    private TableColumn<Booking, String> workshopTitleColumn;
    @FXML
    private TableColumn<Booking, LocalDateTime> bookingDateColumn;
    @FXML
    private TableColumn<Booking, String> paymentStatusColumn;
    @FXML
    private HBox crudToolbar;

    private final BookingService bookingService = ServiceProvider.getBookingService();
    private final CommunityService communityService = ServiceProvider.getCommunityService();
    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();

    @FXML
    public void initialize() {
        memberNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMember() != null ? cellData.getValue().getMember().getName() : "Unknown"));
        workshopTitleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getWorkshop() != null ? cellData.getValue().getWorkshop().getTitle() : "Unknown"));
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
        paymentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        UiPermissions.applyCrudToolbar(crudToolbar, Permissions.Resource.BOOKINGS);
        refreshData();
    }

    @FXML
    public void handleRefresh() {
        refreshData();
    }

    @FXML
    public void handleAddBooking() {
        if (!UiPermissions.checkCreate(Permissions.Resource.BOOKINGS)) return;
        Booking draft = new Booking();
        if (showBookingDialog("Add Booking", draft, false)) {
            try {
                bookingService.createBooking(draft);
                refreshData();
            } catch (Exception e) {
                showError("Add booking failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handleEditBooking() {
        if (!UiPermissions.checkUpdate(Permissions.Resource.BOOKINGS)) return;
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select a booking to edit.");
            return;
        }
        Booking edited = new Booking();
        edited.setId(selected.getId());
        edited.setMember(selected.getMember());
        edited.setWorkshop(selected.getWorkshop());
        edited.setBookingDate(selected.getBookingDate());
        edited.setPaymentStatus(selected.getPaymentStatus());
        if (showBookingDialog("Edit Booking", edited, true)) {
            try {
                bookingService.updateBooking(edited);
                refreshData();
            } catch (Exception e) {
                showError("Edit booking failed", e.getMessage());
            }
        }
    }

    @FXML
    public void handleDeleteBooking() {
        if (!UiPermissions.checkDelete(Permissions.Resource.BOOKINGS)) return;
        Booking selected = bookingTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No selection", "Please select a booking to delete.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Booking");
        confirm.setHeaderText("Delete booking for: " +
            (selected.getMember() != null ? selected.getMember().getName() : "Unknown"));
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                bookingService.deleteBooking(selected.getId());
                refreshData();
            } catch (Exception e) {
                showError("Delete booking failed", e.getMessage());
            }
        }
    }

    private void refreshData() {
        bookingTable.setItems(FXCollections.observableArrayList(bookingService.getAllBookings()));
    }

    private boolean showBookingDialog(String title, Booking booking, boolean editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<CommunityMember> memberBox = new ComboBox<>(FXCollections.observableArrayList(communityService.getAllMembers()));
        ComboBox<Workshop> workshopBox = new ComboBox<>(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
        ComboBox<String> paymentBox = new ComboBox<>(FXCollections.observableArrayList("PENDING", "PAID", "CANCELLED"));

        if (booking.getMember() != null) {
            memberBox.setValue(communityService.getAllMembers().stream()
                    .filter(m -> m.getId() != null && m.getId().equals(booking.getMember().getId()))
                    .findFirst().orElse(booking.getMember()));
        }
        if (booking.getWorkshop() != null) {
            workshopBox.setValue(workshopService.getAllWorkshops().stream()
                    .filter(w -> w.getId() != null && w.getId().equals(booking.getWorkshop().getId()))
                    .findFirst().orElse(booking.getWorkshop()));
        }
        if (booking.getPaymentStatus() != null) {
            paymentBox.setValue(booking.getPaymentStatus());
        } else {
            paymentBox.setValue("PENDING");
        }

        memberBox.setPrefWidth(260);
        workshopBox.setPrefWidth(260);
        paymentBox.setPrefWidth(260);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.add(new Label("Member:"), 0, 0);
        grid.add(memberBox, 1, 0);
        grid.add(new Label("Workshop:"), 0, 1);
        grid.add(workshopBox, 1, 1);
        grid.add(new Label("Payment Status:"), 0, 2);
        grid.add(paymentBox, 1, 2);

        GridPane.setHgrow(memberBox, Priority.ALWAYS);
        GridPane.setHgrow(workshopBox, Priority.ALWAYS);
        GridPane.setHgrow(paymentBox, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;

        if (memberBox.getValue() == null || workshopBox.getValue() == null) {
            showError("Validation", "Member and Workshop are required.");
            return false;
        }

        booking.setMember(memberBox.getValue());
        booking.setWorkshop(workshopBox.getValue());
        booking.setPaymentStatus(paymentBox.getValue() != null ? paymentBox.getValue() : "PENDING");
        if (booking.getBookingDate() == null) {
            booking.setBookingDate(LocalDateTime.now());
        }
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

