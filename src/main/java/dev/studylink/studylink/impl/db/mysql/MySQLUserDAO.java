package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.User;
import dev.studylink.studylink.dao.UserDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;


public class MySQLUserDAO implements UserDAO {

    private static MySQLUserDAO instance;

    private MySQLUserDAO() {}

    public static synchronized MySQLUserDAO getInstance() {
        if (instance == null) {
            instance = new MySQLUserDAO();
        }
        return instance;
    }
    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("fullname"),
                    rs.getString("email"),
                    rs.getString("password")
                );
                return Optional.of(user);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par fullname: " + e.getMessage());
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public boolean createUser(String fullname, String email, String passwordHash) {
        String sql = "INSERT INTO users (fullname, email, password) VALUES (?, ?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {


            stmt.setString(1, fullname);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'utilisateur: " + e.getMessage());
            e.printStackTrace();

        }

        return false;
    }

    @Override
    public User[] getAllUsers() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("fullname"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }

        return users.toArray(new User[0]);
    }

    @Override
    public void close() {
        Connection.close();
    }
}

