package dev.studylink.studylink.business;

import dev.studylink.studylink.dao.*;
import dev.studylink.studylink.exception.LoginError;
import dev.studylink.studylink.exception.UnauthorizedException;
import dev.studylink.studylink.exception.UserDoesNotExist;
import dev.studylink.studylink.exception.UserAlreadyExists;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;

public class UserManager {
    private static UserManager instance;
    private UserFactory userFactory;
    private UserDAO userDAO;
    private FriendRequestDAO friendRequestDAO;
    private FriendshipDAO friendshipDAO;
    private CategoryDAO categoryDAO;

    private UserManager(UserFactory userFactory) {
        this.userFactory = userFactory;
        this.userDAO = userFactory.createUserDAO();
        this.friendRequestDAO = userFactory.createFriendRequestDAO();
        this.friendshipDAO = userFactory.createFriendshipDAO();
        this.categoryDAO = userFactory.createCategoryDAO();
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

    public boolean doesUserExistById(int userId) {
        return userDAO.findById(userId).isPresent();
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
        if (user.isSuspended()) {
            throw new LoginError("Compte suspendu: " + user.getSuspensionReason());
        }
        if (!doesPasswordMatch(password, user.getPasswordHash())) {
            throw new LoginError("Mot de passe incorrect");
        }
        userDAO.updateLastLogin(user.getId());
        return user;
    }

    // ===== PROFILE MANAGEMENT =====
    public boolean updateUserProfile(int userId, String fullname, String bio, String avatarPath) {
        try {
            User user = getUserById(userId);
            user.setFullname(fullname);
            user.setBio(bio);
            user.setAvatarPath(avatarPath);
            return userDAO.updateUser(user);
        } catch (UserDoesNotExist e) {
            return false;
        }
    }

    public boolean updateUserEmail(int userId, String newEmail) throws UserAlreadyExists {
        if (doesUserExist(newEmail)) {
            throw new UserAlreadyExists("Cet email est déjà utilisé");
        }
        try {
            User user = getUserById(userId);
            user.setEmail(newEmail);
            return userDAO.updateUser(user);
        } catch (UserDoesNotExist e) {
            return false;
        }
    }

    public boolean updateUserPassword(int userId, String oldPassword, String newPassword) throws LoginError {
        try {
            User user = getUserById(userId);
            if (!doesPasswordMatch(oldPassword, user.getPasswordHash())) {
                throw new LoginError("Ancien mot de passe incorrect");
            }
            String newPasswordHash = hash(newPassword);
            return true;
        } catch (UserDoesNotExist e) {
            return false;
        }
    }

    public boolean addCategoryToUser(int userId, Category category) {
        return userDAO.addCategoryToUser(userId, category.getId());
    }

    public boolean removeCategoryFromUser(int userId, int categoryId) {
        return userDAO.removeCategoryFromUser(userId, categoryId);
    }

    public User getUserById(int userId) throws UserDoesNotExist {
        return userDAO.findById(userId)
                .orElseThrow(() -> new UserDoesNotExist("Utilisateur introuvable"));
    }

    public List<User> searchUsersByQuery(String query) {
        return userDAO.searchUsers(query);
    }

    // ===== FRIEND MANAGEMENT =====
    public FriendRequest createFriendRequest(int senderId, int receiverId) {
        // Check if already friends
        if (friendshipDAO.areFriends(senderId, receiverId)) {
            return null;
        }

        // Check if request already exists
        if (friendRequestDAO.requestExists(senderId, receiverId)) {
            return null;
        }

        return friendRequestDAO.createFriendRequest(senderId, receiverId);
    }

    public boolean acceptFriendRequest(int requestId) {
        try {
            FriendRequest request = friendRequestDAO.findById(requestId)
                    .orElseThrow(() -> new Exception("Request not found"));

            // Update request status
            boolean statusUpdated = friendRequestDAO.updateRequestStatus(
                    requestId, FriendRequestStatus.ACCEPTED);

            if (statusUpdated) {
                // Create friendship
                return friendshipDAO.createFriendship(
                        request.getSenderId(), request.getReceiverId());
            }

            return false;
        } catch (Exception e) {
            System.err.println("Error accepting friend request: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectFriendRequest(int requestId) {
        return friendRequestDAO.updateRequestStatus(requestId, FriendRequestStatus.REJECTED);
    }

    public List<FriendRequest> getFriendRequestsForUser(int userId) {
        return friendRequestDAO.getPendingRequestsForUser(userId);
    }

    public List<FriendRequest> getSentRequestsByUser(int userId) {
        return friendRequestDAO.getSentRequestsByUser(userId);
    }

    public List<User> getFriendsForUser(int userId) {
        return friendshipDAO.getFriendsForUser(userId);
    }

    public boolean removeFriendship(int userId, int friendId) {
        return friendshipDAO.removeFriendship(userId, friendId);
    }

    // ===== ADMIN FUNCTIONS =====
    public boolean isAdmin(int userId) {
        try {
            User user = getUserById(userId);
            return user.getRole() == Role.ADMIN;
        } catch (UserDoesNotExist e) {
            return false;
        }
    }

    public boolean changeRole(int targetUserId, Role newRole) {
        return userDAO.updateUserRole(targetUserId, newRole);
    }

    public boolean suspendUser(int targetUserId, String reason) {
        return userDAO.suspendUser(targetUserId, reason);
    }

    public boolean unsuspendUser(int targetUserId) {
        return userDAO.unsuspendUser(targetUserId);
    }

    public boolean deleteUser(int targetUserId) {
        return userDAO.deleteUser(targetUserId);
    }

    public List<User> getAllUsers() {
        User[] usersArray = userDAO.getAllUsers();
        return List.of(usersArray);
    }

    // ===== CATEGORY MANAGEMENT =====
    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public List<Category> getCategoriesByType(CategoryType type) {
        return categoryDAO.getCategoriesByType(type);
    }

    public boolean createCategory(String title, String description, CategoryType type, int level) {
        return categoryDAO.createCategory(title, description, type, level);
    }

    public boolean updateCategory(Category category) {
        return categoryDAO.updateCategory(category);
    }

    public boolean deleteCategory(int categoryId) {
        return categoryDAO.deleteCategory(categoryId);
    }





}