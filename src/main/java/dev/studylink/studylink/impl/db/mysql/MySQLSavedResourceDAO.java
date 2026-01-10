package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.CategoryType;
import dev.studylink.studylink.business.Resource;
import dev.studylink.studylink.dao.SavedResourceDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MySQLSavedResourceDAO implements SavedResourceDAO {
    private static MySQLSavedResourceDAO instance;

    private MySQLSavedResourceDAO() {}

    public static synchronized MySQLSavedResourceDAO getInstance() {
        if (instance == null) {
            instance = new MySQLSavedResourceDAO();
        }
        return instance;
    }

    @Override
    public boolean saveResource(int userId, int resourceId) {
        String sql = "INSERT INTO saved_resources (user_id, resource_id) VALUES (?, ?) " +
                "ON CONFLICT (user_id, resource_id) DO NOTHING";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error saving resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean unsaveResource(int userId, int resourceId) {
        String sql = "DELETE FROM saved_resources WHERE user_id = ? AND resource_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error unsaving resource: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Resource> getSavedResources(int userId) {
        String sql = "SELECT r.* FROM resources r " +
                "JOIN saved_resources sr ON r.id = sr.resource_id " +
                "WHERE sr.user_id = ? " +
                "ORDER BY sr.saved_at DESC";
        List<Resource> resources = new ArrayList<>();

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            MySQLResourceDAO resourceDAO = MySQLResourceDAO.getInstance();

            while (rs.next()) {
                Resource resource = buildResourceFromResultSet(rs);
                resource.setCategories(resourceDAO.getResourceCategories(resource.getId()));
                resources.add(resource);
            }

        } catch (SQLException e) {
            System.err.println("Error getting saved resources: " + e.getMessage());
            e.printStackTrace();
        }

        return resources;
    }

    @Override
    public boolean isResourceSaved(int userId, int resourceId) {
        String sql = "SELECT COUNT(*) FROM saved_resources WHERE user_id = ? AND resource_id = ?";

        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, resourceId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error checking if resource is saved: " + e.getMessage());
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
