package dev.studylink.studylink.business;

public enum NotificationType {
    NEW_MESSAGE,
    USER_JOINED,
    MESSAGE_EDITED;

    @Override
    public String toString() {
        return name();
    }

    public static NotificationType fromString(String type) {
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NEW_MESSAGE;
        }
    }
}