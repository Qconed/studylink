package dev.studylink.studylink.business;

import java.time.LocalDateTime;

/**
 * Classe représentant une notification pour un utilisateur
 */
public class Notification {
    private int id;
    private int userId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    // Constructeur vide
    public Notification() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    // Constructeur pour création de notification
    public Notification(int userId, String content) {
        this.userId = userId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    // Constructeur complet (pour récupération depuis BDD)
    public Notification(int id, int userId, String content, LocalDateTime timestamp, boolean isRead) {
        this.id = id;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Méthode utilitaire pour marquer comme lue
    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                '}';
    }
}
