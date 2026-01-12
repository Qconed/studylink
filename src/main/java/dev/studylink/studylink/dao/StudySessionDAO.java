package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.StudySession;

import java.util.List;
import java.util.Optional;

public interface StudySessionDAO {
    /**
     * Trouve une session par son identifiant
     */
    Optional<StudySession> findById(int id);

    /**
     * Crée une session et retourne true si la création a réussi
     */
    boolean createStudySession(StudySession session);

    /**
     * Met à jour une session existante
     */
    boolean updateStudySession(StudySession session);

    /**
     * Supprime une session
     */
    boolean deleteStudySession(int sessionId);

    /**
     * Liste toutes les sessions
     */
    List<StudySession> findAll();

    /**
     * Liste les sessions organisées par un utilisateur
     */
    List<StudySession> findByOrganizer(int organizerId);

    /**
     * Liste les sessions auxquelles un utilisateur participe (sans être organisateur)
     */
    List<StudySession> findByParticipant(int userId);

    /**
     * Ajoute un participant à une session (insert ignore pour éviter doublons)
     */
    boolean addParticipant(int sessionId, int userId, String role);

    /**
     * Retire un participant d'une session
     */
    boolean removeParticipant(int sessionId, int userId);

    /**
     * Retourne le nombre actuel de participants pour une session
     */
    int getParticipantsCount(int sessionId);

    /**
     * Retourne la liste des IDs des participants pour une session
     */
    List<Integer> getParticipantsIds(int sessionId);

    /**
     * Retourne la liste des catégories associées à la session
     */
    List<Integer> getSessionCategoryIds(int sessionId);

    /**
     * Ajoute une catégorie à une session
     */
    boolean addCategoryToSession(int sessionId, int categoryId);

    /**
     * Retire une catégorie d'une session
     */
    boolean removeCategoryFromSession(int sessionId, int categoryId);

    /**
     * Ferme les ressources (DataSource, etc.)
     */
    void close();
}
