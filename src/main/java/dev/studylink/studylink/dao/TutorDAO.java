package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.TutorProfile;
import dev.studylink.studylink.business.TutorReview;
import dev.studylink.studylink.business.TutorSession;

import java.util.List;
import java.util.Optional;

public interface TutorDAO {
    // ===== TUTOR PROFILES =====
    
    /**
     * Créer un profil de tuteur pour un utilisateur
     */
    boolean createTutorProfile(TutorProfile profile);
    
    /**
     * Récupérer un profil de tuteur par son ID
     */
    Optional<TutorProfile> getTutorProfileById(int tutorId);
    
    /**
     * Récupérer un profil de tuteur par l'ID utilisateur
     */
    Optional<TutorProfile> getTutorProfileByUserId(int userId);
    
    /**
     * Récupérer tous les profils de tuteurs
     */
    List<TutorProfile> getAllTutorProfiles();
    
    /**
     * Rechercher des tuteurs par matière
     */
    List<TutorProfile> searchTutorsBySubject(String subject);
    
    /**
     * Récupérer les tuteurs premium
     */
    List<TutorProfile> getPremiumTutors();
    
    /**
     * Mettre à jour un profil de tuteur
     */
    boolean updateTutorProfile(TutorProfile profile);
    
    /**
     * Mettre à jour la note moyenne d'un tuteur
     */
    boolean updateTutorRating(int tutorId, double newRating);
    
    /**
     * Incrémenter le nombre total de sessions d'un tuteur
     */
    boolean incrementTutorSessions(int tutorId);
    
    /**
     * Supprimer un profil de tuteur
     */
    boolean deleteTutorProfile(int tutorId);
    
    // ===== TUTOR SESSIONS =====
    
    /**
     * Créer une session de tutorat
     */
    boolean createTutorSession(TutorSession session);
    
    /**
     * Récupérer une session par son ID
     */
    Optional<TutorSession> getTutorSessionById(int sessionId);
    
    /**
     * Récupérer toutes les sessions d'un tuteur
     */
    List<TutorSession> getSessionsByTutorId(int tutorId);
    
    /**
     * Rechercher des sessions par matière
     */
    List<TutorSession> searchSessionsBySubject(String subject);
    
    /**
     * Mettre à jour une session
     */
    boolean updateTutorSession(TutorSession session);
    
    /**
     * Supprimer une session
     */
    boolean deleteTutorSession(int sessionId);
    
    // ===== TUTOR REVIEWS =====
    
    /**
     * Créer un avis pour un tuteur
     */
    boolean createReview(TutorReview review);
    
    /**
     * Récupérer tous les avis d'un tuteur
     */
    List<TutorReview> getReviewsByTutorId(int tutorId);
    
    /**
     * Calculer la note moyenne d'un tuteur
     */
    double calculateAverageRating(int tutorId);
    
    /**
     * Compter le nombre d'avis d'un tuteur
     */
    int getReviewCount(int tutorId);
    
    /**
     * Fermer les ressources
     */
    void close();
}
