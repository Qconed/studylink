package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class MySQLUserDAO implements UserDAO {
    private static MySQLUserDAO instance;

    private MySQLUserDAO() {}

    public static synchronized MySQLUserDAO getInstance() {
        if (instance == null) {
            instance = new MySQLUserDAO();
        }
        return instance;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = buildUserFromResultSet(rs);
                // Load categories for this user
                user.setCategories(getUserCategories(user.getId()));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = buildUserFromResultSet(rs);
                user.setCategories(getUserCategories(user.getId()));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean createUser(String fullname, String email, String passwordHash) {
        String sql = "INSERT INTO users (fullname, email, password, role) VALUES (?, ?, ?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, fullname);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.setString(4, Role.STUDENT.toString());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET fullname = ?, email = ?, role = ?, bio = ?, " +
                "avatar_path = ?, is_suspended = ?, suspension_reason = ? WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullname());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().toString());
            stmt.setString(4, user.getBio());
            stmt.setString(5, user.getAvatarPath());
            stmt.setBoolean(6, user.isSuspended());
            stmt.setString(7, user.getSuspensionReason());
            stmt.setInt(8, user.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public User[] getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY fullname";
        List<User> users = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = buildUserFromResultSet(rs);
                user.setCategories(getUserCategories(user.getId()));
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }

        return users.toArray(new User[0]);
    }

    @Override
    public List<User> searchUsers(String query) {
        String sql = "SELECT * FROM users WHERE fullname LIKE ? OR email LIKE ? ORDER BY fullname";
        List<User> users = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = buildUserFromResultSet(rs);
                user.setCategories(getUserCategories(user.getId()));
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public List<User> searchUsersByFullname(String fullname) {
        String sql = "SELECT * FROM users WHERE fullname LIKE ? ORDER BY fullname";
        List<User> users = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + fullname + "%";
            stmt.setString(1, searchPattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                User user = buildUserFromResultSet(rs);
                user.setCategories(getUserCategories(user.getId()));
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Error searching users by fullname: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public boolean updateUserRole(int userId, Role role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role.toString());
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean suspendUser(int userId, String reason) {
        String sql = "UPDATE users SET is_suspended = TRUE, suspension_reason = ? WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reason);
            stmt.setInt(2, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error suspending user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean unsuspendUser(int userId) {
        String sql = "UPDATE users SET is_suspended = FALSE, suspension_reason = NULL WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error unsuspending user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Category> getUserCategories(int userId) {
        String sql = "SELECT c.* FROM categories c " +
                "INNER JOIN user_categories uc ON c.id = uc.category_id " +
                "WHERE uc.user_id = ?";
        List<Category> categories = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        CategoryType.fromString(rs.getString("type")),
                        rs.getInt("level")
                );
                categories.add(category);
            }

        } catch (SQLException e) {
            System.err.println("Error getting user categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    @Override
    public boolean addCategoryToUser(int userId, int categoryId) {
        String sql = "INSERT IGNORE INTO user_categories (user_id, category_id) VALUES (?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding category to user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeCategoryFromUser(int userId, int categoryId) {
        String sql = "DELETE FROM user_categories WHERE user_id = ? AND category_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, categoryId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error removing category from user: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login_at = NOW() WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    // Helper method to build User from ResultSet
    private User buildUserFromResultSet(ResultSet rs) throws SQLException {
        Timestamp lastLoginTimestamp = rs.getTimestamp("last_login_at");
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");

        return new User(
                rs.getInt("id"),
                rs.getString("fullname"),
                rs.getString("email"),
                rs.getString("password"),
                Role.fromString(rs.getString("role")),
                rs.getString("bio"),
                rs.getString("avatar_path"),
                createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : LocalDateTime.now(),
                lastLoginTimestamp != null ? lastLoginTimestamp.toLocalDateTime() : null,
                rs.getBoolean("is_suspended"),
                rs.getString("suspension_reason")
        );
    }
}

