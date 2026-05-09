package com.project.artconnect.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration used by the application.
 *
 * This class lives in `com.project.artconnect.util` because several utility
 * classes (e.g. `ConnectionManager`, `ServiceProvider`) reference it from
 * that package. It prefers to read `database.properties` from resources when
 * present; otherwise falls back to hard-coded defaults (suitable for local dev).
 */
public final class DatabaseConfig {
    public static final String URL;
    public static final String USER;
    public static final String PASSWORD;
    public static final boolean USE_PERSISTENCE;

    static {
        // Defaults (use the credentials you provided)
        String url = "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "Password123!";
        boolean usePersistence = true; // enable JDBC by default as requested

        // Try to load src/main/resources/database.properties if present
        try (InputStream is = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                url = p.getProperty("url", url);
                user = p.getProperty("user", user);
                password = p.getProperty("password", password);
                String up = p.getProperty("usePersistence", p.getProperty("usePersistence", null));
                if (up != null) {
                    usePersistence = Boolean.parseBoolean(up);
                }
            }
        } catch (IOException ignored) {
            // If reading fails, keep defaults
        }

        URL = url;
        USER = user;
        PASSWORD = password;
        USE_PERSISTENCE = usePersistence;
    }

    private DatabaseConfig() {
    }
}

