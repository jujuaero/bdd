package com.project.artconnect.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;
import java.util.Optional;

public final class LoginDialog {

    private LoginDialog() {}

    public static boolean show() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    LoginDialog.class.getResource("/com/project/artconnect/ui/LoginView.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Connexion");
            dialog.setHeaderText("Sélectionnez votre rôle");
            loader.load();
            LoginController controller = loader.getController();
            dialog.getDialogPane().setContent(loader.getRoot());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(
                    LoginDialog.class.getResource("/com/project/artconnect/ui/artconnect.css").toExternalForm());

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return false;
            }
            if (!controller.applySession()) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Connexion");
                a.setHeaderText("E-mail requis");
                a.setContentText("Les utilisateurs doivent saisir un e-mail pour s'inscrire ou se connecter.");
                a.showAndWait();
                return show();
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load login dialog", e);
        }
    }
}
