package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UserDoesNotExist;
import dev.studylink.studylink.exception.UserAlreadyExists;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;


public class UserManager {
    private static UserManager instance;
    private UserFactory userFactory;
    private UserDAO userDAO;

    private UserManager(UserFactory userFactory) {
        this.userFactory = userFactory;
        this.userDAO = userFactory.createUserDAO();
    }

    public static UserManager getInstance(UserFactory userFactory) {
        if (instance == null) {
            instance = new UserManager(userFactory);
        }
        return instance;
    }

    public String hash(String passwordClear) {
        return BCrypt.hashpw(passwordClear, BCrypt.gensalt());

    }

    public boolean doesPasswordMatch(String input, String hashed) {
        return BCrypt.checkpw(input, hashed);
    }

    public boolean doesUserExist(String email) {
        return userDAO.findByEmail(email).isPresent();
    }

    public Boolean register(String password, String email, String fullname) throws UserAlreadyExists{
        if (doesUserExist(email)) {
            throw new UserAlreadyExists("Un utilisateur avec l'email " + email + " existe déjà");
        }
        String passwordHash = hash(password);
        boolean userCreated = userDAO.createUser(fullname, email, passwordHash);
        return userCreated;
    }

    public User login(String password, String email) throws LoginError, UserDoesNotExist {
        User user = userDAO.findByEmail(email).orElseThrow(() -> new UserDoesNotExist("User does not exist"));
        if (!doesPasswordMatch(password, user.getPasswordHash())) {
            throw new LoginError("Mot de passe incorrect");
        }
        return user;
    }


}