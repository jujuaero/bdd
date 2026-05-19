package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.security.AuthSession;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.security.UserRole;
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
import javafx.scene.layout.HBox;
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
    @FXML
    private HBox crudToolbar;

    private final CommunityService communityService = ServiceProvider.getCommunityService();
    private final DisciplineService disciplineService = ServiceProvider.getDisciplineService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));

        UiPermissions.applyCrudToolbar(crudToolbar, Permissions.Resource.COMMUNITY);
        refreshData();

        // Setup disciplines table
        disciplineNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        disciplinesTable.setItems(FXCollections.observableArrayList(disciplineService.getAllDisciplines()));
    }

    @FXML
    private void handleRefresh() { refreshData(); }

    @FXML
    private void handleAddMember() {
        if (!UiPermissions.checkCreate(Permissions.Resource.COMMUNITY)) return;
        CommunityMember draft = new CommunityMember();
        if (AuthSession.get().getRole() == UserRole.ARTIST) {
            draft.setEmail(AuthSession.get().getMemberEmail());
        }
        if (showMemberDialog("Add Member", draft, false)) {
            try { communityService.createMember(draft); refreshData(); }
            catch (Exception e) { showError("Add member failed", e.getMessage()); }
        }
    }

    @FXML
    private void handleEditMember() {
        if (!UiPermissions.checkUpdate(Permissions.Resource.COMMUNITY)) return;
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select a member to edit."); return; }
        if (AuthSession.get().getRole() == UserRole.ARTIST
                && !selected.getEmail().equalsIgnoreCase(AuthSession.get().getMemberEmail())) {
            showError("Accès refusé", "En tant qu'artiste, vous ne pouvez modifier que votre propre fiche.");
            return;
        }
        CommunityMember edited = new CommunityMember();
        edited.setId(selected.getId());
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
        if (!UiPermissions.checkDelete(Permissions.Resource.COMMUNITY)) return;
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("No selection", "Please select a member to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Member");
        confirm.setHeaderText("Delete member: " + selected.getName());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try { communityService.deleteMember(selected.getId()); refreshData(); }
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
        InputValidation.ValidatedField emailValidation =
                InputValidation.attach(emailField, InputValidation.Type.EMAIL, "Email");
        InputValidation.ValidatedField yearValidation =
                InputValidation.attach(yearField, InputValidation.Type.OPTIONAL_INTEGER, "Birth Year");
        InputValidation.addRow(grid, 1, "Email:", emailField, emailValidation);
        InputValidation.addRow(grid, 2, "Birth Year:", yearField, yearValidation);
        grid.add(new Label("Phone:"), 0, 3); grid.add(phoneField, 1, 3);
        grid.add(new Label("City:"), 0, 4); grid.add(cityField, 1, 4);
        grid.add(new Label("Membership Type:"), 0, 5); grid.add(membershipField, 1, 5);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(phoneField, Priority.ALWAYS);
        GridPane.setHgrow(cityField, Priority.ALWAYS);
        GridPane.setHgrow(membershipField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);
        InputValidation.bindOkButton(dialog, emailValidation, yearValidation);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return false;
        String n = nameField.getText() == null ? "" : nameField.getText().trim();
        if (n.isEmpty()) { showError("Validation", "Name is required."); return false; }
        member.setName(n);
        member.setEmail(InputValidation.parseRequiredEmail(emailField));
        member.setPhone(phoneField.getText());
        member.setCity(cityField.getText());
        member.setMembershipType(membershipField.getText());
        member.setBirthYear(InputValidation.parseOptionalInt(yearField));
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


