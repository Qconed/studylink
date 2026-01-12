package dev.studylink.studylink.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    private int id;
    private ChatType type;
    private String name;
    private int createdById;
    private List<User> participants;
    private List<Message> messages;
    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;
    private boolean isActive;

    // Constructor pour création
    public Chat() {
        this.participants = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.isActive = true;
    }

    // Constructor complet
    public Chat(int id, ChatType type, String name, int createdById) {
        this();
        this.id = id;
        this.type = type;
        this.name = name;
        this.createdById = createdById;
    }

    // Getters
    public int getId() { return id; }
    public ChatType getType() { return type; }
    public String getName() { return name; }
    public int getCreatedById() { return createdById; }
    public List<User> getParticipants() { return participants; }
    public List<Message> getMessages() { return messages; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setType(ChatType type) { this.type = type; }
    public void setName(String name) { this.name = name; }
    public void setCreatedById(int createdById) { this.createdById = createdById; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public void setActive(boolean active) { isActive = active; }
    public void setParticipants(List<User> participants) { this.participants = participants; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    // Business methods
    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }

    public void removeParticipant(User user) {
        participants.remove(user);
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public boolean isPrivate() { return type == ChatType.PRIVATE; }
    public boolean isGroup() { return type == ChatType.GROUP; }

    @Override
    public String toString() {
        return name != null ? name : "Chat #" + id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Chat chat = (Chat) obj;
        return id == chat.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}