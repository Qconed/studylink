package dev.studylink.studylink.business;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UserManager {
    private static UserManager instance;
    private UserFactory userFactory;
    private UserDAO userDAO;

    private UserManager(UserFactory userFactory) {
        this.userFactory = userFactory;
        this.userDAO = userFactory.createUserDao();
    }

    public static synchronized UserManager getInstance(UserFactory userFactory) {
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

    public boolean doesUserExist(String username) {
        try {
            userDAO.getUserByUsername(username);
            return true;
        } catch (UserDoesNotExist e) {
            return false;
        }
    }

    public User login(String password, String username) throws LoginError {
        try {
            User user = userDAO.getUserByUsername(username);
            if (!doesPasswordMatch(password, user.getPasswordHash())) {
                throw new LoginError("Mot de passe incorrect");
            }
            return user;
        } catch (UserDoesNotExist e) {
            throw new LoginError("Utilisateur introuvable: " + username);
        }
    }
}