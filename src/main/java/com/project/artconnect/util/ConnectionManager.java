package com.project.artconnect.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class to manage JDBC connections.
 */
public class ConnectionManager {

    /**
     * Provides a connection to the MySQL database using values from
     * DatabaseConfig. Change the constants in DatabaseConfig to match your
     * local environment (URL, USER, PASSWORD).
     *
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        // Use DriverManager directly for the exercise. In production consider a
        // connection pool (HikariCP).
        return DriverManager.getConnection(com.project.artconnect.util.DatabaseConfig.URL,
                com.project.artconnect.util.DatabaseConfig.USER,
                com.project.artconnect.util.DatabaseConfig.PASSWORD);
    }
}
