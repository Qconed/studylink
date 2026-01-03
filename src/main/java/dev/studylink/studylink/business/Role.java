package dev.studylink.studylink.business;

public enum Role {
    STUDENT,
    TUTOR,
    ADMIN;

    @Override
    public String toString() {
        return name();
    }

    public static Role fromString(String role) {
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return STUDENT; // par defaut
        }
    }
}