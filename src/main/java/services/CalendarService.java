package services;

import models.Session;
import utils.MyDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarService {

    private Connection cnx;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public CalendarService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // Get all reserved sessions for calendar
    public List<CalendarEvent> getReservedSessions() throws SQLException {
        List<CalendarEvent> events = new ArrayList<>();

        // ⭐ FIXED: Using correct column names from your database
        String sql = "SELECT s.*, u.id as user_id, u.firstname, u.lastname, u.email " +
                "FROM sessions s " +
                "LEFT JOIN user u ON s.reserved_by = u.id " +
                "WHERE s.reserved_by IS NOT NULL " +
                "ORDER BY s.session_date, s.start_time";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            try {
                CalendarEvent event = new CalendarEvent();
                event.setSessionId(rs.getInt("session_id"));
                event.setTitle(rs.getString("title"));
                event.setDate(rs.getDate("session_date").toLocalDate());
                event.setStartTime(rs.getTime("start_time").toLocalTime());
                event.setEndTime(rs.getTime("end_time").toLocalTime());
                event.setLocation(rs.getString("location"));
                event.setSessionType(rs.getString("session_type"));
                event.setPatientId(rs.getInt("reserved_by"));

                // ⭐ FIXED: Using firstname and lastname columns
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String patientName = "Patient " + rs.getInt("reserved_by");

                if (firstName != null && lastName != null && !firstName.isEmpty() && !lastName.isEmpty()) {
                    patientName = firstName + " " + lastName;
                } else if (firstName != null && !firstName.isEmpty()) {
                    patientName = firstName;
                } else if (rs.getString("email") != null) {
                    patientName = rs.getString("email");
                }
                event.setPatientName(patientName);

                event.setStatus(rs.getString("status"));

                events.add(event);
            } catch (Exception e) {
                System.err.println("Error processing session: " + e.getMessage());
            }
        }
        return events;
    }

    // Get sessions grouped by date
    public Map<LocalDate, List<CalendarEvent>> getSessionsByDate() throws SQLException {
        Map<LocalDate, List<CalendarEvent>> calendarMap = new HashMap<>();
        List<CalendarEvent> allEvents = getReservedSessions();

        for (CalendarEvent event : allEvents) {
            calendarMap
                    .computeIfAbsent(event.getDate(), k -> new ArrayList<>())
                    .add(event);
        }

        return calendarMap;
    }

    // Get sessions for a specific date range
    public List<CalendarEvent> getSessionsInRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<CalendarEvent> events = new ArrayList<>();

        // ⭐ FIXED: Using correct column names from your database
        String sql = "SELECT s.*, u.id as user_id, u.firstname, u.lastname, u.email " +
                "FROM sessions s " +
                "LEFT JOIN user u ON s.reserved_by = u.id " +
                "WHERE s.reserved_by IS NOT NULL " +
                "AND s.session_date BETWEEN ? AND ? " +
                "ORDER BY s.session_date, s.start_time";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(startDate));
        ps.setDate(2, Date.valueOf(endDate));
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            try {
                CalendarEvent event = new CalendarEvent();
                event.setSessionId(rs.getInt("session_id"));
                event.setTitle(rs.getString("title"));
                event.setDate(rs.getDate("session_date").toLocalDate());
                event.setStartTime(rs.getTime("start_time").toLocalTime());
                event.setEndTime(rs.getTime("end_time").toLocalTime());
                event.setLocation(rs.getString("location"));
                event.setSessionType(rs.getString("session_type"));
                event.setPatientId(rs.getInt("reserved_by"));

                // ⭐ FIXED: Using firstname and lastname columns
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String patientName = "Patient " + rs.getInt("reserved_by");

                if (firstName != null && lastName != null && !firstName.isEmpty() && !lastName.isEmpty()) {
                    patientName = firstName + " " + lastName;
                } else if (firstName != null && !firstName.isEmpty()) {
                    patientName = firstName;
                } else if (rs.getString("email") != null) {
                    patientName = rs.getString("email");
                }
                event.setPatientName(patientName);

                event.setStatus(rs.getString("status"));

                events.add(event);
            } catch (Exception e) {
                System.err.println("Error processing session: " + e.getMessage());
            }
        }
        return events;
    }

    // Get today's sessions
    public List<CalendarEvent> getTodaySessions() throws SQLException {
        return getSessionsInRange(LocalDate.now(), LocalDate.now());
    }

    // Get upcoming sessions (from tomorrow onwards)
    public List<CalendarEvent> getUpcomingSessions() throws SQLException {
        return getSessionsInRange(LocalDate.now(), LocalDate.now().plusMonths(1));
    }

    // Inner class for calendar events
    public static class CalendarEvent {
        private int sessionId;
        private String title;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String location;
        private String sessionType;
        private int patientId;
        private String patientName;
        private String status;

        // Getters and setters
        public int getSessionId() { return sessionId; }
        public void setSessionId(int sessionId) { this.sessionId = sessionId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getStartTime() { return startTime; }
        public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

        public LocalTime getEndTime() { return endTime; }
        public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getSessionType() { return sessionType; }
        public void setSessionType(String sessionType) { this.sessionType = sessionType; }

        public int getPatientId() { return patientId; }
        public void setPatientId(int patientId) { this.patientId = patientId; }

        public String getPatientName() { return patientName; }
        public void setPatientName(String patientName) { this.patientName = patientName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getTimeRange() {
            return startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                    endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        public String getDisplayColor() {
            if (sessionType == null) return "#95a5a6";
            switch (sessionType.toLowerCase()) {
                case "individual": return "#3498db"; // Blue
                case "group": return "#2ecc71"; // Green
                case "family": return "#9b59b6"; // Purple
                case "couple": return "#e67e22"; // Orange
                case "online": return "#1abc9c"; // Turquoise
                default: return "#95a5a6"; // Gray
            }
        }
    }
}