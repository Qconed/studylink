package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UserDoesNotExist;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;

// Session facade will allow to the UI to easiliy use the business logic (for now there is only the user management)
// follows the singleton desing pattern
public class SessionFacade {
    private static SessionFacade instance = null;
    private UserManager userManager = UserManager.getInstance((UserFactory) MySQLUserFactory.getInstance()); // delegate for the user management. But only need to know the UserFactory, not the concrete implementation of it

    // CONSTRUCTION -------------------
    private SessionFacade() {}

    // methode d'obtention d'instance singleton
    public static SessionFacade getInstance() {
        if (instance == null) {
            instance = new SessionFacade();
        }
        return instance;
    }

    // METHODS -----------------------
    public Boolean register(String password, String username){
        return userManager.register(password, username);
    }

    public User login(String password, String username) throws LoginError, UserDoesNotExist {
        return userManager.login(password, username);
    }
}