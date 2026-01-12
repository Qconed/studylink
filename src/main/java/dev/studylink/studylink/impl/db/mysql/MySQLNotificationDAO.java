package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Notification;
import dev.studylink.studylink.dao.NotificationDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation MySQL du DAO pour les notifications
 */
public class MySQLNotificationDAO implements NotificationDAO {

    @Override
    public boolean createNotification(Notification notification) {
        String sql = "INSERT INTO notifications (user_id, content, timestamp, is_read) VALUES (?, ?, ?, ?)";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getContent());
            stmt.setTimestamp(3, Timestamp.valueOf(notification.getTimestamp()));
            stmt.setBoolean(4, notification.isRead());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    @Override
    public List<Notification> findByUserId(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY timestamp DESC";
        List<Notification> notifications = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Notification notification = new Notification(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getBoolean("is_read")
                );
                notifications.add(notification);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return notifications;
    }

    @Override
    public List<Notification> findUnreadByUserId(int userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY timestamp DESC";
        List<Notification> notifications = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Notification notification = new Notification(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("content"),
                    rs.getTimestamp("timestamp").toLocalDateTime(),
                    rs.getBoolean("is_read")
                );
                notifications.add(notification);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des notifications non lues: " + e.getMessage());
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
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage de la notification comme lue: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    @Override
    public boolean markAllAsReadForUser(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows >= 0; // Retourne true même si aucune notification n'était non lue
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage de toutes les notifications comme lues: " + e.getMessage());
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
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la notification: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    @Override
    public boolean deleteAllForUser(int userId) {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows >= 0; // Retourne true même si aucune notification n'existait
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de toutes les notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }

    @Override
    public int countUnreadNotifications(int userId) {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = FALSE";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des notifications non lues: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0;
    }

    @Override
    public void close() {
        Connection.close();
    }
}
