package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.BlockDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLBlockDAO implements BlockDAO {
    private static MySQLBlockDAO instance;

    private MySQLBlockDAO() {}

    public static synchronized MySQLBlockDAO getInstance() {
        if (instance == null) {
            instance = new MySQLBlockDAO();
        }
        return instance;
    }

    @Override
    public boolean blockUser(int chatId, int blockedId, int blockerId) {
        String sql = "INSERT INTO chat_blocks (chat_id, blocked_user_id, blocker_user_id, created_at) " +
                "VALUES (?, ?, ?, NOW()) " +
                "ON CONFLICT (chat_id, blocked_user_id, blocker_user_id) DO NOTHING";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, blockedId);
            stmt.setInt(3, blockerId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur blocage utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean unblockUser(int chatId, int blockedId, int blockerId) {
        String sql = "DELETE FROM chat_blocks WHERE chat_id = ? AND blocked_user_id = ? AND blocker_user_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, blockedId);
            stmt.setInt(3, blockerId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur déblocage utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean isUserBlocked(int chatId, int blockerId, int blockedId) {
        String sql = "SELECT COUNT(*) FROM chat_blocks " +
                "WHERE chat_id = ? AND blocker_user_id = ? AND blocked_user_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, blockerId);
            stmt.setInt(3, blockedId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Erreur vérification blocage: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<User> getBlockedUsers(int chatId, int userId) {
        String sql = "SELECT u.id, u.fullname, u.email, u.role, u.bio, u.avatar_path, " +
                "u.created_at, u.last_login_at, u.is_suspended, u.suspension_reason " +
                "FROM users u " +
                "INNER JOIN chat_blocks cb ON u.id = cb.blocked_user_id " +
                "WHERE cb.chat_id = ? AND cb.blocker_user_id = ?";

        List<User> blockedUsers = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp lastLoginTs = rs.getTimestamp("last_login_at");
                Timestamp createdAtTs = rs.getTimestamp("created_at");

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        "",
                        Role.fromString(rs.getString("role")),
                        rs.getString("bio"),
                        rs.getString("avatar_path"),
                        createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now(),
                        lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null,
                        rs.getBoolean("is_suspended"),
                        rs.getString("suspension_reason")
                );
                blockedUsers.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateurs bloqués: " + e.getMessage());
            e.printStackTrace();
        }

        return blockedUsers;
    }

    @Override
    public void close() {
        Connection.close();
    }
}