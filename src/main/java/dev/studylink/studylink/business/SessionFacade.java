
package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UserAlreadyExists;
import dev.studylink.studylink.exception.UserDoesNotExist;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.impl.db.mysql.MySQLUserDAO;
import dev.studylink.studylink.business.UserManager;

// Session facade will allow to the UI to easiliy use the business logic (for now there is only the user management)
// follows the singleton desing pattern
public class SessionFacade {
    private static SessionFacade instance = null;
    private UserFactory userFactory = MySQLUserFactory.getInstance();
    private UserManager userManager = UserManager.getInstance(userFactory); // delegate for the user management. But only need to know the UserFactory, not the concrete implementation of it

    // CONSTRUCTION -------------------
    private SessionFacade() {}

    // methode d'obtention d'instance singleton
    public static SessionFacade getInstance() {
        if (instance == null) {
            instance = new SessionFacade();
        }
        return instance;
    }


    public User login(String password ,String email) throws LoginError, UserDoesNotExist {
        return userManager.login(password, email);
    }



    public boolean register(String password, String email, String fullname) throws UserAlreadyExists{
        return userManager.register(password, email, fullname);
    }
}