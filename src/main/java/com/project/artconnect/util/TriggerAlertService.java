package com.project.artconnect.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Surfaces MySQL trigger messages: SIGNAL exceptions and rows written to alert_log.
 */
public final class TriggerAlertService {

    private static long lastAlertId = 0;

    private TriggerAlertService() {}

    /**
     * Call after a successful database mutation to show informational trigger alerts.
     */
    public static void showPendingAlerts() {
        if (!DatabaseConfig.USE_PERSISTENCE) return;
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id, message FROM alert_log WHERE id > ? ORDER BY id")) {
            ps.setLong(1, lastAlertId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String message = rs.getString("message");
                    lastAlertId = Math.max(lastAlertId, id);
                    showInfo("Alerte base de données", message);
                }
            }
        } catch (SQLException e) {
            // Table may not exist yet if triggers SQL was not applied
            if (!isMissingTable(e)) {
                System.err.println("[TriggerAlert] " + e.getMessage());
            }
        }
    }

    /**
     * Shows trigger SIGNAL messages (SQLSTATE 45000) or rethrows unexpected errors.
     */
    public static void handleMutationError(Exception e) throws Exception {
        if (e instanceof RuntimeException re && re.getCause() instanceof SQLException sql) {
            if (showSignalIfPresent(sql)) return;
        }
        if (e.getCause() instanceof SQLException sql) {
            if (showSignalIfPresent(sql)) return;
        }
        if (e instanceof SQLException sql) {
            if (showSignalIfPresent(sql)) return;
        }
        throw e;
    }

    public static String extractMessage(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLException sql && "45000".equals(sql.getSQLState())) {
                return sql.getMessage();
            }
            t = t.getCause();
        }
        return e.getMessage();
    }

    private static boolean showSignalIfPresent(SQLException sql) {
        if ("45000".equals(sql.getSQLState())) {
            showWarning("Règle métier (trigger)", sql.getMessage());
            return true;
        }
        SQLException next = sql.getNextException();
        while (next != null) {
            if ("45000".equals(next.getSQLState())) {
                showWarning("Règle métier (trigger)", next.getMessage());
                return true;
            }
            next = next.getNextException();
        }
        return false;
    }

    private static boolean isMissingTable(SQLException e) {
        return e.getMessage() != null && e.getMessage().contains("alert_log");
    }

    private static void showInfo(String header, String message) {
        runOnFxThread(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("ArtConnect");
            a.setHeaderText(header);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    private static void showWarning(String header, String message) {
        runOnFxThread(() -> {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("ArtConnect");
            a.setHeaderText(header);
            a.setContentText(message);
            a.showAndWait();
        });
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
