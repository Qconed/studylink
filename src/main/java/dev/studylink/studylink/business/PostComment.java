package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class PostComment {
    private int id;
    private int postId;
    private int authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    // Constructor pour créer un nouveau commentaire
    public PostComment(int postId, int authorId, String content) {
        this.postId = postId;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor pour les commentaires depuis la DB
    public PostComment(int id, int postId, int authorId, String authorName, String content, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getPostId() { return postId; }
    public int getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
}
