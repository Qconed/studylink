package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.NotificationDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLNotificationFactory;

import java.util.List;

/**
 * Manager pour la gestion de la logique métier des notifications
 * Suit le pattern Singleton
 */
public class NotificationManager {
    private static NotificationManager instance = null;
    private final NotificationDAO notificationDAO;

    // CONSTRUCTION -------------------
    private NotificationManager() {
        this.notificationDAO = MySQLNotificationFactory.getInstance().createNotificationDAO();
    }

    // Méthode d'obtention d'instance singleton
    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    // METHODES BUSINESS -----------------------

    /**
     * Crée une nouvelle notification pour un utilisateur
     * @param userId ID de l'utilisateur destinataire
     * @param content Contenu de la notification
     * @return true si la création a réussi, false sinon
     */
    public boolean createNotification(int userId, String content) {
        if (content == null || content.trim().isEmpty()) {
            System.err.println("Le contenu de la notification ne peut pas être vide");
            return false;
        }

        Notification notification = new Notification(userId, content);
        return notificationDAO.createNotification(notification);
    }

    /**
     * Récupère toutes les notifications d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des notifications
     */
    public List<Notification> getUserNotifications(int userId) {
        return notificationDAO.findByUserId(userId);
    }

    /**
     * Récupère uniquement les notifications non lues d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des notifications non lues
     */
    public List<Notification> getUnreadNotifications(int userId) {
        return notificationDAO.findUnreadByUserId(userId);
    }

    /**
     * Compte le nombre de notifications non lues pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications non lues
     */
    public int getUnreadCount(int userId) {
        return notificationDAO.countUnreadNotifications(userId);
    }

    /**
     * Marque une notification comme lue
     * @param notificationId ID de la notification
     * @return true si l'opération a réussi, false sinon
     */
    public boolean markNotificationAsRead(int notificationId) {
        return notificationDAO.markAsRead(notificationId);
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     * @param userId ID de l'utilisateur
     * @return true si l'opération a réussi, false sinon
     */
    public boolean markAllNotificationsAsRead(int userId) {
        return notificationDAO.markAllAsReadForUser(userId);
    }

    /**
     * Supprime une notification spécifique
     * @param notificationId ID de la notification à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteNotification(int notificationId) {
        return notificationDAO.deleteNotification(notificationId);
    }

    /**
     * Supprime toutes les notifications d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteAllUserNotifications(int userId) {
        return notificationDAO.deleteAllForUser(userId);
    }

    /**
     * Vérifie si un utilisateur a des notifications non lues
     * @param userId ID de l'utilisateur
     * @return true si l'utilisateur a des notifications non lues
     */
    public boolean hasUnreadNotifications(int userId) {
        return getUnreadCount(userId) > 0;
    }

    /**
     * Crée une notification pour informer d'une nouvelle demande d'ami
     * @param receiverId ID de l'utilisateur qui reçoit la demande
     * @param senderName Nom de l'utilisateur qui envoie la demande
     */
    public boolean notifyFriendRequest(int receiverId, String senderName) {
        String content = senderName + " vous a envoyé une demande d'ami";
        return createNotification(receiverId, content);
    }

    /**
     * Crée une notification pour informer qu'une demande d'ami a été acceptée
     * @param userId ID de l'utilisateur à notifier
     * @param friendName Nom de l'ami qui a accepté
     */
    public boolean notifyFriendRequestAccepted(int userId, String friendName) {
        String content = friendName + " a accepté votre demande d'ami";
        return createNotification(userId, content);
    }

    /**
     * Crée une notification pour une nouvelle ressource partagée
     * @param userId ID de l'utilisateur à notifier
     * @param resourceTitle Titre de la ressource
     */
    public boolean notifyNewResource(int userId, String resourceTitle) {
        String content = "Nouvelle ressource disponible: " + resourceTitle;
        return createNotification(userId, content);
    }

    /**
     * Crée une notification pour une nouvelle session d'étude
     * @param userId ID de l'utilisateur à notifier
     * @param sessionTitle Titre de la session
     */
    public boolean notifyNewStudySession(int userId, String sessionTitle) {
        String content = "Nouvelle session d'étude: " + sessionTitle;
        return createNotification(userId, content);
    }

    /**
     * Ferme les ressources du DAO
     */
    public void close() {
        notificationDAO.close();
    }
}
