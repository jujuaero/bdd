package com.project.artconnect.security;

/**
 * Application roles for access control.
 */
public enum UserRole {
    /** Read-only access to all data. */
    VISITOR("Visiteur"),
    /** Can manage own profile and artist-related content. */
    ARTIST("Artiste"),
    /** Full create, update, and delete on all resources. */
    ADMIN("Administrateur");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
