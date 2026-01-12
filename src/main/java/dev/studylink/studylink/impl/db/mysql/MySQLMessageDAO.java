package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Message;
import dev.studylink.studylink.dao.MessageDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLMessageDAO implements MessageDAO {
    private static MySQLMessageDAO instance;

    private MySQLMessageDAO() {}

    public static synchronized MySQLMessageDAO getInstance() {
        if (instance == null) {
            instance = new MySQLMessageDAO();
        }
        return instance;
    }

    @Override
    public boolean createMessage(Message message) {
        String sql = "INSERT INTO messages (chat_id, sender_id, content, created_at) " +
                "VALUES (?, ?, ?, NOW())";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getChatId());
            stmt.setInt(2, message.getSenderId());
            stmt.setString(3, message.getContent());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    message.setId(rs.getInt(1));
                    message.setCreatedAt(LocalDateTime.now());
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur création message: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Optional<Message> findById(int id) {
        String sql = "SELECT id, chat_id, sender_id, content, created_at, edited_at, " +
                "is_edited, is_deleted FROM messages WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Message message = buildMessageFromResultSet(rs);
                return Optional.of(message);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération message: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Message> getMessagesByChat(int chatId, int limit, int offset) {
        String sql = "SELECT m.id, m.chat_id, m.sender_id, m.content, m.created_at, m.edited_at, " +
                "m.is_edited, m.is_deleted, u.fullname FROM messages m " +
                "LEFT JOIN users u ON m.sender_id = u.id " +
                "WHERE m.chat_id = ? ORDER BY m.created_at DESC LIMIT ? OFFSET ?";

        List<Message> messages = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chatId);
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(buildMessageFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération messages: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    @Override
    public boolean updateMessage(Message message) {
        String sql = "UPDATE messages SET content = ?, edited_at = NOW(), is_edited = TRUE " +
                "WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, message.getContent());
            stmt.setInt(2, message.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur mise à jour message: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteMessage(int messageId) {
        String sql = "UPDATE messages SET is_deleted = TRUE WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur suppression message: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    private Message buildMessageFromResultSet(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setChatId(rs.getInt("chat_id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setSenderFullname(rs.getString("fullname"));
        message.setContent(rs.getString("content"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            message.setCreatedAt(createdTs.toLocalDateTime());
        }

        Timestamp editedTs = rs.getTimestamp("edited_at");
        if (editedTs != null) {
            message.setEditedAt(editedTs.toLocalDateTime());
        }

        message.setEdited(rs.getBoolean("is_edited"));
        message.setDeleted(rs.getBoolean("is_deleted"));

        return message;
    }
}