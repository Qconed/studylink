package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class Comment {
    private int id;
    private String content;
    private int authorId;
    private int resourceId;
    private LocalDateTime timestamp;
    private String authorName;

    // Constructor without ID (for creation)
    public Comment(String content, int authorId, int resourceId) {
        this.id = 0;
        this.content = content;
        this.authorId = authorId;
        this.resourceId = resourceId;
        this.timestamp = LocalDateTime.now();
        this.authorName = null;
    }

    // Constructor with ID (from database)
    public Comment(int id, String content, int authorId, int resourceId, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.authorId = authorId;
        this.resourceId = resourceId;
        this.timestamp = timestamp;
        this.authorName = null;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getAuthorId() {
        return authorId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getAuthorName() {
        return authorName;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", authorId=" + authorId +
                ", resourceId=" + resourceId +
                ", timestamp=" + timestamp +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Comment comment = (Comment) obj;
        return id == comment.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
