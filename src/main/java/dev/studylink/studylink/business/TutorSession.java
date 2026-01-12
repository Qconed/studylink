package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class TutorSession {
    private int id;
    private int tutorId;
    private String title;
    private String subject;
    private String description;
    private double price;
    private int durationMinutes;
    private int maxStudents;
    private LocalDateTime createdAt;

    // Constructor pour création
    public TutorSession(int tutorId, String title, String subject, String description, 
                        double price, int durationMinutes, int maxStudents) {
        this.tutorId = tutorId;
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.maxStudents = maxStudents;
    }

    // Constructor complet depuis DB
    public TutorSession(int id, int tutorId, String title, String subject, String description, 
                        double price, int durationMinutes, int maxStudents, LocalDateTime createdAt) {
        this.id = id;
        this.tutorId = tutorId;
        this.title = title;
        this.subject = subject;
        this.description = description;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.maxStudents = maxStudents;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getTutorId() { return tutorId; }
    public String getTitle() { return title; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getMaxStudents() { return maxStudents; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }

    // Helper pour formater la durée
    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        if (hours > 0 && mins > 0) {
            return String.format("%dh %dmin", hours, mins);
        } else if (hours > 0) {
            return String.format("%dh", hours);
        } else {
            return String.format("%dmin", mins);
        }
    }

    // Helper pour formater le prix
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
}
