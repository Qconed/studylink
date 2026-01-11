package dev.studylink.studylink.impl.db.mysql;

import dev.studylink.studylink.business.StudySession;
import dev.studylink.studylink.dao.StudySessionDAO;
import dev.studylink.studylink.db.Connection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation MySQL/JDBC du StudySessionDAO.
 * Les méthodes effectuent des opérations SQL directes (PreparedStatement).
 */
public class MySQLStudySessionDAO implements StudySessionDAO {
    private static MySQLStudySessionDAO instance;

    private MySQLStudySessionDAO() {}

    public static synchronized MySQLStudySessionDAO getInstance() {
        if (instance == null) {
            instance = new MySQLStudySessionDAO();
        }
        return instance;
    }

    @Override
    public Optional<StudySession> findById(int id) {
        String sql = "SELECT * FROM study_sessions WHERE id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                StudySession s = buildFromResultSet(rs);
                s.setCategoryIds(getSessionCategoryIds(id));
                return Optional.of(s);
            }
        } catch (SQLException e) {
            System.err.println("Error finding study session by id: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public boolean createStudySession(StudySession session) {
        String sql = "INSERT INTO study_sessions (title, description, organizer_id, is_tutored, price, start_datetime, end_datetime, location, min_participants, max_participants, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, session.getTitle());
            stmt.setString(2, session.getDescription());
            stmt.setInt(3, session.getOrganizerId());
            stmt.setBoolean(4, session.isTutored());
            stmt.setDouble(5, session.getPrice());
            stmt.setTimestamp(6, Timestamp.valueOf(session.getTimeSlot().getStartTime()));
            stmt.setTimestamp(7, Timestamp.valueOf(session.getTimeSlot().getEndTime()));
            stmt.setString(8, session.getTimeSlot().getLocation());
            stmt.setInt(9, session.getMinParticipants());
            stmt.setInt(10, session.getMaxParticipants());
            stmt.setString(11, session.getStatus().toString());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    session.setId(keys.getInt(1));
                }
                // insert categories if present
                for (Integer catId : session.getCategoryIds()) {
                    addCategoryToSession(session.getId(), catId);
                }
                // organizer as participant
                addParticipant(session.getId(), session.getOrganizerId(), "ORGANIZER");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating study session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateStudySession(StudySession session) {
        String sql = "UPDATE study_sessions SET title = ?, description = ?, is_tutored = ?, price = ?, start_datetime = ?, end_datetime = ?, location = ?, min_participants = ?, max_participants = ?, status = ? WHERE id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, session.getTitle());
            stmt.setString(2, session.getDescription());
            stmt.setBoolean(3, session.isTutored());
            stmt.setDouble(4, session.getPrice());
            stmt.setTimestamp(5, Timestamp.valueOf(session.getTimeSlot().getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(session.getTimeSlot().getEndTime()));
            stmt.setString(7, session.getTimeSlot().getLocation());
            stmt.setInt(8, session.getMinParticipants());
            stmt.setInt(9, session.getMaxParticipants());
            stmt.setString(10, session.getStatus().toString());
            stmt.setInt(11, session.getId());

            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating study session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteStudySession(int sessionId) {
        String sql = "DELETE FROM study_sessions WHERE id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting study session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<StudySession> findAll() {
        String sql = "SELECT * FROM study_sessions ORDER BY start_datetime";
        List<StudySession> res = new ArrayList<>();
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                StudySession s = buildFromResultSet(rs);
                s.setCategoryIds(getSessionCategoryIds(s.getId()));
                res.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error listing study sessions: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public List<StudySession> findByOrganizer(int organizerId) {
        String sql = "SELECT * FROM study_sessions WHERE organizer_id = ? ORDER BY start_datetime";
        List<StudySession> res = new ArrayList<>();
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, organizerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                StudySession s = buildFromResultSet(rs);
                s.setCategoryIds(getSessionCategoryIds(s.getId()));
                res.add(s);
            }
        } catch (SQLException e) {
            System.err.println("Error finding study sessions by organizer: " + e.getMessage());
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public boolean addParticipant(int sessionId, int userId, String role) {
        String sql = "INSERT IGNORE INTO session_participants (session_id, user_id, role) VALUES (?, ?, ?)";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, userId);
            stmt.setString(3, role);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding participant: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeParticipant(int sessionId, int userId) {
        String sql = "DELETE FROM session_participants WHERE session_id = ? AND user_id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, userId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing participant: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getParticipantsCount(int sessionId) {
        String sql = "SELECT COUNT(*) as cnt FROM session_participants WHERE session_id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            System.err.println("Error getting participants count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Integer> getParticipantsIds(int sessionId) {
        String sql = "SELECT user_id FROM session_participants WHERE session_id = ?";
        List<Integer> ids = new ArrayList<>();
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting participants ids: " + e.getMessage());
            e.printStackTrace();
        }
        return ids;
    }

    @Override
    public List<Integer> getSessionCategoryIds(int sessionId) {
        String sql = "SELECT category_id FROM session_categories WHERE session_id = ?";
        List<Integer> cats = new ArrayList<>();
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cats.add(rs.getInt("category_id"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting session categories: " + e.getMessage());
            e.printStackTrace();
        }
        return cats;
    }

    @Override
    public boolean addCategoryToSession(int sessionId, int categoryId) {
        String sql = "INSERT IGNORE INTO session_categories (session_id, category_id) VALUES (?, ?)";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, categoryId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding category to session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeCategoryFromSession(int sessionId, int categoryId) {
        String sql = "DELETE FROM session_categories WHERE session_id = ? AND category_id = ?";
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.setInt(2, categoryId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing category from session: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void close() {
        Connection.close();
    }

    // Helper to build StudySession from ResultSet
    private StudySession buildFromResultSet(ResultSet rs) throws SQLException {
        StudySession s = new StudySession();
        s.setId(rs.getInt("id"));
        s.setTitle(rs.getString("title"));
        s.setDescription(rs.getString("description"));
        s.setOrganizerId(rs.getInt("organizer_id"));
        s.setTutored(rs.getBoolean("is_tutored"));
        s.setPrice(rs.getDouble("price"));

        Timestamp start = rs.getTimestamp("start_datetime");
        Timestamp end = rs.getTimestamp("end_datetime");

        if (start != null && end != null) {
            s.setTimeSlot(new dev.studylink.studylink.business.TimeSlot(start.toLocalDateTime(), end.toLocalDateTime(), start.toLocalDateTime(), rs.getString("location")));
        }

        s.setMinParticipants(rs.getInt("min_participants"));
        s.setMaxParticipants(rs.getInt("max_participants"));
        s.setStatus(dev.studylink.studylink.business.StudySessionStatus.valueOf(rs.getString("status")));

        // created_at et updated_at sont gérés en base; on met à jour updatedAt en mémoire
        s.touchUpdatedAt();
        return s;
    }
}
