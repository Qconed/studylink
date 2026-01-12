package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int chatId;
    private int senderId;
    private String senderFullname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private boolean isEdited;
    private boolean isDeleted;

    // Constructor
    public Message() {}

    public Message(int chatId, int senderId, String content) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isEdited = false;
        this.isDeleted = false;
    }

    // Getters
    public int getId() { return id; }
    public int getChatId() { return chatId; }
    public int getSenderId() { return senderId; }
    public String getSenderFullname() { return senderFullname; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getEditedAt() { return editedAt; }
    public boolean isEdited() { return isEdited; }
    public boolean isDeleted() { return isDeleted; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setChatId(int chatId) { this.chatId = chatId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }
    public void setSenderFullname(String senderFullname) { this.senderFullname = senderFullname; }
    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
    public void setEdited(boolean edited) { isEdited = edited; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    // Business methods
    public boolean isFromUser(int userId) {
        return this.senderId == userId;
    }

    public void editContent(String newContent) {
        this.content = newContent;
        this.editedAt = LocalDateTime.now();
        this.isEdited = true;
    }

    public void delete() {
        this.isDeleted = true;
    }

    @Override
    public String toString() {
        return "Message #" + id + " from user " + senderId;
    }
}