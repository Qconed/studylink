package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class TutorReview {
    private int id;
    private int tutorId;
    private int studentId;
    private Integer bookingId; // Peut être null
    private String studentName; // Récupéré via JOIN
    private int rating; // 1-5
    private String comment;
    private LocalDateTime createdAt;

    // Constructor pour création
    public TutorReview(int tutorId, int studentId, Integer bookingId, int rating, String comment) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.bookingId = bookingId;
        this.rating = rating;
        this.comment = comment;
    }

    // Constructor complet depuis DB
    public TutorReview(int id, int tutorId, int studentId, Integer bookingId, 
                       String studentName, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.bookingId = bookingId;
        this.studentName = studentName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getTutorId() { return tutorId; }
    public int getStudentId() { return studentId; }
    public Integer getBookingId() { return bookingId; }
    public String getStudentName() { return studentName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setRating(int rating) { 
        if (rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }
    public void setComment(String comment) { this.comment = comment; }

    // Helper pour afficher les étoiles
    public String getStarsDisplay() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }
}
