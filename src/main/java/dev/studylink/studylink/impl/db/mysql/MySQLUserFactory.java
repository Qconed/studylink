package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.dao.CartDAO;
import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.dao.FriendRequestDAO;
import dev.studylink.studylink.dao.FriendshipDAO;
import dev.studylink.studylink.dao.PostDAO;
import dev.studylink.studylink.dao.SessionBookingDAO;
import dev.studylink.studylink.dao.TutorDAO;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.dao.UserFactory;


public class MySQLUserFactory implements UserFactory {
    private static MySQLUserFactory instance;

    private MySQLUserFactory(){}

    public static MySQLUserFactory getInstance() {
        if (instance == null) {
            instance = new MySQLUserFactory();
        }
        return instance;
    }

    @Override
    public UserDAO createUserDAO() {
        return MySQLUserDAO.getInstance();
    }

    @Override
    public FriendRequestDAO createFriendRequestDAO() {
        return MySQLFriendRequestDAO.getInstance();
    }

    @Override
    public FriendshipDAO createFriendshipDAO() {
        return MySQLFriendshipDAO.getInstance();
    }

    @Override
    public CategoryDAO createCategoryDAO() {
        return MySQLCategoryDAO.getInstance();
    }

    @Override
    public PostDAO createPostDAO() {
        return MySQLPostDAO.getInstance();
    }
    
    @Override
    public CartDAO createCartDAO() {
        return MySQLCartDAO.getInstance();
    }
    
    @Override
    public TutorDAO createTutorDAO() {
        return MySQLTutorDAO.getInstance();
    }
    
    @Override
    public SessionBookingDAO createSessionBookingDAO() {
        return MySQLSessionBookingDAO.getInstance();
    }
}

