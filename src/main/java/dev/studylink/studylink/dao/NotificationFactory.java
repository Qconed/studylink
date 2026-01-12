package dev.studylink.studylink.dao;

/**
 * Factory pour créer des instances de NotificationDAO
 */
public interface NotificationFactory {
    /**
     * Crée une instance de NotificationDAO
     */
    NotificationDAO createNotificationDAO();
}
