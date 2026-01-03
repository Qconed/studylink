package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.FriendshipDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLFriendshipDAO implements FriendshipDAO {
    private static MySQLFriendshipDAO instance;

    private MySQLFriendshipDAO() {}

    public static synchronized MySQLFriendshipDAO getInstance() {
        if (instance == null) {
            instance = new MySQLFriendshipDAO();
        }
        return instance;
    }

    @Override
    public boolean createFriendship(int userId1, int userId2) {
        // Ensure user1_id < user2_id for consistency
        int user1 = Math.min(userId1, userId2);
        int user2 = Math.max(userId1, userId2);

        String sql = "INSERT INTO friendships (user1_id, user2_id) VALUES (?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user1);
            stmt.setInt(2, user2);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error creating friendship: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeFriendship(int userId1, int userId2) {
        String sql = "DELETE FROM friendships " +
                "WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int min = Math.min(userId1, userId2);
            int max = Math.max(userId1, userId2);

            stmt.setInt(1, min);
            stmt.setInt(2, max);
            stmt.setInt(3, max);
            stmt.setInt(4, min);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error removing friendship: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<User> getFriendsForUser(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN friendships f ON (u.id = f.user1_id OR u.id = f.user2_id) " +
                "WHERE (f.user1_id = ? OR f.user2_id = ?) AND u.id != ? " +
                "ORDER BY u.fullname";
        List<User> friends = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                friends.add(buildUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting friends: " + e.getMessage());
            e.printStackTrace();
        }

        return friends;
    }

    @Override
    public boolean areFriends(int userId1, int userId2) {
        String sql = "SELECT COUNT(*) FROM friendships " +
                "WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int min = Math.min(userId1, userId2);
            int max = Math.max(userId1, userId2);

            stmt.setInt(1, min);
            stmt.setInt(2, max);
            stmt.setInt(3, max);
            stmt.setInt(4, min);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking friendship: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

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