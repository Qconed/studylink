package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Chat;
import dev.studylink.studylink.business.ChatType;
import dev.studylink.studylink.business.Role;
import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.ChatDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLChatDAO implements ChatDAO {
    private static MySQLChatDAO instance;

    private MySQLChatDAO() {}

    public static synchronized MySQLChatDAO getInstance() {
        if (instance == null) {
            instance = new MySQLChatDAO();
        }
        return instance;
    }

    @Override
    public boolean createChat(Chat chat) {
        String sql = "INSERT INTO chats (type, name, created_by_id, created_at, last_message_at) " +
                "VALUES (?, ?, ?, NOW(), NOW())";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, chat.getType().toString());
            stmt.setString(2, chat.getName());
            stmt.setInt(3, chat.getCreatedById());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    chat.setId(rs.getInt(1));

                    // Ajouter le créateur comme participant
                    for (User participant : chat.getParticipants()) {
                        addParticipant(chat.getId(), participant.getId());
                    }
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur création chat: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Optional<Chat> findById(int id) {
        String sql = "SELECT id, type, name, created_by_id, created_at, last_message_at, is_active " +
                "FROM chats WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Chat chat = new Chat(
                        rs.getInt("id"),
                        ChatType.valueOf(rs.getString("type")),
                        rs.getString("name"),
                        rs.getInt("created_by_id")
                );
                chat.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                chat.setLastMessageAt(rs.getTimestamp("last_message_at").toLocalDateTime());
                chat.setActive(rs.getBoolean("is_active"));

                // Charger les participants
                chat.setParticipants(getParticipants(id));

                return Optional.of(chat);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération chat: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Chat> getChatsByUser(int userId) {
        String sql = "SELECT DISTINCT c.id, c.last_message_at FROM chats c " +
                "INNER JOIN chat_participants cp ON c.id = cp.chat_id " +
                "WHERE cp.user_id = ? AND c.is_active = TRUE " +
                "ORDER BY c.last_message_at DESC";

        List<Chat> chats = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Optional<Chat> chat = findById(rs.getInt("id"));
                chat.ifPresent(chats::add);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération chats: " + e.getMessage());
            e.printStackTrace();
        }

        return chats;
    }

    @Override
    public boolean updateChat(Chat chat) {
        String sql = "UPDATE chats SET name = ?, last_message_at = ?, is_active = ? WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chat.getName());
            stmt.setTimestamp(2, Timestamp.valueOf(chat.getLastMessageAt()));
            stmt.setBoolean(3, chat.isActive());
            stmt.setInt(4, chat.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour chat: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteChat(int chatId) {
        String sql = "UPDATE chats SET is_active = FALSE WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur suppression chat: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean addParticipant(int chatId, int userId) {
        String sql = "INSERT INTO chat_participants (chat_id, user_id, joined_at) " +
                "VALUES (?, ?, NOW()) " +
                "ON CONFLICT (chat_id, user_id) DO NOTHING";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur ajout participant: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeParticipant(int chatId, int userId) {
        String sql = "DELETE FROM chat_participants WHERE chat_id = ? AND user_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur retrait participant: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<User> getParticipants(int chatId) {
        String sql = "SELECT u.id, u.fullname, u.email, u.role, u.bio, u.avatar_path, " +
                "u.created_at, u.last_login_at, u.is_suspended, u.suspension_reason " +
                "FROM users u " +
                "INNER JOIN chat_participants cp ON u.id = cp.user_id " +
                "WHERE cp.chat_id = ?";

        List<User> participants = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Timestamp lastLoginTs = rs.getTimestamp("last_login_at");
                Timestamp createdAtTs = rs.getTimestamp("created_at");

                User user = new User(
                        rs.getInt("id"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        "", // password non chargé
                        Role.fromString(rs.getString("role")),
                        rs.getString("bio"),
                        rs.getString("avatar_path"),
                        createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now(),
                        lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null,
                        rs.getBoolean("is_suspended"),
                        rs.getString("suspension_reason")
                );
                participants.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération participants: " + e.getMessage());
            e.printStackTrace();
        }

        return participants;
    }

    @Override
    public void close() {
        Connection.close();
    }
}