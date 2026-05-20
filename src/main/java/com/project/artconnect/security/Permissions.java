package com.project.artconnect.security;

/**
 * Central permission checks for UI and handlers.
 */
public final class Permissions {

    public enum Resource {
        ARTISTS, ARTWORKS, GALLERIES, EXHIBITIONS, WORKSHOPS, BOOKINGS, COMMUNITY
    }

    private Permissions() {}

    public static boolean canView() {
        return true;
    }

    public static boolean canCreate(Resource resource) {
        return switch (AuthSession.get().getRole()) {
            case ADMIN -> true;
            case ORGANIZER -> resource == Resource.EXHIBITIONS || resource == Resource.WORKSHOPS;
            case VISITOR -> false;
            case MEMBER -> false;
        };
    }

    public static boolean canUpdate(Resource resource) {
        return switch (AuthSession.get().getRole()) {
            case ADMIN -> true;
            case ORGANIZER -> resource == Resource.EXHIBITIONS || resource == Resource.WORKSHOPS;
            case MEMBER -> resource == Resource.COMMUNITY;
            case VISITOR -> false;
        };
    }

    public static boolean canDelete(Resource resource) {
        return switch (AuthSession.get().getRole()) {
            case ADMIN -> true;
            case ORGANIZER -> resource == Resource.EXHIBITIONS || resource == Resource.WORKSHOPS;
            case MEMBER, VISITOR -> false;
        };
    }

    public static boolean requiresAdmin(Resource resource) {
        return resource != Resource.COMMUNITY;
    }
}
