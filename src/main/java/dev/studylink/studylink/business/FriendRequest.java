package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class FriendRequest {
    private int id;
    private int senderId;
    private int receiverId;
    private FriendRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    // For display purposes
    private String senderName;
    private String receiverName;

    // Constructor without ID (for creation)
    public FriendRequest(int senderId, int receiverId) {
        this.id = 0;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = FriendRequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.respondedAt = null;
    }

    // Constructor with ID (from database)
    public FriendRequest(int id, int senderId, int receiverId, FriendRequestStatus status) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.respondedAt = null;
    }

    // Full constructor
    public FriendRequest(int id, int senderId, int receiverId, FriendRequestStatus status,
                         LocalDateTime createdAt, LocalDateTime respondedAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    // Business methods
    public void accept() {
        this.status = FriendRequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = FriendRequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", status=" + status +
                '}';
    }
}