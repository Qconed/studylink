
package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.UserFactory;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UnauthorizedException;
import dev.studylink.studylink.exception.UserAlreadyExists;
import dev.studylink.studylink.exception.UserDoesNotExist;
import dev.studylink.studylink.impl.db.mysql.MySQLUserFactory;

import java.io.File;
import java.util.List;

// Session facade will allow to the UI to easily use the business logic (for now there is only the user management)
// follows the singleton desing pattern
public class SessionFacade {
    private static SessionFacade instance = null;
    private UserFactory userFactory = MySQLUserFactory.getInstance();
    private UserManager userManager = UserManager.getInstance(userFactory); // delegate for the user management. But only need to know the UserFactory, not the concrete implementation of it
    private NotificationManager notificationManager = NotificationManager.getInstance(); // delegate for notification management
    private User currentUser = null; // to track the logged-in user

    private SessionFacade() {}

    public static SessionFacade getInstance() {
        if (instance == null) {
            instance = new SessionFacade();
        }
        return instance;
    }


    public User login(String password ,String email) throws LoginError, UserDoesNotExist {
        User user = userManager.login(password, email);
        this.currentUser = user;
        return user;
    }

    public boolean register(String password, String email, String fullname) throws UserAlreadyExists{
        return userManager.register(password, email, fullname);
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // ===== PROFILE MANAGEMENT =====
    public boolean updateProfile(int userId, String fullname, String bio) {
        return userManager.updateUserProfile(userId, fullname, bio, null);
    }

    public boolean updateEmail(int userId, String newEmail) throws UserAlreadyExists {
        return userManager.updateUserEmail(userId, newEmail);
    }

    public boolean updatePassword(int userId, String oldPassword, String newPassword) throws LoginError {
        return userManager.updateUserPassword(userId, oldPassword, newPassword);
    }

    public boolean uploadAvatar(int userId, File avatarFile) {
        // TODO: Implement file upload logic or not  , c a voir
        String avatarPath = "/avatars/" + userId + "_" + avatarFile.getName();
        try {
            User user = userManager.getUserById(userId);
            user.setAvatarPath(avatarPath);
            return userManager.updateUserProfile(userId, user.getFullname(), user.getBio(), avatarPath);
        } catch (UserDoesNotExist e) {
            return false;
        }
    }

    public boolean addCategoryToUser(int userId, Category category) {
        return userManager.addCategoryToUser(userId, category);
    }

    public boolean removeCategoryFromUser(int userId, int categoryId) {
        return userManager.removeCategoryFromUser(userId, categoryId);
    }

    public User getUserById(int userId) throws UserDoesNotExist {
        return userManager.getUserById(userId);
    }

    // ===== FRIEND MANAGEMENT =====
    public List<User> searchUsers(String query) {
        return userManager.searchUsersByQuery(query);
    }

    public boolean sendFriendRequest(int senderId, int receiverId) {
        FriendRequest request = userManager.createFriendRequest(senderId, receiverId);
        return request != null;
    }

    public boolean acceptFriendRequest(int requestId) {
        return userManager.acceptFriendRequest(requestId);
    }

    public boolean rejectFriendRequest(int requestId) {
        return userManager.rejectFriendRequest(requestId);
    }

    public List<FriendRequest> getFriendRequests(int userId) {
        return userManager.getFriendRequestsForUser(userId);
    }

    public List<FriendRequest> getSentRequests(int userId) {
        return userManager.getSentRequestsByUser(userId);
    }

    public List<User> getFriends(int userId) {
        return userManager.getFriendsForUser(userId);
    }

    public boolean removeFriend(int userId, int friendId) {
        return userManager.removeFriendship(userId, friendId);
    }



    // ===== ADMIN FUNCTIONS =====
    public boolean changeUserRole(int adminId, int targetUserId, Role newRole) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.changeRole(targetUserId, newRole);
    }

