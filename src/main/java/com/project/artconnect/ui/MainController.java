package com.project.artconnect.ui;

import javafx.fxml.FXML;

public class MainController {

    @FXML
    private javafx.scene.control.Label modeLabel;

    @FXML
    public void initialize() {
        boolean usingJdbc = com.project.artconnect.util.ServiceProvider.getArtistService().getClass().getSimpleName().startsWith("Jdbc");
        String mode = usingJdbc ? "JDBC (Database)" : "In-Memory";
        if (modeLabel != null) {
            modeLabel.setText("ArtConnect Pro v1.0 | Mode: " + mode);
        }
        System.out.println("=== ArtConnect Pro Started ===");
        System.out.println("Mode effective: " + mode);
        System.out.println("=============================");
    }

    @FXML
    private void handleExit() {
        javafx.application.Platform.exit();
    }
}


