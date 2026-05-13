package com.mentalhealth.app.models;

import com.mentalhealth.app.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private Integer createdBy;
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
    public void setTitle(String title) { this.title = title == null ? "" : title.trim(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = Math.max(0, maxParticipants); }

    public int getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(int cp) { this.currentParticipants = Math.max(0, cp); }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType == null ? "WORKSHOP" : eventType; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = Math.max(0, price); }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status == null ? "UPCOMING" : status; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // =================== BUSINESS LOGIC ===================

    public boolean isAvailable() {
        return getAvailableSpots() > 0 && "UPCOMING".equalsIgnoreCase(status);
    }

    public int getAvailableSpots() {
        return Math.max(0, maxParticipants - currentParticipants);
    }

    public boolean isFree() {
        return price == 0;
    }

    public boolean isSoldOut() {
        return maxParticipants > 0 && currentParticipants >= maxParticipants;
    }

    public boolean isUpcoming() {
        return "UPCOMING".equalsIgnoreCase(status);
    }

    public boolean isOngoing() {
        return "ONGOING".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(status);
    }

    public boolean isPast() {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    public boolean isFuture() {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    public long getDaysUntilEvent() {
        if (dateTime == null) return 0;
        return Math.max(0, ChronoUnit.DAYS.between(LocalDateTime.now(), dateTime));
    }

    public double getOccupancyPercentage() {
        if (maxParticipants <= 0) return 0;
        return Math.min(100, ((double) currentParticipants / maxParticipants) * 100);
    }

    public String getFormattedPrice() {
        return isFree() ? "FREE" : String.format("$%.2f", price);
    }

    public String getShortDescription(int maxLength) {
        if (description == null || description.trim().isEmpty()) return "";
        if (description.length() <= maxLength) return description;
        return description.substring(0, maxLength) + "...";
    }

    public String getShortTitle(int maxLength) {
        if (title == null || title.trim().isEmpty()) return "";
        if (title.length() <= maxLength) return title;
        return title.substring(0, maxLength) + "...";
    }

    public String getEventTypeEmoji() {
        if (eventType == null) return "📅";
        return switch (eventType) {
            case "WORKSHOP" -> "🛠";
            case "GROUP_THERAPY" -> "👥";
            case "SEMINAR" -> "🎓";
            case "SOCIAL" -> "🎉";
            default -> "📅";
        };
    }

    public String getStatusEmoji() {
        if (status == null) return "⚪";
        return switch (status) {
            case "UPCOMING" -> "🟢";
            case "ONGOING" -> "🔵";
            case "COMPLETED" -> "⚫";
            case "CANCELLED" -> "🔴";
            default -> "⚪";
        };
    }

    @Override
    public String toString() {
        return title + " - " + (dateTime != null ? dateTime.toLocalDate().toString() : "No date");
    }

    // =================== CREATE ===================

    public boolean save() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (status == null) status = "UPCOMING";
        if (eventType == null) eventType = "WORKSHOP";

        String sql = "INSERT INTO events (" +
                "title, description, date_time, location, " +
                "max_participants, current_participants, event_type, price, " +
                "image_url, status, created_by, created_at, updated_at" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DatabaseConnection.getConnection()
                .prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setString(2, description);

            if (dateTime != null) {
                ps.setTimestamp(3, Timestamp.valueOf(dateTime));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setString(4, location);
            ps.setInt(5, maxParticipants);
            ps.setInt(6, currentParticipants);
            ps.setString(7, eventType);
            ps.setDouble(8, price);
            ps.setString(9, imageUrl);
            ps.setString(10, status);

            if (createdBy != null) {
                ps.setInt(11, createdBy);
            } else {
                ps.setNull(11, Types.INTEGER);
            }

            ps.setTimestamp(12, Timestamp.valueOf(createdAt));
            ps.setTimestamp(13, Timestamp.valueOf(updatedAt));

            int rows = ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                this.id = keys.getInt(1);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("Error saving event: " + e.getMessage());
            return false;
        }
    }

    // =================== READ ===================

    public static List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY date_time ASC";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(fromResultSet(rs));
            }

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
            if (rs.next()) {
                return fromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Error finding event: " + e.getMessage());
        }

        return null;
    }

    public static List<Event> search(String keyword) {
        List<Event> list = new ArrayList<>();

        String sql = "SELECT * FROM events " +
                "WHERE title LIKE ? OR description LIKE ? OR location LIKE ? " +
                "ORDER BY date_time ASC";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            String p = "%" + keyword + "%";
            ps.setString(1, p);
            ps.setString(2, p);
            ps.setString(3, p);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error searching events: " + e.getMessage());
        }

        return list;
    }

    public static List<Event> findByType(String type) {
        List<Event> list = new ArrayList<>();

        String sql = "SELECT * FROM events WHERE event_type=? ORDER BY date_time ASC";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, type);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(fromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error filtering events: " + e.getMessage());
        }

        return list;
    }

    public static int count() {
        String sql = "SELECT COUNT(*) FROM events";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error counting events: " + e.getMessage());
        }

        return 0;
    }

    public static int upcomingCount() {
        String sql = "SELECT COUNT(*) FROM events WHERE status='UPCOMING'";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error counting upcoming events: " + e.getMessage());
        }

        return 0;
    }

    public static int totalParticipants() {
        String sql = "SELECT COALESCE(SUM(current_participants),0) FROM events";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Error summing participants: " + e.getMessage());
        }

        return 0;
    }

    // =================== UPDATE ===================

    public boolean update() {
        updatedAt = LocalDateTime.now();

        String sql = "UPDATE events SET " +
                "title=?, description=?, date_time=?, location=?, " +
                "max_participants=?, current_participants=?, event_type=?, price=?, " +
                "image_url=?, status=?, created_by=?, updated_at=? " +
                "WHERE id=?";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, description);

            if (dateTime != null) {
                ps.setTimestamp(3, Timestamp.valueOf(dateTime));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setString(4, location);
            ps.setInt(5, maxParticipants);
            ps.setInt(6, currentParticipants);
            ps.setString(7, eventType);
            ps.setDouble(8, price);
            ps.setString(9, imageUrl);
            ps.setString(10, status);

            if (createdBy != null) {
                ps.setInt(11, createdBy);
            } else {
                ps.setNull(11, Types.INTEGER);
            }

            ps.setTimestamp(12, Timestamp.valueOf(updatedAt));
            ps.setInt(13, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            return false;
        }
    }

    // =================== DELETE ===================

    public boolean delete() {
        String sql = "DELETE FROM events WHERE id=?";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            return false;
        }
    }

    // =================== MAPPER ===================

    private static Event fromResultSet(ResultSet rs) throws SQLException {
        Event e = new Event();

        e.id = rs.getInt("id");
        e.title = rs.getString("title");
        e.description = rs.getString("description");

        Timestamp dateTs = rs.getTimestamp("date_time");
        if (dateTs != null) {
            e.dateTime = dateTs.toLocalDateTime();
        }

        e.location = rs.getString("location");
        e.maxParticipants = rs.getInt("max_participants");
        e.currentParticipants = rs.getInt("current_participants");
        e.eventType = rs.getString("event_type");
        e.price = rs.getDouble("price");
        e.imageUrl = rs.getString("image_url");
        e.status = rs.getString("status");

        int createdByValue = rs.getInt("created_by");
        if (!rs.wasNull()) {
            e.createdBy = createdByValue;
        }

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            e.createdAt = createdTs.toLocalDateTime();
        }

        Timestamp updatedTs = rs.getTimestamp("updated_at");
        if (updatedTs != null) {
            e.updatedAt = updatedTs.toLocalDateTime();
        }

        return e;
    }
}