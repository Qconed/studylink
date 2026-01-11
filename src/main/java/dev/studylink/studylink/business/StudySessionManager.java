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
     * Liste les sessions auxquelles un utilisateur participe
     */
    public List<StudySession> listByParticipant(int userId) {
        return dao.findByParticipant(userId);
    }

    /**
     * Ajoute un participant si la session n'est pas pleine.
     */
    public boolean joinSession(int sessionId, int userId) {
        Optional<StudySession> opt = dao.findById(sessionId);
        if (opt.isEmpty()) {
            System.err.println("Join failed: Session " + sessionId + " not found");
            return false;
        }
        StudySession s = opt.get();
        int count = dao.getParticipantsCount(sessionId);
        
        System.out.println("\n=== Join Session Debug ===");
        System.out.println("Session ID: " + sessionId);
        System.out.println("User ID: " + userId);
        System.out.println("Current participants: " + count);
        System.out.println("Max participants: " + s.getMaxParticipants());
        System.out.println("Session status: " + s.getStatus());
        System.out.println("========================\n");
        
        if (count >= s.getMaxParticipants()) {
            System.err.println("Join failed: Session is full (" + count + "/" + s.getMaxParticipants() + ")");
            return false;
        }
        if (s.getStatus() != StudySessionStatus.SCHEDULED) {
            System.err.println("Join failed: Session status is " + s.getStatus() + " (expected SCHEDULED)");
            return false;
        }
        
        boolean result = dao.addParticipant(sessionId, userId, "PARTICIPANT");
        if (result) {
            System.out.println("✓ Successfully added participant " + userId + " to session " + sessionId);
        } else {
            System.err.println("Join failed: Could not add participant to database");
        }
        return result;
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
     * Annule une session (supprime la session de la base de données)
     */
    public boolean cancelSession(int sessionId) {
        Optional<StudySession> opt = dao.findById(sessionId);
        if (opt.isEmpty()) return false;
        // Supprime la session et toutes ses dépendances (participants, catégories)
        return dao.deleteStudySession(sessionId);
    }

    public void close() {
        dao.close();
    }
}
