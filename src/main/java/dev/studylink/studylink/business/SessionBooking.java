package dev.studylink.studylink.business;

import java.time.LocalDateTime;

public class SessionBooking {
    private int id;
    private int sessionId;
    private int studentId;
    private String studentName; // Récupéré via JOIN
    private String sessionTitle; // Récupéré via JOIN
    private LocalDateTime bookingDate;
    private String status; // PENDING, CONFIRMED, COMPLETED, CANCELLED
    private String paymentStatus; // PENDING, PAID, REFUNDED
    private LocalDateTime createdAt;

    // Constructor pour création
    public SessionBooking(int sessionId, int studentId, LocalDateTime bookingDate) {
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.bookingDate = bookingDate;
        this.status = "PENDING";
        this.paymentStatus = "PENDING";
    }

    // Constructor complet depuis DB
    public SessionBooking(int id, int sessionId, int studentId, String studentName, 
                          String sessionTitle, LocalDateTime bookingDate, String status, 
                          String paymentStatus, LocalDateTime createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.sessionTitle = sessionTitle;
        this.bookingDate = bookingDate;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() { return id; }
    public int getSessionId() { return sessionId; }
    public int getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getSessionTitle() { return sessionTitle; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    // Helper pour savoir si la réservation est active
    public boolean isActive() {
        return status.equals("PENDING") || status.equals("CONFIRMED");
    }

    // Helper pour savoir si le paiement est effectué
    public boolean isPaid() {
        return paymentStatus.equals("PAID");
    }
}
