package dev.studylink.studylink.business;

import dev.studylink.studylink.exception.UnauthorizedException;

import java.util.List;
import java.util.Optional;

/**
 * Facade exposée à l'UI pour manipuler les StudySessions (vérifie l'authentification via SessionFacade).
 */
public class StudySessionFacade {
    private static StudySessionFacade instance = null;
    private StudySessionManager manager = StudySessionManager.getInstance();
    private SessionFacade sessionFacade = SessionFacade.getInstance();

    private StudySessionFacade() {}

    public static StudySessionFacade getInstance() {
        if (instance == null) {
            instance = new StudySessionFacade();
        }
        return instance;
    }

    /**
     * Crée une nouvelle session. L'utilisateur courant devient l'organizer si non fourni.
     * @throws UnauthorizedException si l'utilisateur n'est pas authentifié
     */
    public StudySession createStudySession(StudySession session) throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        if (session.getOrganizerId() == 0) {
            session.setOrganizerId(sessionFacade.getCurrentUser().getId());
        }
        return manager.createSession(session);
    }

    /**
     * Met à jour la session. Vérifie que l'utilisateur courant est l'organisateur.
     */
    public boolean updateStudySession(StudySession session) throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        if (sessionFacade.getCurrentUser().getId() != session.getOrganizerId()) {
            throw new UnauthorizedException("Seul l'organisateur peut modifier la session");
        }
        return manager.updateSession(session);
    }

    /**
     * Annule une session (organisateur uniquement)
     */
    public boolean cancelStudySession(int sessionId) throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        Optional<StudySession> opt = manager.getSessionById(sessionId);
        if (opt.isEmpty()) return false;
        StudySession s = opt.get();
        if (s.getOrganizerId() != sessionFacade.getCurrentUser().getId()) {
            throw new UnauthorizedException("Seul l'organisateur peut annuler la session");
        }
        
        // IMPORTANT : Récupérer les participants AVANT d'annuler la session
        List<Integer> participants = manager.getParticipantsIds(sessionId);
        String sessionTitle = s.getTitle();
        String organizerName = sessionFacade.getCurrentUser().getFullname();
        
        // Annuler la session
        boolean cancelled = manager.cancelSession(sessionId);
        
        // Envoyer des notifications aux participants APRÈS l'annulation
        if (cancelled) {
            for (Integer participantId : participants) {
                sessionFacade.createNotification(participantId, 
                    "📚 La session \"" + sessionTitle + "\" a été annulée par " + organizerName);
            }
        }
        
        return cancelled;
    }

    /**
     * Permet à l'utilisateur courant de rejoindre une session
     */
    public boolean joinStudySession(int sessionId) throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        int userId = sessionFacade.getCurrentUser().getId();
        boolean joined = manager.joinSession(sessionId, userId);
        
        // Envoyer une notification à l'organisateur
        if (joined) {
            Optional<StudySession> sessionOpt = manager.getSessionById(sessionId);
            if (sessionOpt.isPresent()) {
                StudySession session = sessionOpt.get();
                String userName = sessionFacade.getCurrentUser().getFullname();
                sessionFacade.createNotification(session.getOrganizerId(),
                    "👤 " + userName + " a rejoint votre session \"" + session.getTitle() + "\"");
            }
        }
        
        return joined;
    }

    /**
     * Permet à l'utilisateur courant de quitter une session
     */
    public boolean leaveStudySession(int sessionId) throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        int userId = sessionFacade.getCurrentUser().getId();
        
        // Récupérer les informations de la session AVANT que l'utilisateur ne la quitte
        Optional<StudySession> sessionOpt = manager.getSessionById(sessionId);
        StudySession session = null;
        if (sessionOpt.isPresent()) {
            session = sessionOpt.get();
        }
        
        boolean left = manager.leaveSession(sessionId, userId);
        
        // Envoyer une notification à l'organisateur
        if (left && session != null) {
            String userName = sessionFacade.getCurrentUser().getFullname();
            sessionFacade.createNotification(session.getOrganizerId(),
                "👋 " + userName + " a quitté votre session \"" + session.getTitle() + "\"");
        }
        
        return left;
    }

    public Optional<StudySession> getStudySession(int sessionId) {
        return manager.getSessionById(sessionId);
    }

    public List<StudySession> listAllSessions() {
        return manager.listAllSessions();
    }

    public List<StudySession> listMySessions() throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        int userId = sessionFacade.getCurrentUser().getId();
        return manager.listByOrganizer(userId);
    }

    /**
     * Liste les sessions auxquelles l'utilisateur est inscrit (participant)
     */
    public List<StudySession> listRegisteredSessions() throws UnauthorizedException {
        if (!sessionFacade.isLoggedIn()) throw new UnauthorizedException("Utilisateur non authentifié");
        int userId = sessionFacade.getCurrentUser().getId();
        return manager.listByParticipant(userId);
    }

    /**
     * Récupère le nombre de participants d'une session
     */
    public int getParticipantsCount(int sessionId) {
        return manager.getParticipantsIds(sessionId).size();
    }

    /**
     * Vérifie si un utilisateur est inscrit à une session
     */
    public boolean isUserRegistered(int sessionId, int userId) {
        List<Integer> participants = manager.getParticipantsIds(sessionId);
        return participants.contains(userId);
    }
}
