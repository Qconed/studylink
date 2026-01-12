package dev.studylink.studylink.dao;

/**
 * Factory interface pour le Chat System
 * Crée les DAO instances
 */
public interface ChatFactory {
    ChatDAO createChatDAO();
    MessageDAO createMessageDAO();
    BlockDAO createBlockDAO();
    NotificationDAO createNotificationDAO();
}