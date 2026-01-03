package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.dao.CategoryDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MySQLCategoryDAO implements CategoryDAO {
    private static MySQLCategoryDAO instance;

    private MySQLCategoryDAO() {}

    public static synchronized MySQLCategoryDAO getInstance() {
        if (instance == null) {
            instance = new MySQLCategoryDAO();
        }
        return instance;
    }

    @Override
    public Optional<Category> findById(int id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Category category = buildCategoryFromResultSet(rs);
                return Optional.of(category);
            }
        } catch (SQLException e) {
            System.err.println("Error finding category by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories ORDER BY type, title";
        List<Category> categories = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(buildCategoryFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    @Override
    public List<Category> getCategoriesByType(CategoryType type) {
        String sql = "SELECT * FROM categories WHERE type = ? ORDER BY title";
        List<Category> categories = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.toString());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(buildCategoryFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error getting categories by type: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    @Override
    public boolean createCategory(String title, String description, CategoryType type, int level) {
        String sql = "INSERT INTO categories (title, description, type, level) VALUES (?, ?, ?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, type.toString());
            stmt.setInt(4, level);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET title = ?, description = ?, type = ?, level = ? WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getTitle());
            stmt.setString(2, category.getDescription());
            stmt.setString(3, category.getType().toString());
            stmt.setInt(4, category.getLevel());
            stmt.setInt(5, category.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    private Category buildCategoryFromResultSet(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                CategoryType.fromString(rs.getString("type")),
                rs.getInt("level")
        );
    }
}