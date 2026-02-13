package models;

import java.time.LocalDateTime;

/**
 * ContentPath Model - Tracks which user accessed which content node and when.
 * Used for audit trail and user activity logging.
 * UPDATED: Added additional fields for JOIN queries
 */
public class ContentPath {
    private int pathId;
    private int userId;
    private int nodeId;
    private LocalDateTime accessedAt;

    // Additional fields from JOIN queries (not in database)
    private String contentTitle;
    private String userFirstName;
    private String userLastName;
    private String userEmail;

    // Constructors
    public ContentPath() {
    }

    public ContentPath(int userId, int nodeId) {
        this.userId = userId;
        this.nodeId = nodeId;
        this.accessedAt = LocalDateTime.now();
    }

    public ContentPath(int userId, int nodeId, LocalDateTime accessedAt) {
        this.userId = userId;
        this.nodeId = nodeId;
        this.accessedAt = accessedAt;
    }

    // Getters and Setters
    public int getPathId() { return pathId; }
    public void setPathId(int pathId) { this.pathId = pathId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getNodeId() { return nodeId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }

    public LocalDateTime getAccessedAt() { return accessedAt; }
    public void setAccessedAt(LocalDateTime accessedAt) { this.accessedAt = accessedAt; }

    // Additional getters/setters for JOIN fields
    public String getContentTitle() { return contentTitle; }
    public void setContentTitle(String contentTitle) { this.contentTitle = contentTitle; }

    public String getUserFirstName() { return userFirstName; }
    public void setUserFirstName(String userFirstName) { this.userFirstName = userFirstName; }

    public String getUserLastName() { return userLastName; }
    public void setUserLastName(String userLastName) { this.userLastName = userLastName; }

    public String getUserFullName() {
        return (userFirstName != null ? userFirstName : "") + " " +
                (userLastName != null ? userLastName : "");
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @Override
    public String toString() {
        return "ContentPath{" +
                "pathId=" + pathId +
                ", userId=" + userId +
                ", nodeId=" + nodeId +
                ", accessedAt=" + accessedAt +
                '}';
    }
}