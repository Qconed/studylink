package dev.studylink.studylink.impl.db.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import dev.studylink.studylink.business.TutorProfile;
import dev.studylink.studylink.business.TutorReview;
import dev.studylink.studylink.business.TutorSession;
import dev.studylink.studylink.dao.TutorDAO;
import dev.studylink.studylink.db.Connection;

public class MySQLTutorDAO implements TutorDAO {
    private static MySQLTutorDAO instance;

    private MySQLTutorDAO() {}

    public static synchronized MySQLTutorDAO getInstance() {
        if (instance == null) {
            instance = new MySQLTutorDAO();
        }
        return instance;
    }

    // ===== TUTOR PROFILES =====

    @Override
    public boolean createTutorProfile(TutorProfile profile) {
        String sql = "INSERT INTO tutor_profiles (user_id, bio, hourly_rate, subjects, availability) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getBio());
            stmt.setDouble(3, profile.getHourlyRate());
            
            // Convertir List<String> en Array PostgreSQL
            Array subjectsArray = conn.createArrayOf("text", profile.getSubjects().toArray());
            stmt.setArray(4, subjectsArray);
            
            stmt.setString(5, profile.getAvailability());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<TutorProfile> getTutorProfileById(int tutorId) {
        String sql = "SELECT tp.*, u.fullname as tutor_name " +
                     "FROM tutor_profiles tp " +
                     "JOIN users u ON tp.user_id = u.id " +
                     "WHERE tp.id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(buildTutorProfileFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<TutorProfile> getTutorProfileByUserId(int userId) {
        String sql = "SELECT tp.*, u.fullname as tutor_name " +
                     "FROM tutor_profiles tp " +
                     "JOIN users u ON tp.user_id = u.id " +
                     "WHERE tp.user_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(buildTutorProfileFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public List<TutorProfile> getAllTutorProfiles() {
        String sql = "SELECT tp.*, u.fullname as tutor_name " +
                     "FROM tutor_profiles tp " +
                     "JOIN users u ON tp.user_id = u.id " +
                     "ORDER BY tp.average_rating DESC, tp.total_sessions DESC";
        
        List<TutorProfile> profiles = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                profiles.add(buildTutorProfileFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return profiles;
    }

    @Override
    public List<TutorProfile> searchTutorsBySubject(String subject) {
        String sql = "SELECT tp.*, u.fullname as tutor_name " +
                     "FROM tutor_profiles tp " +
                     "JOIN users u ON tp.user_id = u.id " +
                     "WHERE ? = ANY(tp.subjects) " +
                     "ORDER BY tp.average_rating DESC";
        
        List<TutorProfile> profiles = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, subject);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                profiles.add(buildTutorProfileFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return profiles;
    }

    @Override
    public List<TutorProfile> getPremiumTutors() {
        String sql = "SELECT tp.*, u.fullname as tutor_name " +
                     "FROM tutor_profiles tp " +
                     "JOIN users u ON tp.user_id = u.id " +
                     "WHERE tp.is_premium = TRUE " +
                     "ORDER BY tp.average_rating DESC";
        
        List<TutorProfile> profiles = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                profiles.add(buildTutorProfileFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return profiles;
    }

    @Override
    public boolean updateTutorProfile(TutorProfile profile) {
        String sql = "UPDATE tutor_profiles SET bio = ?, hourly_rate = ?, subjects = ?, " +
                     "availability = ?, is_premium = ? WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profile.getBio());
            stmt.setDouble(2, profile.getHourlyRate());
            
            Array subjectsArray = conn.createArrayOf("text", profile.getSubjects().toArray());
            stmt.setArray(3, subjectsArray);
            
            stmt.setString(4, profile.getAvailability());
            stmt.setBoolean(5, profile.isPremium());
            stmt.setInt(6, profile.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateTutorRating(int tutorId, double newRating) {
        String sql = "UPDATE tutor_profiles SET average_rating = ? WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDouble(1, newRating);
            stmt.setInt(2, tutorId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean incrementTutorSessions(int tutorId) {
        String sql = "UPDATE tutor_profiles SET total_sessions = total_sessions + 1 WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTutorProfile(int tutorId) {
        String sql = "DELETE FROM tutor_profiles WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== TUTOR SESSIONS =====

    @Override
    public boolean createTutorSession(TutorSession session) {
        String sql = "INSERT INTO tutor_sessions (tutor_id, title, subject, description, price, " +
                     "duration_minutes, max_students) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, session.getTutorId());
            stmt.setString(2, session.getTitle());
            stmt.setString(3, session.getSubject());
            stmt.setString(4, session.getDescription());
            stmt.setDouble(5, session.getPrice());
            stmt.setInt(6, session.getDurationMinutes());
            stmt.setInt(7, session.getMaxStudents());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<TutorSession> getTutorSessionById(int sessionId) {
        String sql = "SELECT * FROM tutor_sessions WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(buildTutorSessionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public List<TutorSession> getSessionsByTutorId(int tutorId) {
        String sql = "SELECT * FROM tutor_sessions WHERE tutor_id = ? ORDER BY created_at DESC";
        
        List<TutorSession> sessions = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sessions.add(buildTutorSessionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return sessions;
    }

    @Override
    public List<TutorSession> searchSessionsBySubject(String subject) {
        String sql = "SELECT * FROM tutor_sessions WHERE subject ILIKE ? ORDER BY created_at DESC";
        
        List<TutorSession> sessions = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + subject + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sessions.add(buildTutorSessionFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return sessions;
    }

    @Override
    public boolean updateTutorSession(TutorSession session) {
        String sql = "UPDATE tutor_sessions SET title = ?, subject = ?, description = ?, " +
                     "price = ?, duration_minutes = ?, max_students = ? WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, session.getTitle());
            stmt.setString(2, session.getSubject());
            stmt.setString(3, session.getDescription());
            stmt.setDouble(4, session.getPrice());
            stmt.setInt(5, session.getDurationMinutes());
            stmt.setInt(6, session.getMaxStudents());
            stmt.setInt(7, session.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteTutorSession(int sessionId) {
        String sql = "DELETE FROM tutor_sessions WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== TUTOR REVIEWS =====

    @Override
    public boolean createReview(TutorReview review) {
        String sql = "INSERT INTO tutor_reviews (tutor_id, student_id, booking_id, rating, comment) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, review.getTutorId());
            stmt.setInt(2, review.getStudentId());
            
            if (review.getBookingId() != null) {
                stmt.setInt(3, review.getBookingId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setInt(4, review.getRating());
            stmt.setString(5, review.getComment());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<TutorReview> getReviewsByTutorId(int tutorId) {
        String sql = "SELECT tr.*, u.fullname as student_name " +
                     "FROM tutor_reviews tr " +
                     "JOIN users u ON tr.student_id = u.id " +
                     "WHERE tr.tutor_id = ? " +
                     "ORDER BY tr.created_at DESC";
        
        List<TutorReview> reviews = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                reviews.add(buildTutorReviewFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return reviews;
    }

    @Override
    public double calculateAverageRating(int tutorId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM tutor_reviews WHERE tutor_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0.0;
    }

    @Override
    public int getReviewCount(int tutorId) {
        String sql = "SELECT COUNT(*) as count FROM tutor_reviews WHERE tutor_id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    @Override
    public void close() {
        // HikariCP gère la fermeture des connexions
    }

    // ===== HELPER METHODS =====

    private TutorProfile buildTutorProfileFromResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        Timestamp updatedAtTimestamp = rs.getTimestamp("updated_at");
        
        // Récupérer l'array PostgreSQL et le convertir en List
        Array subjectsArray = rs.getArray("subjects");
        List<String> subjects = new ArrayList<>();
        if (subjectsArray != null) {
            String[] subjectsStr = (String[]) subjectsArray.getArray();
            subjects = Arrays.asList(subjectsStr);
        }
        
        return new TutorProfile(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("tutor_name"),
            rs.getString("bio"),
            rs.getDouble("hourly_rate"),
            subjects,
            rs.getString("availability"),
            rs.getInt("total_sessions"),
            rs.getDouble("average_rating"),
            rs.getBoolean("is_premium"),
            createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null,
            updatedAtTimestamp != null ? updatedAtTimestamp.toLocalDateTime() : null
        );
    }

    private TutorSession buildTutorSessionFromResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        
        return new TutorSession(
            rs.getInt("id"),
            rs.getInt("tutor_id"),
            rs.getString("title"),
            rs.getString("subject"),
            rs.getString("description"),
            rs.getDouble("price"),
            rs.getInt("duration_minutes"),
            rs.getInt("max_students"),
            createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null
        );
    }

    private TutorReview buildTutorReviewFromResultSet(ResultSet rs) throws SQLException {
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        
        Integer bookingId = rs.getInt("booking_id");
        if (rs.wasNull()) {
            bookingId = null;
        }
        
        return new TutorReview(
            rs.getInt("id"),
            rs.getInt("tutor_id"),
            rs.getInt("student_id"),
            bookingId,
            rs.getString("student_name"),
            rs.getInt("rating"),
            rs.getString("comment"),
            createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null
        );
    }
}
