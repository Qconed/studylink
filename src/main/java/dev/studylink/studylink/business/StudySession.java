package dev.studylink.studylink.business;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une session d'étude. Combine Product (titre/desc/prix) et TimeSlot (temps/lieu) via composition.
 */
public class StudySession implements Product {
    private int id;
    private String title;
    private String description;
    private double price;

    private boolean isTutored;
    private TimeSlot timeSlot;

    private int organizerId; // identifiant de l'utilisateur organisateur

    private int minParticipants;
    private int maxParticipants;

    private StudySessionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Integer> categoryIds = new ArrayList<>();

    // Constructors
    public StudySession() {
        this.timeSlot = new TimeSlot();
        this.status = StudySessionStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public StudySession(int id, String title, String description, double price, boolean isTutored,
                        TimeSlot timeSlot, int organizerId, int minParticipants, int maxParticipants) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.isTutored = isTutored;
        this.timeSlot = timeSlot != null ? timeSlot : new TimeSlot();
        this.organizerId = organizerId;
        this.minParticipants = Math.max(1, minParticipants);
        this.maxParticipants = Math.max(this.minParticipants, maxParticipants);
        this.status = StudySessionStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Product interface
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void setPrice(double price) {
        this.price = price;
    }

    // Getters / setters spécifiques
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isTutored() {
        return isTutored;
    }

    public void setTutored(boolean tutored) {
        isTutored = tutored;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public int getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(int organizerId) {
        this.organizerId = organizerId;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public void setMinParticipants(int minParticipants) {
        this.minParticipants = Math.max(1, minParticipants);
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = Math.max(this.minParticipants, maxParticipants);
    }

    public StudySessionStatus getStatus() {
        return status;
    }

    public void setStatus(StudySessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public List<Integer> getCategoryIds() {
        return new ArrayList<>(categoryIds);
    }

    public void setCategoryIds(List<Integer> categoryIds) {
        this.categoryIds = new ArrayList<>(categoryIds);
    }

    public void addCategoryId(int categoryId) {
        if (!this.categoryIds.contains(categoryId)) {
            this.categoryIds.add(categoryId);
        }
    }

    public void removeCategoryId(int categoryId) {
        this.categoryIds.removeIf(id -> id == categoryId);
    }

    /**
     * Valide les règles métiers locales (dates cohérentes, participants, prix) avant persistance.
     * @return true si la session est valide
     */
    public boolean validate() {
        if (title == null || title.isEmpty()) return false;
        if (timeSlot == null || !timeSlot.isValid()) return false;
        if (minParticipants < 1) return false;
        if (maxParticipants < minParticipants) return false;
        if (!isTutored && price != 0.0) return false;
        if (price < 0.0) return false;
        return true;
    }

    @Override
    public String toString() {
        return "StudySession{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", organizerId=" + organizerId +
                ", start=" + (timeSlot != null ? timeSlot.getStartTime() : null) +
                '}';
    }
}

