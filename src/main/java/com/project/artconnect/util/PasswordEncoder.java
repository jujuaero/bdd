package com.project.artconnect.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class for password hashing and verification using bcrypt.
 * Provides secure password storage and validation.
 */
public class PasswordEncoder {
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Encodes (hashes) a plain text password using bcrypt.
     *
     * @param plainPassword the plain text password to hash
     * @return the bcrypt-hashed password
     */
    public static String encode(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return encoder.encode(plainPassword);
    }

    /**
     * Verifies a plain text password against a bcrypt-hashed password.
     *
     * @param plainPassword the plain text password to verify
     * @param hashedPassword the bcrypt-hashed password to compare against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean matches(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return encoder.matches(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Invalid bcrypt format
            return false;
        }
    }
}

