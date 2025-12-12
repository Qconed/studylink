package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.dao.UserFactory;

public class MySQLUserFactory implements UserFactory {

    public UserDAO createUserDAO() {
        return new MySQLUserDAO();
    }

    // Singleton (optionnel)
    private static MySQLUserFactory instance;

    public static MySQLUserFactory getInstance() {
        if (instance == null) {
            instance = new MySQLUserFactory();
        }
        return instance;
    }
}

