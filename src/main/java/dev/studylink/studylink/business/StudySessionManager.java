package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.StudySessionFactory;
import dev.studylink.studylink.dao.StudySessionDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLStudySessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * Manager bas-niveau pour les StudySession : encapsule l'accès au DAO et fournit
 * des opérations transactionnelles simples.
 */
public class StudySessionManager {
    private static StudySessionManager instance = null;
    private StudySessionFactory factory = MySQLStudySessionFactory.getInstance();
    private StudySessionDAO dao = factory.createStudySessionDAO();

    private StudySessionManager() {}

    public static StudySessionManager getInstance() {
        if (instance == null) {
            instance = new StudySessionManager();
        }
        return instance;
    }

    /**
     * Crée une session en persistant via DAO. Retourne l'objet créé (ou null si erreur).
     */
    public StudySession createSession(StudySession session) {
        if (!session.validate()) return null;
        boolean ok = dao.createStudySession(session);
        return ok ? session : null;
    }

    /**
     * Met à jour une session existante.
     */
    public boolean updateSession(StudySession session) {
        if (!session.validate()) return false;
        return dao.updateStudySession(session);
    }

    /**
     * Supprime une session.
     */
    public boolean deleteSession(int sessionId) {
        return dao.deleteStudySession(sessionId);
    }

    /**
     * Récupère une session par son id
     */
    public Optional<StudySession> getSessionById(int id) {
        return dao.findById(id);
    }

    /**
     * Liste toutes les sessions
     */
    public List<StudySession> listAllSessions() {
        return dao.findAll();
    }

    /**
     * Liste les sessions d'un organisateur
     */
    public List<StudySession> listByOrganizer(int organizerId) {
        return dao.findByOrganizer(organizerId);
    }

    /**
     * Ajoute un participant si la session n'est pas pleine.
     */
    public boolean joinSession(int sessionId, int userId) {
        Optional<StudySession> opt = dao.findById(sessionId);
        if (opt.isEmpty()) return false;
        StudySession s = opt.get();
        int count = dao.getParticipantsCount(sessionId);
        if (count >= s.getMaxParticipants() || s.getStatus() != StudySessionStatus.SCHEDULED) {
            return false;
        }
        return dao.addParticipant(sessionId, userId, "PARTICIPANT");
    }

    /**
     * Retire un participant
     */
    public boolean leaveSession(int sessionId, int userId) {
        return dao.removeParticipant(sessionId, userId);
    }

    /**
     * Récupère la liste des IDs des participants
     */
    public List<Integer> getParticipantsIds(int sessionId) {
        return dao.getParticipantsIds(sessionId);
    }

    /**
     * Retire un participant par l'organisateur (ou par le participant lui-même)
     */
    public boolean removeParticipant(int sessionId, int userId) {
        return dao.removeParticipant(sessionId, userId);
    }

    /**
     * Annule une session (change le statut)
     */
    public boolean cancelSession(int sessionId) {
        Optional<StudySession> opt = dao.findById(sessionId);
        if (opt.isEmpty()) return false;
        StudySession s = opt.get();
        s.setStatus(StudySessionStatus.CANCELLED);
        return dao.updateStudySession(s);
    }

    public void close() {
        dao.close();
    }
}
