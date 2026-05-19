package com.project.artconnect.security;

/**
 * Application roles for access control.
 */
public enum UserRole {
    /** Read-only access to all data. */
    VISITOR("Visiteur"),
    /** Can register and book events/workshops, and manage own account. */
    MEMBER("Membre"),
    /** Can create, edit and delete exhibitions/workshops. */
    ORGANIZER("Organisateur"),
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
