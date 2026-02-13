package services;

import models.Session;
import models.SessionReview;
import utils.MyDB;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {

    private Connection cnx;
    private SessionService sessionService;
    private SessionReviewService reviewService;

    public RecommendationService() {
        try {
            this.cnx = MyDB.getInstance().getConnection();
            this.sessionService = new SessionService();
            this.reviewService = new SessionReviewService();
        } catch (Exception e) {
            System.err.println("Error initializing RecommendationService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Get recommendations based on patient's past sessions
    public List<Session> getRecommendationsForPatient(int patientId) throws SQLException {
        // Get patient's past sessions
        List<Session> pastSessions = sessionService.getPatientPastSessions(patientId);

        if (pastSessions.isEmpty()) {
            // If no history, recommend popular sessions
            return getPopularSessions();
        }

        // Analyze patient preferences
        Map<String, Integer> typePreferences = new HashMap<>();
        Map<String, Integer> timePreferences = new HashMap<>();

        for (Session session : pastSessions) {
            // Count session types
            String type = session.getSessionType();
            typePreferences.put(type, typePreferences.getOrDefault(type, 0) + 1);

            // Count time slots (morning, afternoon, evening)
            int hour = session.getStartTime().getHour();
            String timeSlot;
            if (hour < 12) timeSlot = "morning";
            else if (hour < 17) timeSlot = "afternoon";
            else timeSlot = "evening";
            timePreferences.put(timeSlot, timePreferences.getOrDefault(timeSlot, 0) + 1);
        }

        // Get most preferred type and time
        String preferredType = typePreferences.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        String preferredTime = timePreferences.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Get recommendations based on preferences
        return getRecommendedSessions(preferredType, preferredTime, patientId);
    }

    // Get popular sessions (fallback when no history)
    private List<Session> getPopularSessions() throws SQLException {
        List<Session> allSessions = sessionService.getAvailableSessions();

        // Sort by popularity (number of times booked)
        return allSessions.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getPopularity(), s1.getPopularity()))
                .limit(5)
                .collect(Collectors.toList());
    }

    // Get sessions based on preferences
    private List<Session> getRecommendedSessions(String preferredType, String preferredTime, int patientId) throws SQLException {
        List<Session> availableSessions = sessionService.getAvailableSessions();
        List<Session> recommendations = new ArrayList<>();

        // Score each session
        Map<Session, Integer> sessionScores = new HashMap<>();

        for (Session session : availableSessions) {
            int score = 0;

            // Match by type (highest weight)
            if (preferredType != null && session.getSessionType().equals(preferredType)) {
                score += 10;
            }

            // Match by time
            if (preferredTime != null) {
                int hour = session.getStartTime().getHour();
                String timeSlot;
                if (hour < 12) timeSlot = "morning";
                else if (hour < 17) timeSlot = "afternoon";
                else timeSlot = "evening";

                if (timeSlot.equals(preferredTime)) {
                    score += 5;
                }
            }

            // Popularity bonus
            score += session.getPopularity();

            // Rating bonus
            double avgRating = reviewService.getAverageRating(session.getSessionId());
            score += (int)(avgRating * 2);

            sessionScores.put(session, score);
        }

        // Sort by score and return top 5
        return sessionScores.entrySet().stream()
                .sorted(Map.Entry.<Session, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Get similar sessions to a given session
    public List<Session> getSimilarSessions(int sessionId) throws SQLException {
        Session targetSession = sessionService.getSessionById(sessionId);
        if (targetSession == null) return new ArrayList<>();

        List<Session> allSessions = sessionService.getAllSessions();

        return allSessions.stream()
                .filter(s -> s.getSessionId() != sessionId)
                .filter(s -> s.getSessionType().equals(targetSession.getSessionType()))
                .limit(3)
                .collect(Collectors.toList());
    }

    // Update popularity count when session is booked
    public void updatePopularity(int sessionId) throws SQLException {
        String sql = "UPDATE sessions SET popularity = popularity + 1 WHERE session_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.executeUpdate();
    }
}