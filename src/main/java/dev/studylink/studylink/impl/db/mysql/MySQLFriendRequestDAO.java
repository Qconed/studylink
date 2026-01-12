package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.FriendRequest;
import dev.studylink.studylink.business.FriendRequestStatus;
import dev.studylink.studylink.dao.FriendRequestDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLFriendRequestDAO implements FriendRequestDAO {
    private static MySQLFriendRequestDAO instance;

    private MySQLFriendRequestDAO() {}

    public static synchronized MySQLFriendRequestDAO getInstance() {
        if (instance == null) {
            instance = new MySQLFriendRequestDAO();
        }
        return instance;
    }

    @Override
    public FriendRequest createFriendRequest(int senderId, int receiverId) {
        String sql = "INSERT INTO friend_requests (sender_id, receiver_id, status) VALUES (?, ?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, FriendRequestStatus.PENDING.toString());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int requestId = rs.getInt(1);
                    return new FriendRequest(requestId, senderId, receiverId, FriendRequestStatus.PENDING);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error creating friend request: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Optional<FriendRequest> findById(int requestId) {
        String sql = "SELECT fr.*, " +
                "s.fullname as sender_name, " +
                "r.fullname as receiver_name " +
                "FROM friend_requests fr " +
                "JOIN users s ON fr.sender_id = s.id " +
                "JOIN users r ON fr.receiver_id = r.id " +
                "WHERE fr.id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                FriendRequest request = buildFriendRequestFromResultSet(rs);
                return Optional.of(request);
            }

        } catch (SQLException e) {
            System.err.println("Error finding friend request by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean updateRequestStatus(int requestId, FriendRequestStatus status) {
        // Si ACCEPTED ou REJECTED, on supprime la ligne au lieu de l'updater
        if (status == FriendRequestStatus.ACCEPTED || status == FriendRequestStatus.REJECTED) {
            return deleteFriendRequest(requestId);
        }
        String sql = "UPDATE friend_requests SET status = ?, responded_at = NOW() WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.toString());
            stmt.setInt(2, requestId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating friend request status: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean deleteFriendRequest(int requestId) {
        String sql = "DELETE FROM friend_requests WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting friend request: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<FriendRequest> getPendingRequestsForUser(int userId) {
        String sql = "SELECT fr.*, " +
                "s.fullname as sender_name, " +
                "r.fullname as receiver_name " +
                "FROM friend_requests fr " +
                "JOIN users s ON fr.sender_id = s.id " +
                "JOIN users r ON fr.receiver_id = r.id " +
                "WHERE fr.receiver_id = ? AND fr.status = 'PENDING' " +
                "ORDER BY fr.created_at DESC";
        List<FriendRequest> requests = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                requests.add(buildFriendRequestFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting pending requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    @Override
    public List<FriendRequest> getSentRequestsByUser(int userId) {
        String sql = "SELECT fr.*, " +
                "s.fullname as sender_name, " +
                "r.fullname as receiver_name " +
                "FROM friend_requests fr " +
                "JOIN users s ON fr.sender_id = s.id " +
                "JOIN users r ON fr.receiver_id = r.id " +
                "WHERE fr.sender_id = ? " +
                "ORDER BY fr.created_at DESC";
        List<FriendRequest> requests = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                requests.add(buildFriendRequestFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting sent requests: " + e.getMessage());
            e.printStackTrace();
        }

        return requests;
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
    public boolean requestExists(int senderId, int receiverId) {
        // On vérifie seulement les demandes PENDING
        // (car ACCEPTED/REJECTED sont supprimées maintenant)
        String sql = "SELECT COUNT(*) FROM friend_requests " +
                "WHERE ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                "AND status = 'PENDING'";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking request existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    private FriendRequest buildFriendRequestFromResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        Timestamp respondedAtTimestamp = rs.getTimestamp("responded_at");

        FriendRequest request = new FriendRequest(
                rs.getInt("id"),
                rs.getInt("sender_id"),
                rs.getInt("receiver_id"),
                FriendRequestStatus.fromString(rs.getString("status")),
                createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : LocalDateTime.now(),
                respondedAtTimestamp != null ? respondedAtTimestamp.toLocalDateTime() : null
        );

        request.setSenderName(rs.getString("sender_name"));
        request.setReceiverName(rs.getString("receiver_name"));

        return request;
    }
}