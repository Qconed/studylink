package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class Post {
    private int id;
    private int userId;
    private String authorName;
    private String content;
    private int likes;
    private int commentsCount;
    private LocalDateTime createdAt;

    // Constructeur, Getters et Setters
    public Post(int id, String authorName, String content, int likes, int commentsCount) {
        this.id = id;
        this.authorName = authorName;
        this.content = content;
        this.likes = likes;
        this.commentsCount = commentsCount;
    }

    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public int getLikes() { return likes; }
    public int getCommentsCount() { return commentsCount; }
}