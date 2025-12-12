package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UserDoesNotExist;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(passwordClear.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    public boolean doesPasswordMatch(String input, String hashed) {
        return hash(input).equals(hashed);
    }
    // todo on remplace Username par email
    public boolean doesUserExist(String username) {
        return userDAO.findByUsername(username).isPresent();
    }

    //todo on remplace Username par email
    public User login(String password, String username) throws LoginError,UserDoesNotExist {
        User user = userDAO.findByUsername(username).orElseThrow(() -> new UserDoesNotExist("User does not exist"));
        if (!doesPasswordMatch(password, user.getPasswordHash())) {
            throw new LoginError("Mot de passe incorrect");
        }
        return user;
    }
    // todo on remplace Username par email
    public Boolean register(String password, String username) {

        return true; // TODO : implement the register function
    }
}