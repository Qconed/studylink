
package dev.studylink.studylink.dao;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    /**
     * Trouve un utilisateur par son username
     */
    Optional<User> findByEmail(String email);

    /**
     * Trouve un utilisateur par ID
     */
    Optional<User> findById(int id);

    /**
     * Crée un nouvel utilisateur
     */
    boolean createUser(String fullname, String email, String passwordHash);

    /**
     * Update a user
     */
    boolean updateUser(User user);

    /**
     * Delete a user
     */
    boolean deleteUser(int userId);

    /**
     * renvoie tous les utilisateurs
     */
    User[] getAllUsers();

    /**
     * Search users by query (fullname or email)
     */
    List<User> searchUsers(String query);

    /**
     * Search users by fullname only
     */
    List<User> searchUsersByFullname(String fullname);

    /**
     * Update user role
     */
    boolean updateUserRole(int userId, Role role);

    /**
     * Suspend a user
     */
    boolean suspendUser(int userId, String reason);

    /**
     * Unsuspend a user
     */
    boolean unsuspendUser(int userId);

    /**
     * Get all categories for a user
     */
    List<Category> getUserCategories(int userId);

    /**
     * Add a category to a user
     */
    boolean addCategoryToUser(int userId, int categoryId);

    /**
     * Remove a category from a user
     */
    boolean removeCategoryFromUser(int userId, int categoryId);

    /**
     * Update last login timestamp
     */
    boolean updateLastLogin(int userId);

    /**
     * Ferme les ressources
     */
    void close();
}
