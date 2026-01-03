package dev.studylink.studylink.business;

public enum CategoryType {
    ACADEMIC_SUBJECT,
    PERSONAL_INTEREST,
    TRAINING;

    @Override
    public String toString() {
        return name();
    }

    public static CategoryType fromString(String type) {
        try {
            return CategoryType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACADEMIC_SUBJECT; // Default type
        }
    }
}