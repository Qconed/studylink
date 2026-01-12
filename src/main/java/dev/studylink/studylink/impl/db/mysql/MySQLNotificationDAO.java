package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Notification;
import dev.studylink.studylink.business.NotificationType;
import dev.studylink.studylink.dao.NotificationDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLNotificationDAO implements NotificationDAO {
    private static MySQLNotificationDAO instance;

    private MySQLNotificationDAO() {}

    public static synchronized MySQLNotificationDAO getInstance() {
        if (instance == null) {
            instance = new MySQLNotificationDAO();
        }
        return instance;
    }

    @Override
    public boolean createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, type, chat_id, related_message_id, created_at, is_read) " +
                "VALUES (?, ?, ?, ?, NOW(), FALSE)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getType().toString());
            stmt.setInt(3, notification.getChatId());

            if (notification.getRelatedMessageId() != null) {
                stmt.setInt(4, notification.getRelatedMessageId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur création notification: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Notification> getUnreadNotifications(int userId) {
        String sql = "SELECT id, user_id, type, chat_id, related_message_id, created_at, is_read " +
                "FROM notifications WHERE user_id = ? AND is_read = FALSE " +
                "ORDER BY created_at DESC";

        List<Notification> notifications = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Notification notif = buildNotificationFromResultSet(rs);
                notifications.add(notif);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération notifications: " + e.getMessage());
            e.printStackTrace();
        }

        return notifications;
    }

    @Override
    public boolean markAsRead(int notificationId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur marquage notification: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteNotification(int notificationId) {
        String sql = "DELETE FROM notifications WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur suppression notification: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    private Notification buildNotificationFromResultSet(ResultSet rs) throws SQLException {
        Timestamp createdTs = rs.getTimestamp("created_at");

        Notification notif = new Notification();
        notif.setId(rs.getInt("id"));
        notif.setUserId(rs.getInt("user_id"));
        notif.setType(NotificationType.valueOf(rs.getString("type")));
        notif.setChatId(rs.getInt("chat_id"));

        int messageId = rs.getInt("related_message_id");
        if (messageId > 0) {
            notif.setRelatedMessageId(messageId);
        }

        if (createdTs != null) {
            notif.setCreatedAt(createdTs.toLocalDateTime());
        }

        notif.setRead(rs.getBoolean("is_read"));

        return notif;
    }
}