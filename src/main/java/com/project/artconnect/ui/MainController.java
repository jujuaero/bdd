package com.project.artconnect.ui;

import com.project.artconnect.security.AuthSession;
import com.project.artconnect.security.UserRole;
import javafx.fxml.FXML;

public class MainController {

    @FXML
    private javafx.scene.control.Label modeLabel;

    @FXML
    public void initialize() {
        updateStatusBar();
        AuthSession.get().roleProperty().addListener((obs, oldRole, newRole) -> updateStatusBar());
    }

    private void updateStatusBar() {
        boolean usingJdbc = com.project.artconnect.util.ServiceProvider.getArtistService()
                .getClass().getSimpleName().startsWith("Jdbc");
        String mode = usingJdbc ? "JDBC" : "In-Memory";
        UserRole role = AuthSession.get().getRole();
        String roleText = role.getDisplayName();
        if (role == UserRole.ARTIST && !AuthSession.get().getMemberEmail().isBlank()) {
            roleText += " (" + AuthSession.get().getMemberEmail() + ")";
        }
        if (modeLabel != null) {
            modeLabel.setText("ArtConnect Pro | " + roleText + " | Mode: " + mode);
        }
    }

    @FXML
    private void handleSwitchAccount() {
        if (LoginDialog.show()) {
            updateStatusBar();
        }
    }

    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
    }
}
