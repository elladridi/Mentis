package services;

import models.Session;
import utils.MyDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {

    private Connection cnx;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public AnalyticsService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // Get all sessions for analysis
    private List<Session> getAllSessions() throws SQLException {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT s.*, u.firstname, u.lastname FROM sessions s " +
                "LEFT JOIN user u ON s.reserved_by = u.id " +
                "ORDER BY s.session_date DESC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Session s = new Session();
            s.setSessionId(rs.getInt("session_id"));
            s.setTitle(rs.getString("title"));
            s.setSessionDate(rs.getDate("session_date").toLocalDate());
            s.setStartTime(rs.getTime("start_time").toLocalTime());
            s.setEndTime(rs.getTime("end_time").toLocalTime());
            s.setLocation(rs.getString("location"));
            s.setSessionType(rs.getString("session_type"));
            s.setStatus(rs.getString("status"));
            s.setReservedBy(rs.getObject("reserved_by") != null ? rs.getInt("reserved_by") : null);
            sessions.add(s);
        }
        return sessions;
    }

    // Get reserved sessions only
    private List<Session> getReservedSessions() throws SQLException {
        return getAllSessions().stream()
                .filter(s -> s.getReservedBy() != null)
                .collect(Collectors.toList());
    }

    // 1. Most popular session types
    public Map<String, Integer> getPopularSessionTypes() throws SQLException {
        Map<String, Integer> typeCount = new HashMap<>();

        for (Session s : getReservedSessions()) {
            String type = s.getSessionType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        return typeCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // 2. Most popular session titles
    public Map<String, Integer> getPopularTitles() throws SQLException {
        Map<String, Integer> titleCount = new HashMap<>();

        for (Session s : getReservedSessions()) {
            String title = s.getTitle();
            titleCount.put(title, titleCount.getOrDefault(title, 0) + 1);
        }

        return titleCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // 3. Most popular locations
    public Map<String, Integer> getPopularLocations() throws SQLException {
        Map<String, Integer> locationCount = new HashMap<>();

        for (Session s : getReservedSessions()) {
            String location = s.getLocation();
            locationCount.put(location, locationCount.getOrDefault(location, 0) + 1);
        }

        return locationCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // 4. Booking trends by day of week
    public Map<String, Integer> getBookingsByDayOfWeek() throws SQLException {
        Map<String, Integer> dayCount = new HashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (String day : days) {
            dayCount.put(day, 0);
        }

        for (Session s : getReservedSessions()) {
            String day = s.getSessionDate().getDayOfWeek().toString();
            // Capitalize first letter only
            day = day.substring(0, 1) + day.substring(1).toLowerCase();
            dayCount.put(day, dayCount.getOrDefault(day, 0) + 1);
        }

        return dayCount;
    }

    // 5. Booking trends by hour
    public Map<String, Integer> getBookingsByHour() throws SQLException {
        Map<String, Integer> hourCount = new HashMap<>();

        for (int i = 0; i < 24; i++) {
            hourCount.put(String.format("%02d:00", i), 0);
        }

        for (Session s : getReservedSessions()) {
            int hour = s.getStartTime().getHour();
            String hourKey = String.format("%02d:00", hour);
            hourCount.put(hourKey, hourCount.getOrDefault(hourKey, 0) + 1);
        }

        return hourCount;
    }

    // 6. Monthly booking trends
    public Map<String, Integer> getMonthlyTrends() throws SQLException {
        Map<String, Integer> monthlyCount = new HashMap<>();

        for (Session s : getReservedSessions()) {
            String month = s.getSessionDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            monthlyCount.put(month, monthlyCount.getOrDefault(month, 0) + 1);
        }

        return monthlyCount;
    }

    // 7. Top patients (most bookings)
    public Map<String, Integer> getTopPatients() throws SQLException {
        Map<String, Integer> patientCount = new HashMap<>();

        String sql = "SELECT u.firstname, u.lastname, COUNT(*) as booking_count " +
                "FROM sessions s " +
                "JOIN user u ON s.reserved_by = u.id " +
                "WHERE s.reserved_by IS NOT NULL " +
                "GROUP BY s.reserved_by " +
                "ORDER BY booking_count DESC " +
                "LIMIT 5";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            String name = rs.getString("firstname") + " " + rs.getString("lastname");
            int count = rs.getInt("booking_count");
            patientCount.put(name, count);
        }

        return patientCount;
    }

    // 8. Session status distribution
    public Map<String, Integer> getStatusDistribution() throws SQLException {
        Map<String, Integer> statusCount = new HashMap<>();

        for (Session s : getAllSessions()) {
            String status = s.getStatus() != null ? s.getStatus() : "unknown";
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
        }

        return statusCount;
    }

    // 9. Completion rate (reserved vs completed)
    public double getCompletionRate() throws SQLException {
        List<Session> allSessions = getAllSessions();
        List<Session> reservedSessions = getReservedSessions();

        if (allSessions.isEmpty()) return 0;

        // Assuming sessions with reserved_by are considered "booked"
        // You might want to add a 'completed' flag to sessions table for more accuracy
        return (reservedSessions.size() * 100.0) / allSessions.size();
    }

    // 10. Get summary statistics
    public AnalyticsSummary getSummaryStats() throws SQLException {
        List<Session> allSessions = getAllSessions();
        List<Session> reservedSessions = getReservedSessions();

        int totalSessions = allSessions.size();
        int totalReserved = reservedSessions.size();
        int uniquePatients = (int) reservedSessions.stream()
                .map(Session::getReservedBy)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        double avgBookingsPerPatient = uniquePatients > 0 ?
                (double) totalReserved / uniquePatients : 0;

        return new AnalyticsSummary(totalSessions, totalReserved, uniquePatients, avgBookingsPerPatient);
    }

    // Inner class for summary stats
    public static class AnalyticsSummary {
        private int totalSessions;
        private int totalReserved;
        private int uniquePatients;
        private double avgBookingsPerPatient;

        public AnalyticsSummary(int totalSessions, int totalReserved, int uniquePatients, double avgBookingsPerPatient) {
            this.totalSessions = totalSessions;
            this.totalReserved = totalReserved;
            this.uniquePatients = uniquePatients;
            this.avgBookingsPerPatient = avgBookingsPerPatient;
        }

        public int getTotalSessions() { return totalSessions; }
        public int getTotalReserved() { return totalReserved; }
        public int getUniquePatients() { return uniquePatients; }
        public double getAvgBookingsPerPatient() { return avgBookingsPerPatient; }
    }
}