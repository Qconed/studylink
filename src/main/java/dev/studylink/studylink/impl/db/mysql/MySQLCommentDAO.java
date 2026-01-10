package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Comment;
import dev.studylink.studylink.dao.CommentDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCommentDAO implements CommentDAO {
    private static MySQLCommentDAO instance;

    private MySQLCommentDAO() {}

    public static synchronized MySQLCommentDAO getInstance() {
        if (instance == null) {
            instance = new MySQLCommentDAO();
        }
        return instance;
    }

    @Override
    public Comment createComment(Comment comment) {
        String sql = "INSERT INTO comments (content, author_id, resource_id) VALUES (?, ?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getAuthorId());
            stmt.setInt(3, comment.getResourceId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    comment.setId(generatedId);

                    // Fetch complete comment with author name
                    return findById(generatedId).orElse(comment);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error creating comment: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Optional<Comment> findById(int id) {
        String sql = "SELECT c.*, u.fullname as author_name FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "WHERE c.id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Comment comment = buildCommentFromResultSet(rs);
                return Optional.of(comment);
            }

        } catch (SQLException e) {
            System.err.println("Error finding comment by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean deleteComment(int commentId) {
        String sql = "DELETE FROM comments WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting comment: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Comment> getCommentsByResource(int resourceId) {
        String sql = "SELECT c.*, u.fullname as author_name FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "WHERE c.resource_id = ? " +
                "ORDER BY c.timestamp DESC";
        List<Comment> comments = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(buildCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting comments by resource: " + e.getMessage());
            e.printStackTrace();
        }

        return comments;
    }

    @Override
    public List<Comment> getCommentsByAuthor(int userId) {
        String sql = "SELECT c.*, u.fullname as author_name FROM comments c " +
                "JOIN users u ON c.author_id = u.id " +
                "WHERE c.author_id = ? " +
                "ORDER BY c.timestamp DESC";
        List<Comment> comments = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                comments.add(buildCommentFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting comments by author: " + e.getMessage());
            e.printStackTrace();
        }

        return comments;
    }

    @Override
    public void close() {
        Connection.close();
    }

    private Comment buildCommentFromResultSet(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("timestamp");

        Comment comment = new Comment(
                rs.getInt("id"),
                rs.getString("content"),
                rs.getInt("author_id"),
                rs.getInt("resource_id"),
                timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now()
        );

        comment.setAuthorName(rs.getString("author_name"));

        return comment;
    }
}
