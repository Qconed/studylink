package dev.studylink.studylink.impl.db.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.studylink.studylink.business.Category;
import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.dao.ResourceDAO;
import dev.studylink.studylink.db.Connection;

public class MySQLResourceDAO implements ResourceDAO {
    private static MySQLResourceDAO instance;

    private MySQLResourceDAO() {}

    public static synchronized MySQLResourceDAO getInstance() {
        if (instance == null) {
            instance = new MySQLResourceDAO();
        }
        return instance;
    }

    @Override
    public boolean createResource(Resource resource) {
        String sql = "INSERT INTO resources (title, content, attachment_path, price, owner_id) VALUES (?, ?, ?, ?, ?)";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, resource.getTitle());
            stmt.setString(2, resource.getContent());
            stmt.setString(3, resource.getAttachmentPath());
            stmt.setDouble(4, resource.getPrice());
            stmt.setInt(5, resource.getOwnerId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    resource.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error creating resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Optional<Resource> findById(int id) {
        String sql = "SELECT * FROM resources WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Resource resource = buildResourceFromResultSet(rs);
                // Load categories
                resource.setCategories(getResourceCategories(id));
                return Optional.of(resource);
            }

        } catch (SQLException e) {
            System.err.println("Error finding resource by ID: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public boolean updateResource(Resource resource) {
        String sql = "UPDATE resources SET title = ?, content = ?, attachment_path = ?, price = ? WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resource.getTitle());
            stmt.setString(2, resource.getContent());
            stmt.setString(3, resource.getAttachmentPath());
            stmt.setDouble(4, resource.getPrice());
            stmt.setInt(5, resource.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean deleteResource(int resourceId) {
        String sql = "DELETE FROM resources WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Resource> getAllResources() {
        String sql = "SELECT * FROM resources ORDER BY created_at DESC";
        List<Resource> resources = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Resource resource = buildResourceFromResultSet(rs);
                resource.setCategories(getResourceCategories(resource.getId()));
                resources.add(resource);
            }

        } catch (SQLException e) {
            System.err.println("Error getting all resources: " + e.getMessage());
            e.printStackTrace();
        }

        return resources;
    }

    @Override
    public List<Resource> getResourcesByOwner(int userId) {
        String sql = "SELECT * FROM resources WHERE owner_id = ? ORDER BY created_at DESC";
        List<Resource> resources = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = buildResourceFromResultSet(rs);
                resource.setCategories(getResourceCategories(resource.getId()));
                resources.add(resource);
            }

        } catch (SQLException e) {
            System.err.println("Error getting resources by owner: " + e.getMessage());
            e.printStackTrace();
        }

        return resources;
    }

    @Override
    public List<Resource> getResourcesByCategory(int categoryId) {
        String sql = "SELECT r.* FROM resources r " +
                "JOIN resource_categories rc ON r.id = rc.resource_id " +
                "WHERE rc.category_id = ? ORDER BY r.created_at DESC";
        List<Resource> resources = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = buildResourceFromResultSet(rs);
                resource.setCategories(getResourceCategories(resource.getId()));
                resources.add(resource);
            }

        } catch (SQLException e) {
            System.err.println("Error getting resources by category: " + e.getMessage());
            e.printStackTrace();
        }

        return resources;
    }

    @Override
    public List<Resource> searchResources(String query) {
        String sql = "SELECT * FROM resources WHERE title LIKE ? OR content LIKE ? ORDER BY created_at DESC";
        List<Resource> resources = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Resource resource = buildResourceFromResultSet(rs);
                resource.setCategories(getResourceCategories(resource.getId()));
                resources.add(resource);
            }

        } catch (SQLException e) {
            System.err.println("Error searching resources: " + e.getMessage());
            e.printStackTrace();
        }

        return resources;
    }

    @Override
    public boolean addCategoryToResource(int resourceId, int categoryId) {
        String sql = "INSERT INTO resource_categories (resource_id, category_id) VALUES (?, ?) " +
                "ON CONFLICT (resource_id, category_id) DO NOTHING";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            stmt.setInt(2, categoryId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding category to resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeCategoryFromResource(int resourceId, int categoryId) {
        String sql = "DELETE FROM resource_categories WHERE resource_id = ? AND category_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            stmt.setInt(2, categoryId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error removing category from resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Category> getResourceCategories(int resourceId) {
        String sql = "SELECT c.* FROM categories c " +
                "JOIN resource_categories rc ON c.id = rc.category_id " +
                "WHERE rc.resource_id = ?";
        List<Category> categories = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        CategoryType.fromString(rs.getString("type")),
                        rs.getInt("level")
                );
                categories.add(category);
            }

        } catch (SQLException e) {
            System.err.println("Error getting resource categories: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    @Override
    public boolean incrementViewCount(int resourceId) {
        String sql = "UPDATE resources SET view_count = view_count + 1 WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error incrementing view count: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean incrementSaveCount(int resourceId) {
        String sql = "UPDATE resources SET save_count = save_count + 1 WHERE id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error incrementing save count: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean decrementSaveCount(int resourceId) {
        String sql = "UPDATE resources SET save_count = save_count - 1 WHERE id = ? AND save_count > 0";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resourceId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error decrementing save count: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean hasUserViewedResource(int userId, int resourceId) {
        String sql = "SELECT 1 FROM resource_views WHERE user_id = ? AND resource_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);
            
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            System.err.println("Error checking if user viewed resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean recordResourceView(int userId, int resourceId) {
        String sql = "INSERT INTO resource_views (user_id, resource_id) VALUES (?, ?) ON CONFLICT (user_id, resource_id) DO UPDATE SET viewed_at = CURRENT_TIMESTAMP";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error recording resource view: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    private Resource buildResourceFromResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");

        return new Resource(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getInt("owner_id"),
                rs.getString("attachment_path"),
                rs.getDouble("price"),
                createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : LocalDateTime.now(),
                updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : LocalDateTime.now(),
                rs.getInt("view_count"),
                rs.getInt("save_count")
        );
    }
}
