package models;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ContentNode Model - Represents a hierarchical content node (file/document)
 * linked to a user, with optional parent-child relationships.
 * FIXED: Added assignedUsers field for patient access control
 */
public class ContentNode {
    private int nodeId;
    private String title;
    private String description;
    private String pdfPath;           // File path for uploaded PDF
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int createdBy;            // User ID who created this content
    private Integer parentNodeId;     // NULL if root, otherwise parent node ID
    private String assignedUsers;     // JSON string: "[1,2,3]" - users who can view this content

    // Constructors
    public ContentNode() {
    }

    public ContentNode(String title, String description, int createdBy) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.parentNodeId = null;
        this.assignedUsers = "[]"; // Empty array by default
    }

    public ContentNode(String title, String description, String pdfPath, int createdBy, Integer parentNodeId) {
        this.title = title;
        this.description = description;
        this.pdfPath = pdfPath;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.parentNodeId = parentNodeId;
        this.assignedUsers = "[]"; // Empty array by default
    }

    public ContentNode(String title, String description, String pdfPath, int createdBy,
                       Integer parentNodeId, String assignedUsers) {
        this.title = title;
        this.description = description;
        this.pdfPath = pdfPath;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.parentNodeId = parentNodeId;
        this.assignedUsers = assignedUsers != null ? assignedUsers : "[]";
    }

    // Getters and Setters
    public int getNodeId() { return nodeId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPdfPath() { return pdfPath; }
    public void setPdfPath(String pdfPath) { this.pdfPath = pdfPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public Integer getParentNodeId() { return parentNodeId; }
    public void setParentNodeId(Integer parentNodeId) { this.parentNodeId = parentNodeId; }

    public String getAssignedUsers() { return assignedUsers; }
    public void setAssignedUsers(String assignedUsers) {
        this.assignedUsers = assignedUsers != null ? assignedUsers : "[]";
    }

    // ==================== HELPER METHODS FOR ASSIGNED USERS ====================

    /**
     * Get assigned users as List of Integers
     */
    public List<Integer> getAssignedUsersList() {
        if (assignedUsers == null || assignedUsers.trim().isEmpty() || assignedUsers.equals("[]")) {
            return new ArrayList<>();
        }

        try {
            // Remove brackets and split
            String content = assignedUsers.trim();
            if (content.startsWith("[") && content.endsWith("]")) {
                content = content.substring(1, content.length() - 1);
            }

            if (content.trim().isEmpty()) {
                return new ArrayList<>();
            }

            return Arrays.stream(content.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error parsing assigned users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Set assigned users from List of Integers
     */
    public void setAssignedUsersList(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            this.assignedUsers = "[]";
        } else {
            this.assignedUsers = "[" +
                    userIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",")) +
                    "]";
        }
    }

    /**
     * Check if a user is assigned to this content
     */
    public boolean isUserAssigned(int userId) {
        return getAssignedUsersList().contains(userId);
    }

    /**
     * Add a user to assigned list
     */
    public void assignUser(int userId) {
        List<Integer> users = getAssignedUsersList();
        if (!users.contains(userId)) {
            users.add(userId);
            setAssignedUsersList(users);
        }
    }

    /**
     * Remove a user from assigned list
     */
    public void unassignUser(int userId) {
        List<Integer> users = getAssignedUsersList();
        users.remove(Integer.valueOf(userId));
        setAssignedUsersList(users);
    }

    @Override
    public String toString() {
        return "ContentNode{" +
                "nodeId=" + nodeId +
                ", title='" + title + '\'' +
                ", createdBy=" + createdBy +
                ", parentNodeId=" + parentNodeId +
                ", updatedAt=" + updatedAt +
                ", assignedUsers=" + assignedUsers +
                '}';
    }
}
