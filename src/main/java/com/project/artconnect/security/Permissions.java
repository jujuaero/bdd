package com.project.artconnect.security;

/**
 * Central permission checks for UI and handlers.
 */
public final class Permissions {

    public enum Resource {
        ARTISTS, ARTWORKS, GALLERIES, EXHIBITIONS, WORKSHOPS, COMMUNITY
    }

    private Permissions() {}

    public static boolean canView() {
        return true;
    }

    public static boolean canCreate(Resource resource) {
        return switch (AuthSession.get().getRole()) {
            case ADMIN -> true;
            case ARTIST -> resource == Resource.EXHIBITIONS;
            case VISITOR -> false;
        };
    }

    public static boolean canUpdate(Resource resource) {
        return switch (AuthSession.get().getRole()) {
            case ADMIN -> true;
            case ARTIST -> resource == Resource.COMMUNITY || resource == Resource.EXHIBITIONS;
            case VISITOR -> false;
        };
    }

    public static boolean canDelete(Resource resource) {
        return AuthSession.get().getRole() == UserRole.ADMIN;
    }

    public static boolean requiresAdmin(Resource resource) {
        return resource != Resource.COMMUNITY;
    }
}
