package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.CommentDAO;
import dev.studylink.studylink.dao.ResourceDAO;
import dev.studylink.studylink.dao.ResourceFactory;
import dev.studylink.studylink.dao.SavedResourceDAO;

public class MySQLResourceFactory implements ResourceFactory {
    private static MySQLResourceFactory instance;

    private MySQLResourceFactory() {}

    public static MySQLResourceFactory getInstance() {
        if (instance == null) {
            instance = new MySQLResourceFactory();
        }
        return instance;
    }

    @Override
    public ResourceDAO createResourceDAO() {
        return MySQLResourceDAO.getInstance();
    }

    @Override
    public CommentDAO createCommentDAO() {
        return MySQLCommentDAO.getInstance();
    }

    @Override
    public SavedResourceDAO createSavedResourceDAO() {
        return MySQLSavedResourceDAO.getInstance();
    }
}
