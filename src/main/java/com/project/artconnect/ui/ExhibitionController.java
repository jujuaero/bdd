package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ExhibitionController {
    @FXML
    private TableView<Exhibition> exhibitionTable;
    @FXML
    private TableColumn<Exhibition, String> titleColumn;
    @FXML
    private TableColumn<Exhibition, LocalDate> dateColumn;
    @FXML
    private TableColumn<Exhibition, String> themeColumn;
    @FXML
    private TableColumn<Exhibition, String> galleryColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));

        galleryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getGallery() != null ? cellData.getValue().getGallery().getName() : "Unknown"));

        refreshData();
    }

    @FXML
    public void handleRefresh() { refreshData(); }

    @FXML
    public void handleAddExhibition() {
        Exhibition draft = new Exhibition();
        if (showExhibitionDialog("Add Exhibition", draft, false)) {
            try { galleryService.createExhibition(draft); refreshData(); }
            catch (Exception e) { showError("Add exhibition failed", e.getMessage()); }
        }
    }

    @FXML
    public void handleEditExhibition() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select an exhibition to edit."); return; }
        Exhibition edited = new Exhibition();
        edited.setTitle(selected.getTitle());
        edited.setStartDate(selected.getStartDate());
        edited.setEndDate(selected.getEndDate());
        edited.setDescription(selected.getDescription());
        edited.setCuratorName(selected.getCuratorName());
        edited.setTheme(selected.getTheme());
        edited.setGallery(selected.getGallery());
        if (showExhibitionDialog("Edit Exhibition", edited, true)) {
            try { galleryService.updateExhibition(edited); refreshData(); }
            catch (Exception e) { showError("Edit exhibition failed", e.getMessage()); }
        }
    }

    @FXML
    public void handleDeleteExhibition() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select an exhibition to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Exhibition");
        confirm.setHeaderText("Delete exhibition: " + selected.getTitle());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try { galleryService.deleteExhibition(selected.getTitle()); refreshData(); }
            catch (Exception e) { showError("Delete exhibition failed", e.getMessage()); }
        }
    }

    private void refreshData() {
        List<Exhibition> all = new ArrayList<>();
        for (Gallery g : galleryService.getAllGalleries()) {
            all.addAll(g.getExhibitions());
        }
        exhibitionTable.setItems(FXCollections.observableArrayList(all));
    }

    private boolean showExhibitionDialog(String title, Exhibition exhibition, boolean editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField titleField = new TextField(exhibition.getTitle());
        DatePicker startPicker = new DatePicker(exhibition.getStartDate());
        DatePicker endPicker = new DatePicker(exhibition.getEndDate());
        TextField curatorField = new TextField(exhibition.getCuratorName());
        TextField themeField = new TextField(exhibition.getTheme());
        TextField descField = new TextField(exhibition.getDescription());
        ComboBox<Gallery> galleryBox = new ComboBox<>(FXCollections.observableArrayList(galleryService.getAllGalleries()));
        galleryBox.setPrefWidth(260);
        if (exhibition.getGallery() != null) galleryBox.setValue(exhibition.getGallery());
        if (editing) titleField.setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.add(new Label("Title:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Gallery:"), 0, 1); grid.add(galleryBox, 1, 1);
        grid.add(new Label("Start Date:"), 0, 2); grid.add(startPicker, 1, 2);
        grid.add(new Label("End Date:"), 0, 3); grid.add(endPicker, 1, 3);
        grid.add(new Label("Curator:"), 0, 4); grid.add(curatorField, 1, 4);
        grid.add(new Label("Theme:"), 0, 5); grid.add(themeField, 1, 5);
        grid.add(new Label("Description:"), 0, 6); grid.add(descField, 1, 6);
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(curatorField, Priority.ALWAYS);
        GridPane.setHgrow(themeField, Priority.ALWAYS);
        GridPane.setHgrow(descField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;

        String t = titleField.getText() == null ? "" : titleField.getText().trim();
        if (t.isEmpty() || galleryBox.getValue() == null) { showError("Validation", "Title and Gallery are required."); return false; }
        exhibition.setTitle(t);
        exhibition.setGallery(galleryBox.getValue());
        exhibition.setStartDate(startPicker.getValue());
        exhibition.setEndDate(endPicker.getValue());
        exhibition.setCuratorName(curatorField.getText());
        exhibition.setTheme(themeField.getText());
        exhibition.setDescription(descField.getText());
        if (exhibition.getStartDate() != null && exhibition.getEndDate() != null && exhibition.getEndDate().isBefore(exhibition.getStartDate())) {
            showError("Validation", "End date must be after or equal to start date.");
            return false;
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
