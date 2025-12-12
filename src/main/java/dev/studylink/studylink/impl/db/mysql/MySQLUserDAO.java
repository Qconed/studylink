package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MySQLUserDAO implements UserDAO {

    @Override
    public Optional<User> findByCredentials(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
                return Optional.of(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email")
                );
                return Optional.of(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    // TODO: Rework the createuser, should only take the parameters of an user wihtout the id.
    //  DB should be able to compute the last used id, use that to create a new ID for the new User
    @Override
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";

//        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//
//            stmt.setString(1, user.getUsername());
//            stmt.setString(2, hashPassword(user.getPasswordHash()));
//            stmt.setString(3, user.getEmail());
//
//            int affectedRows = stmt.executeUpdate();
//
//            if (affectedRows > 0) {
//                ResultSet generatedKeys = stmt.getGeneratedKeys();
//                if (generatedKeys.next()) {
//                    user.setId(generatedKeys.getInt(1));
//                }
//                return true;
//            }
//
//        } catch (SQLException e) {
//            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
//            e.printStackTrace();
//        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    /**
     * Hash le mot de passe avec MD5 (à remplacer par BCrypt en production!)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur de hashage", e);
        }
    }
}