    public boolean suspendUser(int adminId, int targetUserId, String reason) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.suspendUser(targetUserId, reason);
    }

    public boolean unsuspendUser(int adminId, int targetUserId) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.unsuspendUser(targetUserId);
    }

    public boolean deleteUser(int adminId, int targetUserId) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.deleteUser(targetUserId);
    }

    public List<User> getAllUsers(int adminId) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.getAllUsers();
    }

    // ===== CATEGORY MANAGEMENT =====
    public List<Category> getAllCategories() {
        return userManager.getAllCategories();
    }

    public List<Category> getCategoriesByType(CategoryType type) {
        return userManager.getCategoriesByType(type);
    }

    public boolean createCategory(int adminId, String title, String description,
                                  CategoryType type, int level) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.createCategory(title, description, type, level);
    }

    public boolean updateCategory(int adminId, Category category) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.updateCategory(category);
    }

    public boolean deleteCategory(int adminId, int categoryId) throws UnauthorizedException {
        if (!userManager.isAdmin(adminId)) {
            throw new UnauthorizedException("Vous n'avez pas les droits d'administrateur");
        }
        return userManager.deleteCategory(categoryId);
    }

    // ===== NOTIFICATION MANAGEMENT =====
    
    /**
     * Crée une nouvelle notification pour un utilisateur
     */
    public boolean createNotification(int userId, String content) {
        return notificationManager.createNotification(userId, content);
    }

    /**
     * Récupère toutes les notifications de l'utilisateur actuel
     */
    public List<Notification> getMyNotifications() {
        if (currentUser == null) {
            return List.of();
        }
        return notificationManager.getUserNotifications(currentUser.getId());
    }

    /**
     * Récupère toutes les notifications d'un utilisateur spécifique
     */
    public List<Notification> getUserNotifications(int userId) {
        return notificationManager.getUserNotifications(userId);
    }

    /**
     * Récupère les notifications non lues de l'utilisateur actuel
     */
    public List<Notification> getMyUnreadNotifications() {
        if (currentUser == null) {
            return List.of();
        }
        return notificationManager.getUnreadNotifications(currentUser.getId());
    }

    /**
     * Récupère les notifications non lues d'un utilisateur spécifique
     */
    public List<Notification> getUnreadNotifications(int userId) {
        return notificationManager.getUnreadNotifications(userId);
    }

    /**
     * Compte les notifications non lues de l'utilisateur actuel
     */
    public int getMyUnreadCount() {
        if (currentUser == null) {
            return 0;
        }
        return notificationManager.getUnreadCount(currentUser.getId());
    }

    /**
     * Compte les notifications non lues d'un utilisateur spécifique
     */
    public int getUnreadCount(int userId) {
        return notificationManager.getUnreadCount(userId);
    }

    /**
     * Marque une notification comme lue
     */
    public boolean markNotificationAsRead(int notificationId) {
        return notificationManager.markNotificationAsRead(notificationId);
    }

    /**
     * Marque toutes les notifications de l'utilisateur actuel comme lues
     */
    public boolean markAllMyNotificationsAsRead() {
        if (currentUser == null) {
            return false;
        }
        return notificationManager.markAllNotificationsAsRead(currentUser.getId());
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    public boolean markAllNotificationsAsRead(int userId) {
        return notificationManager.markAllNotificationsAsRead(userId);
    }

    /**
     * Supprime une notification
     */
    public boolean deleteNotification(int notificationId) {
        return notificationManager.deleteNotification(notificationId);
    }

    /**
     * Supprime toutes les notifications de l'utilisateur actuel
     */
    public boolean deleteAllMyNotifications() {
        if (currentUser == null) {
            return false;
        }
        return notificationManager.deleteAllUserNotifications(currentUser.getId());
    }

    /**
     * Supprime toutes les notifications d'un utilisateur
     */
    public boolean deleteAllUserNotifications(int userId) {
        return notificationManager.deleteAllUserNotifications(userId);
    }

    /**
     * Vérifie si l'utilisateur actuel a des notifications non lues
     */
    public boolean hasUnreadNotifications() {
        if (currentUser == null) {
            return false;
        }
        return notificationManager.hasUnreadNotifications(currentUser.getId());
    }

    /**
     * Envoie une notification de demande d'ami
     */
    public boolean notifyFriendRequest(int receiverId, String senderName) {
        return notificationManager.notifyFriendRequest(receiverId, senderName);
    }

    /**
     * Envoie une notification d'acceptation de demande d'ami
     */
    public boolean notifyFriendRequestAccepted(int userId, String friendName) {
        return notificationManager.notifyFriendRequestAccepted(userId, friendName);
    }

    /**
     * Envoie une notification de nouvelle ressource
     */
    public boolean notifyNewResource(int userId, String resourceTitle) {
        return notificationManager.notifyNewResource(userId, resourceTitle);
    }

    /**
     * Envoie une notification de nouvelle session d'étude
     */
    public boolean notifyNewStudySession(int userId, String sessionTitle) {
        return notificationManager.notifyNewStudySession(userId, sessionTitle);
    }
}