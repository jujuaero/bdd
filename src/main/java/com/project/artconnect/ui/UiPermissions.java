package com.project.artconnect.ui;

import com.project.artconnect.security.AuthSession;
import com.project.artconnect.security.Permissions;
import com.project.artconnect.security.UserRole;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * Applies role-based visibility/disable state to CRUD toolbars.
 */
public final class UiPermissions {

    private UiPermissions() {}

    public static void applyCrudToolbar(HBox toolbar, Permissions.Resource resource) {
        if (toolbar == null) return;
        Runnable update = () -> updateToolbar(toolbar, resource);
        update.run();
        AuthSession.get().roleProperty().addListener((obs, oldRole, newRole) -> update.run());
    }

    private static void updateToolbar(HBox toolbar, Permissions.Resource resource) {
        for (Node node : toolbar.getChildren()) {
            if (!(node instanceof Button button)) continue;
            String text = button.getText();
            if (text == null) continue;
            switch (text) {
                case "Add" -> button.setDisable(!Permissions.canCreate(resource));
                case "Edit" -> button.setDisable(!Permissions.canUpdate(resource));
                case "Delete" -> button.setDisable(!Permissions.canDelete(resource));
                default -> { /* Search, Reset, Refresh stay enabled */ }
            }
        }
    }

    public static boolean checkCreate(Permissions.Resource resource) {
        if (Permissions.canCreate(resource)) return true;
        showDenied();
        return false;
    }

    public static boolean checkUpdate(Permissions.Resource resource) {
        if (Permissions.canUpdate(resource)) return true;
        showDenied();
        return false;
    }

    public static boolean checkDelete(Permissions.Resource resource) {
        if (Permissions.canDelete(resource)) return true;
        showDenied();
        return false;
    }

    public static void showDenied() {
        UserRole role = AuthSession.get().getRole();
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Accès refusé");
        alert.setHeaderText("Action non autorisée");
        alert.setContentText(switch (role) {
            case VISITOR -> "En tant que visiteur, vous pouvez uniquement consulter les données. "
                    + "Connectez-vous en tant qu'utilisateur ou administrateur via le menu Compte.";
            case USER -> "En tant qu'utilisateur, vous pouvez vous inscrire et gérer les membres de la communauté. "
                    + "Les autres modifications nécessitent un compte administrateur.";
            case ADMIN -> "Cette action n'est pas disponible.";
        });
        alert.showAndWait();
    }
}
