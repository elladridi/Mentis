package services;

import models.SessionReview;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionReviewService {

    private Connection cnx;

    public SessionReviewService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // CREATE - Add a review
    public void addReview(SessionReview review) throws SQLException {
        // Check if patient already reviewed this session
        if (hasPatientReviewed(review.getSessionId(), review.getPatientId())) {
            throw new SQLException("You have already reviewed this session");
        }

        String sql = "INSERT INTO session_review (session_id, patient_id, rating, comment) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, review.getSessionId());
        ps.setInt(2, review.getPatientId());
        ps.setInt(3, review.getRating());
        ps.setString(4, review.getComment());

        ps.executeUpdate();
        System.out.println("Review added successfully!");
    }

    // READ - Get all reviews for a session
    public List<SessionReview> getReviewsBySession(int sessionId) throws SQLException {
        List<SessionReview> list = new ArrayList<>();

        String sql = "SELECT r.*, s.title, s.session_date, s.start_time, s.end_time, s.location, s.session_type, u.first_name, u.last_name " +
                "FROM session_review r " +
                "JOIN sessions s ON r.session_id = s.session_id " +
                "JOIN user u ON r.patient_id = u.id " +
                "WHERE r.session_id = ? " +
                "ORDER BY r.review_date DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractReviewFromResultSet(rs));
        }
        return list;
    }

    // READ - Get all reviews by a patient
    public List<SessionReview> getReviewsByPatient(int patientId) throws SQLException {
        List<SessionReview> list = new ArrayList<>();

        String sql = "SELECT r.*, s.title, s.session_date, s.start_time, s.end_time, s.location, s.session_type " +
                "FROM session_review r " +
                "JOIN sessions s ON r.session_id = s.session_id " +
                "WHERE r.patient_id = ? " +
                "ORDER BY r.review_date DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(extractReviewFromResultSet(rs));
        }
        return list;
    }

    // READ - Get review by ID
    public SessionReview getReviewById(int reviewId) throws SQLException {
        String sql = "SELECT r.*, s.title, s.session_date, s.start_time, s.end_time, s.location, s.session_type, u.first_name, u.last_name " +
                "FROM session_review r " +
                "JOIN sessions s ON r.session_id = s.session_id " +
                "JOIN user u ON r.patient_id = u.id " +
                "WHERE r.review_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, reviewId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return extractReviewFromResultSet(rs);
        }
        return null;
    }

    // UPDATE - Update a review
    public void updateReview(SessionReview review) throws SQLException {
        String sql = "UPDATE session_review SET rating = ?, comment = ? WHERE review_id = ? AND patient_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, review.getRating());
        ps.setString(2, review.getComment());
        ps.setInt(3, review.getReviewId());
        ps.setInt(4, review.getPatientId());

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Review updated successfully!");
        } else {
            throw new SQLException("Failed to update review or you don't have permission");
        }
    }

    // DELETE - Delete a review
    public void deleteReview(int reviewId, int patientId) throws SQLException {
        String sql = "DELETE FROM session_review WHERE review_id = ? AND patient_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, reviewId);
        ps.setInt(2, patientId);

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Review deleted successfully!");
        } else {
            throw new SQLException("Failed to delete review or you don't have permission");
        }
    }

    // CHECK - If patient already reviewed this session
    public boolean hasPatientReviewed(int sessionId, int patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM session_review WHERE session_id = ? AND patient_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.setInt(2, patientId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // GET average rating for a session
    public double getAverageRating(int sessionId) throws SQLException {
        String sql = "SELECT AVG(rating) FROM session_review WHERE session_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0.0;
    }

    // GET review count for a session
    public int getReviewCount(int sessionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM session_review WHERE session_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    // Helper method to extract Review from ResultSet
    private SessionReview extractReviewFromResultSet(ResultSet rs) throws SQLException {
        SessionReview r = new SessionReview();
        r.setReviewId(rs.getInt("review_id"));
        r.setSessionId(rs.getInt("session_id"));
        r.setPatientId(rs.getInt("patient_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));

        Timestamp ts = rs.getTimestamp("review_date");
        if (ts != null) {
            r.setReviewDate(ts.toLocalDateTime());
        }

        // Set session details
        r.setSessionTitle(rs.getString("title"));

        Date sessionDate = rs.getDate("session_date");
        if (sessionDate != null) {
            r.setSessionDate(sessionDate.toLocalDate());
        }

        Time startTime = rs.getTime("start_time");
        if (startTime != null) {
            r.setStartTime(startTime.toLocalTime());
        }

        Time endTime = rs.getTime("end_time");
        if (endTime != null) {
            r.setEndTime(endTime.toLocalTime());
        }

        r.setLocation(rs.getString("location"));
        r.setSessionType(rs.getString("session_type"));

        // Try to get patient name if available
        try {
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            if (firstName != null && lastName != null) {
                r.setPatientName(firstName + " " + lastName);
            }
        } catch (SQLException e) {
            // Column doesn't exist, ignore
        }

        return r;
    }
}