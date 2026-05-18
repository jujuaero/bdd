package com.project.artconnect.ui;

import com.project.artconnect.security.AuthSession;
import com.project.artconnect.security.UserRole;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private ComboBox<UserRole> roleCombo;
    @FXML
    private TextField emailField;
    @FXML
    private Label emailLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.setValue(UserRole.VISITOR);
        roleCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(UserRole role) {
                return role == null ? "" : role.getDisplayName();
            }

            @Override
            public UserRole fromString(String string) {
                return null;
            }
        });
        roleCombo.valueProperty().addListener((obs, oldVal, role) -> {
            boolean user = role == UserRole.USER;
            emailField.setVisible(user);
            emailField.setManaged(user);
            emailLabel.setVisible(user);
            emailLabel.setManaged(user);
        });
        emailField.setVisible(false);
        emailField.setManaged(false);
        emailLabel.setVisible(false);
        emailLabel.setManaged(false);
    }

    public boolean applySession() {
        UserRole role = roleCombo.getValue() == null ? UserRole.VISITOR : roleCombo.getValue();
        AuthSession.get().setRole(role);
        if (role == UserRole.USER) {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            if (email.isEmpty()) {
                return false;
            }
            AuthSession.get().setMemberEmail(email);
        } else {
            AuthSession.get().setMemberEmail("");
        }
        return true;
    }
}
