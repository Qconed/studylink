package dev.studylink.studylink.impl.db.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import dev.studylink.studylink.business.Post;
import dev.studylink.studylink.business.PostComment;
import dev.studylink.studylink.dao.PostDAO;
import dev.studylink.studylink.db.Connection;

public class MySQLPostDAO implements PostDAO {
    private static MySQLPostDAO instance;

    private MySQLPostDAO() {}

    public static synchronized MySQLPostDAO getInstance() {
        if (instance == null) {
            instance = new MySQLPostDAO();
        }
        return instance;
    }

    @Override
    public boolean createPost(int userId, String content) {
        String sql = "INSERT INTO posts (user_id, content) VALUES (?, ?)";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, content);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Post> getAllPosts() {
        // On fait une jointure pour récupérer le nom de l'auteur et le nombre de commentaires
        String sql = "SELECT p.*, u.fullname, " +
                     "(SELECT COUNT(*) FROM post_comments WHERE post_id = p.id) as comments_count " +
                     "FROM posts p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "ORDER BY p.created_at DESC";
        List<Post> posts = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("created_at");
                posts.add(new Post(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("fullname"),
                    rs.getString("content"),
                    rs.getInt("likes"),
                    rs.getInt("comments_count"),
                    timestamp != null ? timestamp.toLocalDateTime() : null
                ));
}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public boolean deletePost(int postId) {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean incrementLike(int postId) {
        String sql = "UPDATE posts SET likes = likes + 1 WHERE id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hasUserLiked(int postId, int userId) {
        String sql = "SELECT 1 FROM post_likes WHERE post_id = ? AND user_id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean toggleLike(int postId, int userId) {
        try (java.sql.Connection conn = Connection.getDataSource().getConnection()) {
            // Vérifier si l'utilisateur a déjà liké
            if (hasUserLiked(postId, userId)) {
                // Retirer le like
                String deleteSql = "DELETE FROM post_likes WHERE post_id = ? AND user_id = ?";
                String updateSql = "UPDATE posts SET likes = likes - 1 WHERE id = ? AND likes > 0";
                
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                     PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    deleteStmt.setInt(1, postId);
                    deleteStmt.setInt(2, userId);
                    deleteStmt.executeUpdate();
                    
                    updateStmt.setInt(1, postId);
                    updateStmt.executeUpdate();
                    return true;
                }
            } else {
                // Ajouter le like
                String insertSql = "INSERT INTO post_likes (post_id, user_id) VALUES (?, ?)";
                String updateSql = "UPDATE posts SET likes = likes + 1 WHERE id = ?";
                
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql);
                     PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    insertStmt.setInt(1, postId);
                    insertStmt.setInt(2, userId);
                    insertStmt.executeUpdate();
                    
                    updateStmt.setInt(1, postId);
                    updateStmt.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getLikesCount(int postId) {
        String sql = "SELECT likes FROM posts WHERE id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("likes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean createComment(int postId, int userId, String content) {
        String sql = "INSERT INTO post_comments (post_id, user_id, content) VALUES (?, ?, ?)";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setString(3, content);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<PostComment> getCommentsByPost(int postId) {
        String sql = "SELECT c.*, u.fullname FROM post_comments c " +
                     "JOIN users u ON c.user_id = u.id " +
                     "WHERE c.post_id = ? " +
                     "ORDER BY c.created_at ASC";
        List<PostComment> comments = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PostComment comment = new PostComment(
                    rs.getInt("id"),
                    rs.getInt("post_id"),
                    rs.getInt("user_id"),
                    rs.getString("fullname"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    @Override
    public int getCommentsCount(int postId) {
        String sql = "SELECT COUNT(*) FROM post_comments WHERE post_id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void close() {
        Connection.close();
    }
}