package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Notification;
import java.util.List;

public interface NotificationDAO {
    /**
     * Crée une notification
     */
    boolean createNotification(Notification notification);

    /**
     * Récupère les notifications non-lues d'un utilisateur
     */
    List<Notification> getUnreadNotifications(int userId);

    /**
     * Marque une notification comme lue
     */
    boolean markAsRead(int notificationId);

    /**
     * Supprime une notification
     */
    boolean deleteNotification(int notificationId);

    /**
     * Ferme les ressources
     */
    void close();
}