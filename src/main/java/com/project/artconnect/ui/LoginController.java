package com.project.artconnect.ui;

import com.project.artconnect.security.AuthSession;
import com.project.artconnect.security.UserRole;
import com.project.artconnect.config.DatabaseConfig;
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
            boolean isMember = role == UserRole.MEMBER;
            boolean isOrganizerOrAdmin = role == UserRole.ORGANIZER || role == UserRole.ADMIN;
            boolean isAdmin = role == UserRole.ADMIN;
            emailField.setVisible(isMember);
            emailField.setManaged(isMember);
            emailLabel.setVisible(isMember);
            emailLabel.setManaged(isMember);

            passwordField.setVisible(isMember || isOrganizerOrAdmin);
            passwordField.setManaged(isMember || isOrganizerOrAdmin);
            passwordLabel.setVisible(isMember || isOrganizerOrAdmin);
            passwordLabel.setManaged(isMember || isOrganizerOrAdmin);
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

        // Validate credentials for MEMBER, ORGANIZER and ADMIN
        if (role == UserRole.ADMIN) {
            String pwd = passwordField.getText() == null ? "" : passwordField.getText();
            if (pwd.isEmpty()) {
                showAlert("Connexion", "Mot de passe requis", "Veuillez saisir le mot de passe administrateur.");
                return false;
            }
            if (!pwd.equals(DatabaseConfig.getAdminPassword())) {
                showAlert("Connexion", "Échec de l'authentification", "Mot de passe administrateur invalide.");
                return false;
            }
            AuthSession.get().setRole(UserRole.ADMIN);
            AuthSession.get().setMemberEmail("");
            return true;
        }

        if (role == UserRole.ORGANIZER) {
            String pwd = passwordField.getText() == null ? "" : passwordField.getText();
            if (pwd.isEmpty()) {
                showAlert("Connexion", "Mot de passe requis", "Veuillez saisir le mot de passe organisateur.");
                return false;
            }
            if (!pwd.equals(DatabaseConfig.getOrganizerPassword())) {
                showAlert("Connexion", "Échec de l'authentification", "Mot de passe organisateur invalide.");
                return false;
            }
            AuthSession.get().setRole(UserRole.ORGANIZER);
            AuthSession.get().setMemberEmail("");
            return true;
        }

        if (role == UserRole.MEMBER) {
            String email = emailField.getText() == null ? "" : emailField.getText().trim();
            String pwd = passwordField.getText() == null ? "" : passwordField.getText();
            if (email.isEmpty()) {
                showAlert("Connexion", "E-mail requis", "Les membres doivent saisir un e-mail pour s'inscrire ou se connecter.");
                return false;
            }
            if (pwd.isEmpty()) {
                showAlert("Connexion", "Mot de passe requis", "Veuillez saisir le mot de passe membre.");
                return false;
            }
            var cs = com.project.artconnect.util.ServiceProvider.getCommunityService();
            var opt = cs.getMemberByEmail(email);
            if (opt.isPresent()) {
                var member = opt.get();
                if (!pwd.equals(member.getPassword())) {
                    showAlert("Connexion", "Échec de l'authentification", "Mot de passe membre invalide.");
                    return false;
                }
                AuthSession.get().setRole(UserRole.MEMBER);
                AuthSession.get().setMemberEmail(email);
                return true;
            } else {
                // offer to create account
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                a.setTitle("Créer un compte");
                a.setHeaderText("Aucun compte trouvé pour " + email);
                a.setContentText("Voulez-vous créer un compte membre avec cet e-mail ?");
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
                    AuthSession.get().setRole(UserRole.MEMBER);
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
