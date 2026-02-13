package controller;

import models.ContentPath;
import services.ContentPathService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ContentPathController - Business logic for access logs and statistics
 * SEPARATE from ContentNodeController - SINGLE RESPONSIBILITY PRINCIPLE
 */
public class ContentPathController {

    private ContentPathService pathService;
    private int currentUserId;
    private String currentUserRole;

    public ContentPathController() {
        this.pathService = new ContentPathService();
        this.currentUserId = 0;
        this.currentUserRole = "";
    }

    public void setCurrentUser(int userId, String userRole) {
        this.currentUserId = userId;
        this.currentUserRole = userRole != null ? userRole.toLowerCase() : "";
        System.out.println("✅ ContentPathController: User set to ID=" + userId + ", Role=" + this.currentUserRole);
    }

    private boolean isAdmin() {
        return "admin".equals(currentUserRole);
    }

    // ==================== ACCESS LOGS ====================

    public List<ContentPath> getMyAccessLogs() {
        try {
            return pathService.getAccessLogsByUser(currentUserId, currentUserId, isAdmin());
        } catch (SQLException e) {
            System.err.println("❌ Error getting access logs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<ContentPath> getAccessLogsByUser(int userId) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view other users' access logs");
        }
        try {
            return pathService.getAccessLogsByUser(userId, currentUserId, true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting user access logs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<ContentPath> getAllAccessLogs() {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view all access logs");
        }
        try {
            return pathService.getAllAccessLogs(true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting all access logs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteAccessLog(int pathId) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can delete access logs");
        }
        try {
            pathService.deleteAccessLog(pathId, true);
            System.out.println("✅ Access log deleted: " + pathId);
        } catch (SQLException e) {
            System.err.println("❌ Error deleting access log: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public int deleteOldAccessLogs(int daysOld) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can delete old access logs");
        }
        try {
            int deleted = pathService.deleteOldAccessLogs(daysOld, true);
            System.out.println("✅ Deleted " + deleted + " access logs older than " + daysOld + " days");
            return deleted;
        } catch (SQLException e) {
            System.err.println("❌ Error deleting old access logs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ==================== STATISTICS ====================

    public int getActiveUsersCount() {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }
        try {
            return pathService.getActiveUsersCount(true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting active users count: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ==================== EXPORT METHODS ====================

    public String exportAccessLogsToCSV() {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can export access logs");
        }
        try {
            return pathService.exportAccessLogsToCSV(true);
        } catch (SQLException e) {
            System.err.println("❌ Error exporting to CSV: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String exportAccessLogsToHTML() {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can export access logs");
        }
        try {
            return pathService.exportAccessLogsToHTML(true);
        } catch (SQLException e) {
            System.err.println("❌ Error exporting to HTML: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}