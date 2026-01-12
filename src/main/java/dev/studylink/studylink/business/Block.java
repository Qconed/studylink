package dev.studylink.studylink.business;

import java.time.LocalDateTime;

/**
 * Classe représentant un blocage d'utilisateur dans un chat
 */
public class Block {
    private int id;
    private int chatId;
    private int blockedId;
    private int blockerId;
    private LocalDateTime createdAt;

    // Constructeur vide
    public Block() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur pour création
    public Block(int chatId, int blockedId, int blockerId) {
        this.chatId = chatId;
        this.blockedId = blockedId;
        this.blockerId = blockerId;
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur complet
    public Block(int id, int chatId, int blockedId, int blockerId, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.blockedId = blockedId;
        this.blockerId = blockerId;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getChatId() { return chatId; }
    public int getBlockedId() { return blockedId; }
    public int getBlockerId() { return blockerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setChatId(int chatId) { this.chatId = chatId; }
    public void setBlockedId(int blockedId) { this.blockedId = blockedId; }
    public void setBlockerId(int blockerId) { this.blockerId = blockerId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Block{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", blockedId=" + blockedId +
                ", blockerId=" + blockerId +
                ", createdAt=" + createdAt +
                '}';
    }
}
