package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.*;


/**
 * Factory (Singleton) pour créer les DAO du Chat System
 * Pattern identique à MySQLUserFactory
 */
public class MySQLChatFactory implements ChatFactory {
    private static MySQLChatFactory instance;

    private MySQLChatFactory() {}

    public static synchronized MySQLChatFactory getInstance() {
        if (instance == null) {
            instance = new MySQLChatFactory();
        }
        return instance;
    }

    @Override
    public ChatDAO createChatDAO() {
        return MySQLChatDAO.getInstance();
    }

    @Override
    public MessageDAO createMessageDAO() {
        return MySQLMessageDAO.getInstance();
    }

    @Override
    public BlockDAO createBlockDAO() {
        return MySQLBlockDAO.getInstance();
    }

    @Override
    public NotificationDAO createNotificationDAO() {
        return MySQLNotificationDAO.getInstance();
    }
}