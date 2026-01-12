package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Notification;

import java.util.List;

/**
 * Interface DAO pour la gestion des notifications
 */
public interface NotificationDAO {
    
    /**
     * Crée une nouvelle notification
     * @param notification La notification à créer
     * @return true si la création a réussi, false sinon
     */
    boolean createNotification(Notification notification);

    /**
     * Récupère toutes les notifications d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des notifications de l'utilisateur
     */
    List<Notification> findByUserId(int userId);

    /**
     * Récupère toutes les notifications non lues d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return Liste des notifications non lues
     */
    List<Notification> findUnreadByUserId(int userId);

    /**
     * Marque une notification comme lue
     * @param notificationId ID de la notification
     * @return true si l'opération a réussi, false sinon
     */
    boolean markAsRead(int notificationId);

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     * @param userId ID de l'utilisateur
     * @return true si l'opération a réussi, false sinon
     */
    boolean markAllAsReadForUser(int userId);

    /**
     * Supprime une notification par son ID
     * @param notificationId ID de la notification à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteNotification(int notificationId);

    /**
     * Supprime toutes les notifications d'un utilisateur
     * @param userId ID de l'utilisateur
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteAllForUser(int userId);

    /**
     * Compte le nombre de notifications non lues pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre de notifications non lues
     */
    int countUnreadNotifications(int userId);

    /**
     * Ferme les ressources
     */
    void close();
}
