package dev.studylink.studylink.impl.db.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import dev.studylink.studylink.business.CartItem;
import dev.studylink.studylink.dao.CartDAO;
import dev.studylink.studylink.db.Connection;

public class MySQLCartDAO implements CartDAO {
    private static MySQLCartDAO instance;

    private MySQLCartDAO() {}

    public static synchronized MySQLCartDAO getInstance() {
        if (instance == null) {
            instance = new MySQLCartDAO();
        }
        return instance;
    }

    @Override
    public boolean addToCart(int userId, int resourceId, int quantity) {
        String sql = "INSERT INTO cart_items (user_id, resource_id, quantity) VALUES (?, ?, ?) " +
                     "ON CONFLICT (user_id, resource_id) DO UPDATE SET quantity = cart_items.quantity + ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);
            stmt.setInt(3, quantity);
            stmt.setInt(4, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<CartItem> getCartItems(int userId) {
        String sql = "SELECT c.id, c.user_id, c.resource_id, r.title, r.content, r.price, c.quantity, c.added_at " +
                     "FROM cart_items c " +
                     "JOIN resources r ON c.resource_id = r.id " +
                     "WHERE c.user_id = ? " +
                     "ORDER BY c.added_at DESC";
        
        List<CartItem> items = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("added_at");
                items.add(new CartItem(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("resource_id"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getDouble("price"),
                    rs.getInt("quantity"),
                    timestamp != null ? timestamp.toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return items;
    }

    @Override
    public boolean updateQuantity(int cartItemId, int quantity) {
        if (quantity <= 0) {
            return removeFromCart(cartItemId);
        }
        
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeFromCart(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cartItemId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean clearCart(int userId) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getCartItemCount(int userId) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) as total FROM cart_items WHERE user_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    @Override
    public double getCartTotal(int userId) {
        String sql = "SELECT COALESCE(SUM(c.quantity * r.price), 0) as total " +
                     "FROM cart_items c " +
                     "JOIN resources r ON c.resource_id = r.id " +
                     "WHERE c.user_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }

    @Override
    public void close() {
        // Connection pool gère la fermeture
    }
}
