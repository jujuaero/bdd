package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.service.DisciplineService;
import com.project.artconnect.util.ServiceProvider;
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
import javafx.scene.layout.Priority;

public class CommunityController {
    @FXML
    private TableView<CommunityMember> memberTable;
    @FXML
    private TableColumn<CommunityMember, String> nameColumn;
    @FXML
    private TableColumn<CommunityMember, String> emailColumn;
    @FXML
    private TableColumn<CommunityMember, String> cityColumn;

    @FXML
    private TableView<Discipline> disciplinesTable;
    @FXML
    private TableColumn<Discipline, String> disciplineNameColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();
    private final DisciplineService disciplineService = ServiceProvider.getDisciplineService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        refreshData();

        // Setup disciplines table
        disciplineNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        disciplinesTable.setItems(FXCollections.observableArrayList(disciplineService.getAllDisciplines()));
    }

    @FXML
    private void handleRefresh() { refreshData(); }

    @FXML
    private void handleAddMember() {
        CommunityMember draft = new CommunityMember();
        if (showMemberDialog("Add Member", draft, false)) {
            try { communityService.createMember(draft); refreshData(); }
            catch (Exception e) { showError("Add member failed", e.getMessage()); }
        }
    }

    @FXML
    private void handleEditMember() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select a member to edit."); return; }
        CommunityMember edited = new CommunityMember();
        edited.setName(selected.getName());
        edited.setEmail(selected.getEmail());
        edited.setBirthYear(selected.getBirthYear());
        edited.setPhone(selected.getPhone());
        edited.setCity(selected.getCity());
        edited.setMembershipType(selected.getMembershipType());
        if (showMemberDialog("Edit Member", edited, true)) {
            try { communityService.updateMember(edited); refreshData(); }
            catch (Exception e) { showError("Edit member failed", e.getMessage()); }
        }
    }

    @FXML
    private void handleDeleteMember() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select a member to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Member");
        confirm.setHeaderText("Delete member: " + selected.getName());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try { communityService.deleteMember(selected.getName()); refreshData(); }
            catch (Exception e) { showError("Delete member failed", e.getMessage()); }
        }
    }

    private void refreshData() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    private boolean showMemberDialog(String title, CommunityMember member, boolean editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(member.getName());
        TextField emailField = new TextField(member.getEmail());
        TextField yearField = new TextField(member.getBirthYear() == null ? "" : member.getBirthYear().toString());
        TextField phoneField = new TextField(member.getPhone());
        TextField cityField = new TextField(member.getCity());
        TextField membershipField = new TextField(member.getMembershipType());
        if (editing) nameField.setDisable(true);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Birth Year:"), 0, 2); grid.add(yearField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("City:"), 0, 4); grid.add(cityField, 1, 4);
        grid.add(new Label("Membership Type:"), 0, 5); grid.add(membershipField, 1, 5);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(emailField, Priority.ALWAYS);
        GridPane.setHgrow(yearField, Priority.ALWAYS);
        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(cityField, Priority.ALWAYS);
        GridPane.setHgrow(membershipField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;
        String n = nameField.getText() == null ? "" : nameField.getText().trim();
        String e = emailField.getText() == null ? "" : emailField.getText().trim();
        if (n.isEmpty() || e.isEmpty()) { showError("Validation", "Name and Email are required."); return false; }
        member.setName(n);
        member.setEmail(e);
        member.setPhone(phoneField.getText());
        member.setCity(cityField.getText());
        member.setMembershipType(membershipField.getText());
        String y = yearField.getText() == null ? "" : yearField.getText().trim();
        if (!y.isEmpty()) {
            try { member.setBirthYear(Integer.parseInt(y)); }
            catch (NumberFormatException ex) { showError("Validation", "Birth Year must be numeric."); return false; }
        } else member.setBirthYear(null);
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


