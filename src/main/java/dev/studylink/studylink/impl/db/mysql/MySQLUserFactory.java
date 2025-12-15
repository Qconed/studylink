package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.dao.UserFactory;

public class MySQLUserFactory implements UserFactory {
    private static MySQLUserFactory instance;
    private MySQLUserFactory(){}

    public UserDAO createUserDAO() {
        return MySQLUserDAO.getInstance();
    }

    public static MySQLUserFactory getInstance() {
        if (instance == null) {
            instance = new MySQLUserFactory();
        }
        return instance;
    }
}

