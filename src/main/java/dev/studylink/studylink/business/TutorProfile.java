package dev.studylink.studylink.business;

import java.time.LocalDateTime;
import java.util.List;

public class TutorProfile {
    private int id;
    private int userId;
    private String tutorName; // Nom du tuteur (récupéré via JOIN)
    private String bio;
    private double hourlyRate;
    private List<String> subjects;
    private String availability;
    private int totalSessions;
    private double averageRating;
    private boolean isPremium;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor pour création
    public TutorProfile(int userId, String bio, double hourlyRate, List<String> subjects, String availability) {
        this.userId = userId;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.subjects = subjects;
        this.availability = availability;
    }

    // Constructor complet depuis DB
    public TutorProfile(int id, int userId, String tutorName, String bio, double hourlyRate, 
                        List<String> subjects, String availability, int totalSessions, 
                        double averageRating, boolean isPremium, LocalDateTime createdAt, 
                        LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.tutorName = tutorName;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.subjects = subjects;
        this.availability = availability;
        this.totalSessions = totalSessions;
        this.averageRating = averageRating;
        this.isPremium = isPremium;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTutorName() { return tutorName; }
    public String getBio() { return bio; }
    public double getHourlyRate() { return hourlyRate; }
    public List<String> getSubjects() { return subjects; }
    public String getAvailability() { return availability; }
    public int getTotalSessions() { return totalSessions; }
    public double getAverageRating() { return averageRating; }
    public boolean isPremium() { return isPremium; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTutorName(String tutorName) { this.tutorName = tutorName; }
    public void setBio(String bio) { this.bio = bio; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public void setSubjects(List<String> subjects) { this.subjects = subjects; }
    public void setAvailability(String availability) { this.availability = availability; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public void setPremium(boolean premium) { isPremium = premium; }

    // Helper pour formater le prix
    public String getFormattedRate() {
        return String.format("$%.0f/hour", hourlyRate);
    }

    // Helper pour les sujets en string
    public String getSubjectsAsString() {
        return subjects != null ? String.join(", ", subjects) : "";
    }
}
