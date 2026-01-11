package dev.studylink.studylink.impl.db.mysql;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.studylink.studylink.business.SessionBooking;
import dev.studylink.studylink.dao.SessionBookingDAO;
import dev.studylink.studylink.db.Connection;

public class MySQLSessionBookingDAO implements SessionBookingDAO {
    private static MySQLSessionBookingDAO instance;

    private MySQLSessionBookingDAO() {}

    public static synchronized MySQLSessionBookingDAO getInstance() {
        if (instance == null) {
            instance = new MySQLSessionBookingDAO();
        }
        return instance;
    }

    @Override
    public boolean createBooking(SessionBooking booking) {
        String sql = "INSERT INTO session_bookings (session_id, student_id, booking_date, status, payment_status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, booking.getSessionId());
            stmt.setInt(2, booking.getStudentId());
            stmt.setTimestamp(3, Timestamp.valueOf(booking.getBookingDate()));
            stmt.setString(4, booking.getStatus());
            stmt.setString(5, booking.getPaymentStatus());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Optional<SessionBooking> getBookingById(int bookingId) {
        String sql = "SELECT sb.*, u.fullname as student_name, ts.title as session_title " +
                     "FROM session_bookings sb " +
                     "JOIN users u ON sb.student_id = u.id " +
                     "JOIN tutor_sessions ts ON sb.session_id = ts.id " +
                     "WHERE sb.id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bookingId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(buildSessionBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public List<SessionBooking> getBookingsByStudentId(int studentId) {
        String sql = "SELECT sb.*, u.fullname as student_name, ts.title as session_title " +
                     "FROM session_bookings sb " +
                     "JOIN users u ON sb.student_id = u.id " +
                     "JOIN tutor_sessions ts ON sb.session_id = ts.id " +
                     "WHERE sb.student_id = ? " +
                     "ORDER BY sb.booking_date DESC";
        
        List<SessionBooking> bookings = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(buildSessionBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return bookings;
    }

    @Override
    public List<SessionBooking> getBookingsBySessionId(int sessionId) {
        String sql = "SELECT sb.*, u.fullname as student_name, ts.title as session_title " +
                     "FROM session_bookings sb " +
                     "JOIN users u ON sb.student_id = u.id " +
                     "JOIN tutor_sessions ts ON sb.session_id = ts.id " +
                     "WHERE sb.session_id = ? " +
                     "ORDER BY sb.booking_date DESC";
        
        List<SessionBooking> bookings = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(buildSessionBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return bookings;
    }

    @Override
    public List<SessionBooking> getBookingsByTutorId(int tutorId) {
        String sql = "SELECT sb.*, u.fullname as student_name, ts.title as session_title " +
                     "FROM session_bookings sb " +
                     "JOIN users u ON sb.student_id = u.id " +
                     "JOIN tutor_sessions ts ON sb.session_id = ts.id " +
                     "JOIN tutor_profiles tp ON ts.tutor_id = tp.id " +
                     "WHERE tp.id = ? " +
                     "ORDER BY sb.booking_date DESC";
        
        List<SessionBooking> bookings = new ArrayList<>();
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, tutorId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                bookings.add(buildSessionBookingFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return bookings;
    }

    @Override
    public boolean updateBookingStatus(int bookingId, String status) {
        String sql = "UPDATE session_bookings SET status = ? WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, bookingId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updatePaymentStatus(int bookingId, String paymentStatus) {
        String sql = "UPDATE session_bookings SET payment_status = ? WHERE id = ?";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paymentStatus);
            stmt.setInt(2, bookingId);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean cancelBooking(int bookingId) {
        return updateBookingStatus(bookingId, "CANCELLED");
    }

    @Override
    public boolean isSlotAvailable(int sessionId, LocalDateTime bookingDate) {
        // Récupérer max_students pour cette session
        String maxStudentsSql = "SELECT max_students FROM tutor_sessions WHERE id = ?";
        int maxStudents = 0;
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(maxStudentsSql)) {
            
            stmt.setInt(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                maxStudents = rs.getInt("max_students");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Compter les réservations actives
        int activeBookings = getActiveBookingsCount(sessionId, bookingDate);
        
        return activeBookings < maxStudents;
    }

    @Override
    public int getActiveBookingsCount(int sessionId, LocalDateTime bookingDate) {
        String sql = "SELECT COUNT(*) as count FROM session_bookings " +
                     "WHERE session_id = ? AND booking_date = ? " +
                     "AND status IN ('PENDING', 'CONFIRMED')";
        
        try (java.sql.Connection conn = Connection.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionId);
            stmt.setTimestamp(2, Timestamp.valueOf(bookingDate));
            
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

    private SessionBooking buildSessionBookingFromResultSet(ResultSet rs) throws SQLException {
        Timestamp bookingDateTimestamp = rs.getTimestamp("booking_date");
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        
        return new SessionBooking(
            rs.getInt("id"),
            rs.getInt("session_id"),
            rs.getInt("student_id"),
            rs.getString("student_name"),
            rs.getString("session_title"),
            bookingDateTimestamp != null ? bookingDateTimestamp.toLocalDateTime() : null,
            rs.getString("status"),
            rs.getString("payment_status"),
            createdAtTimestamp != null ? createdAtTimestamp.toLocalDateTime() : null
        );
    }
}
