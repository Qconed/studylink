package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.NotificationDAO;
import dev.studylink.studylink.dao.NotificationFactory;

/**
 * Factory MySQL pour créer des instances de NotificationDAO
 */
public class MySQLNotificationFactory implements NotificationFactory {
    
    @Override
    public NotificationDAO createNotificationDAO() {
        return new MySQLNotificationDAO();
    }
    
    // Singleton pattern
    private static MySQLNotificationFactory instance;
    
    public static MySQLNotificationFactory getInstance() {
        if (instance == null) {
            instance = new MySQLNotificationFactory();
        }
        return instance;
    }
}
