package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.StudySessionDAO;
import dev.studylink.studylink.dao.StudySessionFactory;

public class MySQLStudySessionFactory implements StudySessionFactory {
    private static MySQLStudySessionFactory instance;

    private MySQLStudySessionFactory() {}

    public static synchronized MySQLStudySessionFactory getInstance() {
        if (instance == null) {
            instance = new MySQLStudySessionFactory();
        }
        return instance;
    }

    @Override
    public StudySessionDAO createStudySessionDAO() {
        return MySQLStudySessionDAO.getInstance();
    }
}

