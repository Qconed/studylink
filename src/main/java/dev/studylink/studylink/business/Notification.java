package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private NotificationType type;
    private int chatId;
    private Integer relatedMessageId;
    private LocalDateTime createdAt;
    private boolean isRead;

    // Constructors
    public Notification() {}

    public Notification(int userId, NotificationType type, int chatId) {
        this.userId = userId;
        this.type = type;
        this.chatId = chatId;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public int getChatId() { return chatId; }
    public Integer getRelatedMessageId() { return relatedMessageId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setType(NotificationType type) { this.type = type; }
    public void setChatId(int chatId) { this.chatId = chatId; }
    public void setRelatedMessageId(Integer messageId) { this.relatedMessageId = messageId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRead(boolean read) { isRead = read; }

    // Business methods
    public void markAsRead() {
        this.isRead = true;
    }
}