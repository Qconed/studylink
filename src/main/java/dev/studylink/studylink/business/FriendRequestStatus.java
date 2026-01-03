package dev.studylink.studylink.business;

public enum FriendRequestStatus {
    PENDING,
    ACCEPTED,
    REJECTED;

    @Override
    public String toString() {
        return name();
    }

    public static FriendRequestStatus fromString(String status) {
        try {
            return FriendRequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING; // Default status
        }
    }
}