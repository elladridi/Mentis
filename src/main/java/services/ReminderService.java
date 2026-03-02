package services;

import models.Session;
import utils.MyDB;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // ⭐ ADD THIS IMPORT
import java.util.ArrayList;
import java.util.List;

public class ReminderService {

    private Connection cnx;
    private WeatherService weatherService;

    public ReminderService() {
        cnx = MyDB.getInstance().getConnection();
        weatherService = new WeatherService();
    }

    // Check for sessions that need reminders (24h before)
    public List<Session> getSessionsForReminder() throws SQLException {
        List<Session> sessions = new ArrayList<>();

        String sql = "SELECT s.* FROM sessions s " +
                "WHERE s.reserved_by IS NOT NULL " +
                "AND s.reminder_sent = FALSE " +
                "AND TIMESTAMPDIFF(HOUR, NOW(), CONCAT(s.session_date, ' ', s.start_time)) BETWEEN 23 AND 25";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            sessions.add(extractSession(rs));
        }
        return sessions;
    }

    // Send reminder for a session
    public void sendReminder(Session session) throws SQLException {
        // Get weather forecast
        String weatherForecast = weatherService.getWeatherForecast(
                session.getLocation(),
                session.getSessionDate().toString()
        );

        // Store in pending_reminders table
        String sql = "INSERT INTO pending_reminders (session_id, patient_id, weather_forecast, created_at) " +
                "VALUES (?, ?, ?, NOW())";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, session.getSessionId());
        ps.setInt(2, session.getReservedBy());
        ps.setString(3, weatherForecast);
        ps.executeUpdate();

        // Mark reminder as sent
        markReminderSent(session.getSessionId());

        System.out.println("✅ Reminder stored for session " + session.getSessionId() +
                " (patient " + session.getReservedBy() + ")");
    }

    private void markReminderSent(int sessionId) throws SQLException {
        String sql = "UPDATE sessions SET reminder_sent = TRUE WHERE session_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, sessionId);
        ps.executeUpdate();
    }

    // Get pending reminders for a patient (called at login)
    public List<PendingReminder> getPendingReminders(int patientId) throws SQLException {
        List<PendingReminder> reminders = new ArrayList<>();

        String sql = "SELECT pr.*, s.title, s.session_date, s.start_time, s.end_time, s.location, s.session_type " +
                "FROM pending_reminders pr " +
                "JOIN sessions s ON pr.session_id = s.session_id " +
                "WHERE pr.patient_id = ? AND pr.shown = FALSE " +
                "ORDER BY pr.created_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            PendingReminder pr = new PendingReminder();
            pr.setReminderId(rs.getInt("reminder_id"));
            pr.setSessionId(rs.getInt("session_id"));
            pr.setPatientId(rs.getInt("patient_id"));
            pr.setSessionTitle(rs.getString("title"));
            pr.setSessionDate(rs.getDate("session_date").toLocalDate());
            pr.setStartTime(rs.getTime("start_time").toLocalTime());
            pr.setEndTime(rs.getTime("end_time").toLocalTime());
            pr.setLocation(rs.getString("location"));
            pr.setSessionType(rs.getString("session_type"));
            pr.setWeatherForecast(rs.getString("weather_forecast"));
            pr.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            reminders.add(pr);
        }
        return reminders;
    }

    // Mark reminder as shown and get confirmation
    public void confirmReminder(int reminderId, int sessionId, int patientId) throws SQLException {
        // Mark reminder as shown
        String sql1 = "UPDATE pending_reminders SET shown = TRUE WHERE reminder_id = ?";
        PreparedStatement ps1 = cnx.prepareStatement(sql1);
        ps1.setInt(1, reminderId);
        ps1.executeUpdate();

        // Update session confirmation
        String sql2 = "UPDATE sessions SET patient_confirmed = TRUE, confirmed_at = NOW() " +
                "WHERE session_id = ? AND reserved_by = ?";
        PreparedStatement ps2 = cnx.prepareStatement(sql2);
        ps2.setInt(1, sessionId);
        ps2.setInt(2, patientId);
        ps2.executeUpdate();

        System.out.println("✅ Patient " + patientId + " confirmed session " + sessionId);
    }

    // Get confirmation statistics for a psychologist
    public String getConfirmationStats(int psychologistId) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN patient_confirmed = TRUE THEN 1 ELSE 0 END) as confirmed, " +
                "SUM(CASE WHEN patient_confirmed = FALSE AND session_date >= CURDATE() THEN 1 ELSE 0 END) as pending " +
                "FROM sessions WHERE psychologist_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, psychologistId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int total = rs.getInt("total");
            int confirmed = rs.getInt("confirmed");
            int pending = rs.getInt("pending");
            double rate = total > 0 ? (confirmed * 100.0 / total) : 0;

            return String.format(
                    "📊 Confirmation Stats:\n" +
                            "Total sessions: %d\n" +
                            "✅ Confirmed: %d (%.1f%%)\n" +
                            "⏳ Pending: %d",
                    total, confirmed, rate, pending
            );
        }
        return "No data available";
    }

    private Session extractSession(ResultSet rs) throws SQLException {
        Session s = new Session();
        s.setSessionId(rs.getInt("session_id"));
        s.setTitle(rs.getString("title"));
        s.setSessionDate(rs.getDate("session_date").toLocalDate());
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setLocation(rs.getString("location"));
        s.setSessionType(rs.getString("session_type"));
        s.setStatus(rs.getString("status"));
        s.setReservedBy(rs.getInt("reserved_by"));
        return s;
    }

    // Inner class for pending reminders
    public static class PendingReminder {
        private int reminderId;
        private int sessionId;
        private int patientId;
        private String sessionTitle;
        private LocalDate sessionDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String sessionType;
        private String weatherForecast;
        private LocalDateTime createdAt;

        // Getters and setters
        public int getReminderId() { return reminderId; }
        public void setReminderId(int reminderId) { this.reminderId = reminderId; }

        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }

        public int getPatientId() { return patientId; }
        public void setPatientId(int patientId) { this.patientId = patientId; }

        public String getSessionTitle() { return sessionTitle; }
        public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }

        public LocalDate getSessionDate() { return sessionDate; }
        public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }

        public String getWeatherForecast() { return weatherForecast; }
        public void setWeatherForecast(String weatherForecast) { this.weatherForecast = weatherForecast; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        // ⭐ FIXED: Helper to get formatted time range
        public String getTimeRange() {
            DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
            return startTime.format(tf) + " - " + endTime.format(tf);
        }
    }
}