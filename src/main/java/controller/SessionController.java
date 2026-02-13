package controller;

import models.Session;
import services.SessionService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SessionController {
    private SessionService sessionService;

    public SessionController() {
        this.sessionService = new SessionService();
    }

    // Create session with object
    public void createSession(Session session) throws SQLException {
        sessionService.addSession(session);
    }

    // Create session with parameters
    public void createSession(String title, LocalDate sessionDate, LocalTime startTime,
                              LocalTime endTime, String location, String sessionType,
                              String status) throws SQLException {
        Session session = new Session(title, sessionDate, startTime, endTime, location, sessionType, status);
        sessionService.addSession(session);
    }

    // Get all sessions
    public List<Session> getAllSessions() throws SQLException {
        return sessionService.getAllSessions();
    }

    // Get session by ID
    public Session getSessionById(int sessionId) throws SQLException {
        return sessionService.getSessionById(sessionId);
    }

    // Update session with parameters
    public void updateSession(int id, String title, LocalDate sessionDate, LocalTime startTime,
                              LocalTime endTime, String location, String sessionType,
                              String status) throws SQLException {
        Session session = new Session(title, sessionDate, startTime, endTime, location, sessionType, status);
        session.setSessionId(id);
        sessionService.updateSession(session);
    }

    // Update session with object
    public void updateSession(Session session) throws SQLException {
        sessionService.updateSession(session);
    }

    // Delete a session
    public void deleteSession(int id) throws SQLException {
        sessionService.deleteSession(id);
    }

    // Search sessions by type
    public List<Session> searchSessionsByType(String sessionType) throws SQLException {
        List<Session> allSessions = sessionService.getAllSessions();
        List<Session> filtered = new ArrayList<>();

        for (Session session : allSessions) {
            if (session.getSessionType().equalsIgnoreCase(sessionType)) {
                filtered.add(session);
            }
        }

        return filtered;
    }

    // Get active sessions only
    public List<Session> getActiveSessions() throws SQLException {
        List<Session> allSessions = sessionService.getAllSessions();
        List<Session> activeSessions = new ArrayList<>();

        for (Session session : allSessions) {
            if ("active".equalsIgnoreCase(session.getStatus())) {
                activeSessions.add(session);
            }
        }

        return activeSessions;
    }

    // Update session status only
    public boolean updateSessionStatus(int sessionId, String status) throws SQLException {
        try {
            Session session = sessionService.getSessionById(sessionId);
            if (session == null) {
                return false;
            }
            return sessionService.updateSessionStatus(sessionId, status);
        } catch (SQLException e) {
            throw new SQLException("Error updating session status: " + e.getMessage(), e);
        }
    }

    // Get sessions by date
    public List<Session> getSessionsByDate(LocalDate date) throws SQLException {
        List<Session> allSessions = sessionService.getAllSessions();
        List<Session> filtered = new ArrayList<>();

        for (Session session : allSessions) {
            if (session.getSessionDate().equals(date)) {
                filtered.add(session);
            }
        }

        return filtered;
    }

    // Get sessions by location
    public List<Session> getSessionsByLocation(String location) throws SQLException {
        List<Session> allSessions = sessionService.getAllSessions();
        List<Session> filtered = new ArrayList<>();

        for (Session session : allSessions) {
            if (session.getLocation().equalsIgnoreCase(location)) {
                filtered.add(session);
            }
        }

        return filtered;
    }

    // ========== NEW METHODS FOR PATIENT RESERVATIONS ==========

    // Get all available sessions (not reserved)
    public List<Session> getAvailableSessions() throws SQLException {
        return sessionService.getAvailableSessions();
    }

    // Get sessions reserved by a specific patient
    public List<Session> getPatientSessions(int patientId) throws SQLException {
        return sessionService.getPatientSessions(patientId);
    }

    // Get patient's upcoming sessions
    public List<Session> getPatientUpcomingSessions(int patientId) throws SQLException {
        return sessionService.getPatientUpcomingSessions(patientId);
    }

    // Get patient's past sessions
    public List<Session> getPatientPastSessions(int patientId) throws SQLException {
        return sessionService.getPatientPastSessions(patientId);
    }

    // Reserve a session
    public void reserveSession(int sessionId, int patientId) throws SQLException {
        sessionService.reserveSession(sessionId, patientId);
    }

    // Cancel a reservation
    public void cancelReservation(int sessionId, int patientId) throws SQLException {
        sessionService.cancelReservation(sessionId, patientId);
    }

    // Check if session is reserved by a specific patient
    public boolean isReservedByPatient(int sessionId, int patientId) throws SQLException {
        return sessionService.isReservedByPatient(sessionId, patientId);
    }

    // Search available sessions by keyword
    public List<Session> searchAvailableSessions(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAvailableSessions();
        }
        return sessionService.searchAvailableSessions(keyword.trim());
    }

    // Filter available sessions by type
    public List<Session> filterAvailableSessionsByType(String type) throws SQLException {
        return sessionService.filterAvailableSessionsByType(type);
    }

    // Get reservation count for a session
    public int getReservationCount(int sessionId) throws SQLException {
        return sessionService.getReservationCount(sessionId);
    }
}