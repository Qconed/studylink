package dev.studylink.studylink.business;

public enum ChatType {
    PRIVATE,  // Chat 1v1
    GROUP;    // Chat groupe

    @Override
    public String toString() {
        return name();
    }

    public static ChatType fromString(String type) {
        try {
            return ChatType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PRIVATE;
        }
    }
}