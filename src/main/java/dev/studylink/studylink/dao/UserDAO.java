
package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.User;

import java.util.Optional;

public interface UserDAO {
    /**
     * Trouve un utilisateur par son username
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Crée un nouvel utilisateur
     */
    boolean createUser(String fullname, String email, String passwordHash);

    /**
     * renvoie tous les utilisateurs
     */
    User[] getAllUsers();
    /**
     * Ferme les ressources
     */
    void close();
}
