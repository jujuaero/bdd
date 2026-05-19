package com.project.artconnect.ui;

import com.project.artconnect.security.AuthSession;
import com.project.artconnect.security.UserRole;
import com.project.artconnect.util.DatabaseConfig;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private ComboBox<UserRole> roleCombo;
    @FXML
    private TextField emailField;
    @FXML
    private Label emailLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordLabel;

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
            boolean isUser = role == UserRole.USER;
            boolean isAdmin = role == UserRole.ADMIN;
            emailField.setVisible(isUser);
            emailField.setManaged(isUser);
            emailLabel.setVisible(isUser);
            emailLabel.setManaged(isUser);

            passwordField.setVisible(isUser || isAdmin);
            passwordField.setManaged(isUser || isAdmin);
            passwordLabel.setVisible(isUser || isAdmin);
            passwordLabel.setManaged(isUser || isAdmin);
        });
        // initial state
        emailField.setVisible(false);
        emailField.setManaged(false);
        emailLabel.setVisible(false);
        emailLabel.setManaged(false);
        passwordField.setVisible(false);
        passwordField.setManaged(false);
        passwordLabel.setVisible(false);
        passwordLabel.setManaged(false);
    }

    public boolean applySession() {
        UserRole role = roleCombo.getValue() == null ? UserRole.VISITOR : roleCombo.getValue();

        // Validate credentials for USER and ADMIN
        if (role == UserRole.ADMIN) {
            String pwd = passwordField.getText() == null ? "" : passwordField.getText();
            if (pwd.isEmpty()) {
                showAlert("Connexion", "Mot de passe requis", "Veuillez saisir le mot de passe administrateur.");
                return false;
            }
            // admin uses configured admin password
            if (!pwd.equals(DatabaseConfig.ADMIN_PASSWORD)) {
                showAlert("Connexion", "Échec de l'authentification", "Mot de passe administrateur invalide.");
                return false;
            }
            AuthSession.get().setRole(UserRole.ADMIN);
            AuthSession.get().setMemberEmail("");
            return true;
        }

        if (role == UserRole.USER) {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pwd = passwordField.getText() == null ? "" : passwordField.getText();
            if (email.isEmpty()) {
                showAlert("Connexion", "E-mail requis", "Les utilisateurs doivent saisir un e-mail pour s'inscrire ou se connecter.");
                return false;
            }
            if (pwd.isEmpty()) {
                showAlert("Connexion", "Mot de passe requis", "Veuillez saisir le mot de passe utilisateur.");
                return false;
            }
            // authenticate against stored community members (create account if absent)
            var cs = com.project.artconnect.util.ServiceProvider.getCommunityService();
            var opt = cs.getMemberByEmail(email);
            if (opt.isPresent()) {
                var member = opt.get();
                if (!pwd.equals(member.getPassword())) {
                    showAlert("Connexion", "Échec de l'authentification", "Mot de passe utilisateur invalide.");
                    return false;
                }
                AuthSession.get().setRole(UserRole.USER);
                AuthSession.get().setMemberEmail(email);
                return true;
            } else {
                // offer to create account
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                a.setTitle("Créer un compte");
                a.setHeaderText("Aucun compte trouvé pour " + email);
                a.setContentText("Voulez-vous créer un compte avec cet e-mail ?");
                var res = a.showAndWait();
                if (res.isPresent() && res.get() == javafx.scene.control.ButtonType.OK) {
                    com.project.artconnect.model.CommunityMember newMember = new com.project.artconnect.model.CommunityMember();
                    String name = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                    newMember.setName(name);
                    newMember.setEmail(email);
                    newMember.setPassword(pwd);
                    // defaults
                    newMember.setMembershipType("Standard");
                    cs.createMember(newMember);
                    AuthSession.get().setRole(UserRole.USER);
                    AuthSession.get().setMemberEmail(email);
                    return true;
                }
                return false;
            }
        }

        // Visitor
        AuthSession.get().setRole(UserRole.VISITOR);
        AuthSession.get().setMemberEmail("");
        return true;
    }

    private void showAlert(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(header);
        a.setContentText(content);
        a.showAndWait();
    }
}
