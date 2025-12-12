
package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.User;

import java.util.Optional;

public interface UserDAO {
    /**
     * Trouve un utilisateur par son username et password
     */
    // todo : à supprimer
    Optional<User> findByCredentials(String username, String password);
    
    /**
     * Trouve un utilisateur par son username
     */
    // todo on remplace Username par email
    Optional<User> findByUsername(String username);
    
    /**
     * Crée un nouvel utilisateur
     */
    boolean createUser(User user);
    
    /**
     * Ferme les ressources
     */
    void close();
    //todo : add getAllUsers
}
