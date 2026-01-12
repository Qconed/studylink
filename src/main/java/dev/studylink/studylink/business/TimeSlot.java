package dev.studylink.studylink.business;

import java.time.LocalDateTime;

/**
 * TimeSlot contient les informations temporelles et de lieu pour un événement.
 */
public class TimeSlot {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime dateOfDay; // parfois on peut stocker la date sans l'heure séparément
    private String location;

    public TimeSlot() {}

    public TimeSlot(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime dateOfDay, String location) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateOfDay = dateOfDay;
        this.location = location;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getDateOfDay() {
        return dateOfDay;
    }

    public void setDateOfDay(LocalDateTime dateOfDay) {
        this.dateOfDay = dateOfDay;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Vérifie que le time slot a des dates valides (start < end).
     */
    public boolean isValid() {
        return startTime != null && endTime != null && endTime.isAfter(startTime);
    }
}

