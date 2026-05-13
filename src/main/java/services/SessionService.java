package services;

import models.Session;
import utils.MyDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionService {

    private Connection cnx;

    public SessionService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // CREATE - Updated to include new fields
    public void addSession(Session s) throws SQLException {
        String sql = "INSERT INTO sessions(title, session_date, start_time, end_time, location, session_type, status, category, popularity, average_rating) VALUES(?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, s.getTitle());
        ps.setDate(2, Date.valueOf(s.getSessionDate()));
        ps.setTime(3, Time.valueOf(s.getStartTime()));
        ps.setTime(4, Time.valueOf(s.getEndTime()));
        ps.setString(5, s.getLocation());
        ps.setString(6, s.getSessionType());
        ps.setString(7, s.getStatus());
        ps.setString(8, s.getCategory());
        ps.setInt(9, s.getPopularity());
        ps.setDouble(10, s.getAverageRating());

        ps.executeUpdate();
        System.out.println("Session added!");
    }

    // GET ALL - Updated to include new fields
    public List<Session> getAllSessions() throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // UPDATE - Updated to include new fields
    public void updateSession(Session s) throws SQLException {
        String sql = "UPDATE sessions SET title=?, session_date=?, start_time=?, end_time=?, location=?, session_type=?, status=?, category=?, popularity=?, average_rating=? WHERE session_id=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, s.getTitle());
        ps.setDate(2, Date.valueOf(s.getSessionDate()));
        ps.setTime(3, Time.valueOf(s.getStartTime()));
        ps.setTime(4, Time.valueOf(s.getEndTime()));
        ps.setString(5, s.getLocation());
        ps.setString(6, s.getSessionType());
        ps.setString(7, s.getStatus());
        ps.setString(8, s.getCategory());
        ps.setInt(9, s.getPopularity());
        ps.setDouble(10, s.getAverageRating());
        ps.setInt(11, s.getSessionId());

        ps.executeUpdate();
        System.out.println("Session updated!");
    }

    // DELETE
    public void deleteSession(int id) throws SQLException {
        String sql = "DELETE FROM sessions WHERE session_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Session deleted!");
    }

    // Get session by ID - Updated to include new fields
    public Session getSessionById(int id) throws SQLException {
        String sql = "SELECT * FROM sessions WHERE session_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return extractSessionFromResultSet(rs);
        }
        return null;
    }

    // Update session status only
    public boolean updateSessionStatus(int sessionId, String status) throws SQLException {
        String query = "UPDATE sessions SET status = ? WHERE session_id = ?";
        try (PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, sessionId);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating session status: " + e.getMessage());
            throw e;
        }
    }

    // ========== FIXED: Get all available sessions (includes BOTH 'active' AND 'scheduled') ==========
    public List<Session> getAvailableSessions() throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE status IN ('active', 'scheduled') AND reserved_by IS NULL AND " +
                "(session_date > CURDATE() OR (session_date = CURDATE() AND start_time > CURTIME())) " +
                "ORDER BY session_date, start_time";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // Get sessions reserved by a specific patient
    public List<Session> getPatientSessions(int patientId) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE reserved_by = ? ORDER BY session_date DESC, start_time DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // Get patient's upcoming sessions
    public List<Session> getPatientUpcomingSessions(int patientId) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE reserved_by = ? AND " +
                "(session_date > CURDATE() OR " +
                "(session_date = CURDATE() AND start_time > CURTIME())) " +
                "ORDER BY session_date, start_time";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // Get patient's past sessions
    public List<Session> getPatientPastSessions(int patientId) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE reserved_by = ? AND " +
                "(session_date < CURDATE() OR " +
                "(session_date = CURDATE() AND start_time < CURTIME())) " +
                "ORDER BY session_date DESC, start_time DESC";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // Reserve a session
    public void reserveSession(int sessionId, int patientId) throws SQLException {
        // First check if session exists and is available
        String checkSql = "SELECT reserved_by FROM sessions WHERE session_id = ?";
        PreparedStatement checkPs = cnx.prepareStatement(checkSql);
        checkPs.setInt(1, sessionId);
        ResultSet rs = checkPs.executeQuery();

        if (!rs.next()) {
            throw new SQLException("Session not found");
        }

        Object reservedBy = rs.getObject("reserved_by");
        if (reservedBy != null) {
            throw new SQLException("This session is already reserved");
        }

        // Reserve the session AND increment popularity
        String sql = "UPDATE sessions SET reserved_by = ?, reserved_at = NOW(), popularity = popularity + 1 WHERE session_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
        ps.setInt(2, sessionId);

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Session reserved successfully by patient " + patientId);
        } else {
            throw new SQLException("Failed to reserve session");
        }
    }

    // Cancel a reservation
    public void cancelReservation(int sessionId, int patientId) throws SQLException {
        String sql = "UPDATE sessions SET reserved_by = NULL, reserved_at = NULL WHERE session_id = ? AND reserved_by = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.setInt(2, patientId);

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Reservation cancelled successfully");
        } else {
            throw new SQLException("Failed to cancel reservation or you don't have permission");
        }
    }

    // Check if session is reserved by a specific patient
    public boolean isReservedByPatient(int sessionId, int patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessions WHERE session_id = ? AND reserved_by = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.setInt(2, patientId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // FIXED: Search available sessions by keyword (includes 'scheduled')
    public List<Session> searchAvailableSessions(String keyword) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE status IN ('active', 'scheduled') AND reserved_by IS NULL AND " +
                "(session_date > CURDATE() OR (session_date = CURDATE() AND start_time > CURTIME())) AND " +
                "(title LIKE ? OR location LIKE ?) ORDER BY session_date, start_time";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "%" + keyword + "%");
        ps.setString(2, "%" + keyword + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // FIXED: Filter available sessions by type (includes 'scheduled')
    public List<Session> filterAvailableSessionsByType(String type) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE status IN ('active', 'scheduled') AND reserved_by IS NULL AND " +
                "(session_date > CURDATE() OR (session_date = CURDATE() AND start_time > CURTIME()))";

        if (type != null && !"All Types".equals(type) && !type.isEmpty()) {
            sql += " AND session_type = ?";
        }

        sql += " ORDER BY session_date, start_time";

        PreparedStatement ps = cnx.prepareStatement(sql);
        if (type != null && !"All Types".equals(type) && !type.isEmpty()) {
            ps.setString(1, type);
        }
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // Get reservation count for a session
    public int getReservationCount(int sessionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sessions WHERE session_id = ? AND reserved_by IS NOT NULL";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    // Update average rating for a session
    public void updateAverageRating(int sessionId, double newAverage) throws SQLException {
        String sql = "UPDATE sessions SET average_rating = ? WHERE session_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDouble(1, newAverage);
        ps.setInt(2, sessionId);
        ps.executeUpdate();
    }

    // Get most popular sessions
    public List<Session> getMostPopularSessions(int limit) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE status IN ('active', 'scheduled') ORDER BY popularity DESC LIMIT ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractSessionFromResultSet(rs));
        }
        return list;
    }

    // Get sessions by psychologist
    public List<Session> getSessionsByPsychologist(int psychologistId) throws SQLException {
        List<Session> list = new ArrayList<>();

        String sql = "SELECT * FROM sessions WHERE psychologist_id = ? ORDER BY session_date DESC";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, psychologistId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(extractSessionFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sessions for psychologist: " + e.getMessage());
            throw e;
        }

        return list;
    }

    // Helper method to extract Session from ResultSet
    private Session extractSessionFromResultSet(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setSessionId(rs.getInt("session_id"));
        s.setTitle(rs.getString("title"));
        s.setSessionDate(rs.getDate("session_date").toLocalDate());
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setLocation(rs.getString("location"));
        s.setSessionType(rs.getString("session_type"));
        s.setStatus(rs.getString("status"));

        // Reserved fields
        Object reservedByObj = rs.getObject("reserved_by");
        if (reservedByObj != null) {
            s.setReservedBy(rs.getInt("reserved_by"));
        } else {
            s.setReservedBy(null);
        }

        Timestamp reservedAtTs = rs.getTimestamp("reserved_at");
        if (reservedAtTs != null) {
            s.setReservedAt(reservedAtTs.toLocalDateTime());
        } else {
            s.setReservedAt(null);
        }

        // Recommendation fields
        s.setCategory(rs.getString("category"));
        s.setPopularity(rs.getInt("popularity"));
        s.setAverageRating(rs.getDouble("average_rating"));

        return s;
    }
}