package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.SessionBooking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionBookingDAO {
    /**
     * Créer une réservation de session
     */
    boolean createBooking(SessionBooking booking);
    
    /**
     * Récupérer une réservation par son ID
     */
    Optional<SessionBooking> getBookingById(int bookingId);
    
    /**
     * Récupérer toutes les réservations d'un étudiant
     */
    List<SessionBooking> getBookingsByStudentId(int studentId);
    
    /**
     * Récupérer toutes les réservations pour une session
     */
    List<SessionBooking> getBookingsBySessionId(int sessionId);
    
    /**
     * Récupérer toutes les réservations d'un tuteur (via ses sessions)
     */
    List<SessionBooking> getBookingsByTutorId(int tutorId);
    
    /**
     * Mettre à jour le statut d'une réservation
     */
    boolean updateBookingStatus(int bookingId, String status);
    
    /**
     * Mettre à jour le statut de paiement
     */
    boolean updatePaymentStatus(int bookingId, String paymentStatus);
    
    /**
     * Annuler une réservation
     */
    boolean cancelBooking(int bookingId);
    
    /**
     * Vérifier si un créneau est disponible (nombre max d'étudiants non atteint)
     */
    boolean isSlotAvailable(int sessionId, LocalDateTime bookingDate);
    
    /**
     * Compter le nombre de réservations actives pour une session à une date donnée
     */
    int getActiveBookingsCount(int sessionId, LocalDateTime bookingDate);
    
    /**
     * Fermer les ressources
     */
    void close();
}
