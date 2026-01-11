package dev.studylink.studylink.impl.db.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import dev.studylink.studylink.business.Post;
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
        // On fait une jointure pour récupérer le nom de l'auteur en même temps
        String sql = "SELECT p.*, u.fullname FROM posts p " +
                     "JOIN users u ON p.user_id = u.id " +
                     "ORDER BY p.created_at DESC";
        List<Post> posts = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                posts.add(new Post(
                    rs.getInt("id"),
                    rs.getString("fullname"),
                    rs.getString("content"),
                    rs.getInt("likes"),
                    0 // On pourra ajouter le compte des commentaires plus tard
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
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
    public void close() {
        Connection.close();
    }
}