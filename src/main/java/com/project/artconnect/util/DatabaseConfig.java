package com.project.artconnect.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration. Loads values from classpath resource
 * `database.properties` if present, otherwise falls back to sensible defaults.
 * Exposes USE_PERSISTENCE flag used across the application.
 */
public class DatabaseConfig {
    public static final String URL;
    public static final String USER;
    public static final String PASSWORD;
    public static final String ADMIN_PASSWORD;
    public static final boolean USE_PERSISTENCE;

    static {
        Properties p = new Properties();
        String url = "jdbc:mysql://localhost:3306/artconnect_db?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pwd = "Password123!";
        // default admin password for development; override via properties
        String adminPwd = "admin";
        boolean usePersistence = true;
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (in != null) {
                p.load(in);
                url = p.getProperty("url", url);
                user = p.getProperty("user", user);
                pwd = p.getProperty("password", pwd);
                // support an admin password configured via 'adminPassword' property
                adminPwd = p.getProperty("adminPassword", adminPwd);
                usePersistence = Boolean.parseBoolean(p.getProperty("usePersistence", String.valueOf(usePersistence)));
            }
        } catch (Exception e) {
            System.err.println("DatabaseConfig: failed to load database.properties, using defaults: " + e.getMessage());
        }
        URL = url;
        USER = user;
        PASSWORD = pwd;
        ADMIN_PASSWORD = adminPwd;
        USE_PERSISTENCE = usePersistence;
    }

    private DatabaseConfig() { /* utility */ }

    public static String getAdminPassword() {
        return ADMIN_PASSWORD;
    }
}
