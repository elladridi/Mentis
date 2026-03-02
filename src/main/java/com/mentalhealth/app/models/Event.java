package com.mentalhealth.app.models;

import com.mentalhealth.app.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {

    private int id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private int maxParticipants;
    private int currentParticipants;
    private String eventType;
    private double price;
    private String imageUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Event() {}

    public Event(String title, String description, LocalDateTime dateTime,
                 String location, int maxParticipants, String eventType, double price) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.location = location;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = 0;
        this.eventType = eventType;
        this.price = price;
        this.status = "UPCOMING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // =================== GETTERS & SETTERS ===================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int cp) { this.currentParticipants = cp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // =================== BUSINESS LOGIC ===================

    public boolean isAvailable() { return currentParticipants < maxParticipants; }
    public int getAvailableSpots() { return maxParticipants - currentParticipants; }
    public boolean isFree() { return price == 0; }

    @Override
    public String toString() {
        return title + " - " + (dateTime != null ? dateTime.toLocalDate().toString() : "No date");
    }

    // =================== CREATE ===================

    public void save() {
        String sql = "INSERT INTO events (title, description, date_time, location, " +
                "max_participants, current_participants, event_type, price, image_url, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setTimestamp(3, Timestamp.valueOf(dateTime));
            ps.setString(4, location);
            ps.setInt(5, maxParticipants);
            ps.setInt(6, currentParticipants);
            ps.setString(7, eventType);
            ps.setDouble(8, price);
            ps.setString(9, imageUrl);
            ps.setString(10, status);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) this.id = keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error saving event: " + e.getMessage());
        }
    }

    // =================== READ ===================

    public static List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY date_time";
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching events: " + e.getMessage());
        }
        return list;
    }

    public static Event findById(int id) {
        String sql = "SELECT * FROM events WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return fromResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Error finding event: " + e.getMessage());
        }
        return null;
    }

    public static List<Event> search(String keyword) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE title LIKE ? OR description LIKE ? ORDER BY date_time";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            String p = "%" + keyword + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Error searching: " + e.getMessage());
        }
        return list;
    }

    public static List<Event> findByType(String type) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE event_type=? ORDER BY date_time";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(fromResultSet(rs));
        } catch (SQLException e) {
            System.err.println("Error filtering: " + e.getMessage());
        }
        return list;
    }

    public static int count() {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM events")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting: " + e.getMessage());
        }
        return 0;
    }

    public static int totalParticipants() {
        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT COALESCE(SUM(current_participants),0) FROM events")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error summing: " + e.getMessage());
        }
        return 0;
    }

    // =================== UPDATE ===================

    public void update() {
        String sql = "UPDATE events SET title=?, description=?, date_time=?, location=?, " +
                "max_participants=?, current_participants=?, event_type=?, price=?, " +
                "image_url=?, status=?, updated_at=NOW() WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setTimestamp(3, Timestamp.valueOf(dateTime));
            ps.setString(4, location);
            ps.setInt(5, maxParticipants);
            ps.setInt(6, currentParticipants);
            ps.setString(7, eventType);
            ps.setDouble(8, price);
            ps.setString(9, imageUrl);
            ps.setString(10, status);
            ps.setInt(11, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating: " + e.getMessage());
        }
    }

    // =================== DELETE ===================

    public void delete() {
        String sql = "DELETE FROM events WHERE id=?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting: " + e.getMessage());
        }
    }

    // =================== MAPPER ===================

    private static Event fromResultSet(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDateTime(rs.getTimestamp("date_time").toLocalDateTime());
        e.setLocation(rs.getString("location"));
        e.setMaxParticipants(rs.getInt("max_participants"));
        e.setCurrentParticipants(rs.getInt("current_participants"));
        e.setEventType(rs.getString("event_type"));
        e.setPrice(rs.getDouble("price"));
        e.setImageUrl(rs.getString("image_url"));
        e.setStatus(rs.getString("status"));
        e.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        e.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return e;
    }
}