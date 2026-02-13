package models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List; // Add this import for List

public class Session {
    private int sessionId;
    private String title;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String sessionType;
    private String status;

    // Fields for patient reservation
    private Integer reservedBy;
    private LocalDateTime reservedAt;

    // ⭐ NEW FIELDS for recommendations
    private String category; // e.g., "Anxiety", "Depression", "Stress", "General"
    private List<String> tags; // e.g., ["morning", "relaxation", "group"]
    private int popularity; // number of times booked
    private double averageRating; // average rating from reviews

    // Default constructor
    public Session() {}

    // Constructor without ID
    public Session(String title, LocalDate sessionDate, LocalTime startTime,
                   LocalTime endTime, String location, String sessionType, String status) {
        this.title = title;
        this.sessionDate = sessionDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.sessionType = sessionType;
        this.status = status;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters and Setters for reserved fields
    public Integer getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(Integer reservedBy) {
        this.reservedBy = reservedBy;
    }

    public LocalDateTime getReservedAt() {
        return reservedAt;
    }

    public void setReservedAt(LocalDateTime reservedAt) {
        this.reservedAt = reservedAt;
    }

    // ⭐ NEW GETTERS AND SETTERS for recommendation fields

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    // HELPER METHODS

    // Check if session is available
    public boolean isAvailable() {
        return reservedBy == null;
    }

    // Check if session is reserved by a specific patient
    public boolean isReservedBy(int patientId) {
        return reservedBy != null && reservedBy == patientId;
    }

    // ⭐ NEW HELPER METHODS for recommendations

    // Check if session has a specific tag
    public boolean hasTag(String tag) {
        return tags != null && tags.contains(tag);
    }

    // Increment popularity (call when session is booked)
    public void incrementPopularity() {
        this.popularity++;
    }

    // Update average rating with new rating
    public void updateAverageRating(int newRating) {
        // This is a simplified version - in real implementation,
        // you'd need to know the total number of ratings
        this.averageRating = (this.averageRating + newRating) / 2;
    }

    // Get category based on session type (if not explicitly set)
    public String inferCategory() {
        if (category != null) return category;

        // Infer category from session type
        switch (sessionType.toLowerCase()) {
            case "individual":
                return "Personal Growth";
            case "group":
                return "Social Support";
            case "family":
                return "Family Therapy";
            case "couple":
                return "Relationship";
            case "online":
                return "Convenient Care";
            default:
                return "General";
        }
    }
}