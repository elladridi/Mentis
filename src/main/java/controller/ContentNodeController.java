package controller;

import models.ContentNode;
import models.ContentPath;
import models.user;
import services.ContentNodeService;
import services.ContentPathService;
import services.userservice;
import utils.CommandHistory;
import utils.ContentValidator;
import utils.FileUploadManager;
import utils.commands.CreateContentNodeCommand;
import utils.commands.UpdateContentNodeCommand;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ContentNodeController - FIXED: Assigned users are properly saved!
 */
public class ContentNodeController {

    private ContentNodeService contentService;
    private ContentPathService pathService;
    private CommandHistory commandHistory;

    private int currentUserId = 0;
    private String currentUserRole = "";

    public ContentNodeController() {
        this.contentService = new ContentNodeService();
        this.pathService = new ContentPathService();
        this.commandHistory = new CommandHistory();
        FileUploadManager.ensureUploadDirectoryExists();
    }

    public void setCurrentUser(int userId, String userRole) {
        this.currentUserId = userId;
        this.currentUserRole = userRole != null ? userRole.toLowerCase() : "";
        System.out.println("✅ ContentNodeController: User set to ID=" + userId + ", Role=" + this.currentUserRole);
    }

    public int getCurrentUserId() { return currentUserId; }
    public String getCurrentUserRole() { return currentUserRole; }

    private boolean isAdminOrPsychologist() {
        return "admin".equals(currentUserRole) || "psychologist".equals(currentUserRole);
    }

    private boolean isAdmin() {
        return "admin".equals(currentUserRole);
    }

    private boolean canModifyNode(int nodeId) {
        if (isAdmin()) return true;
        if ("psychologist".equals(currentUserRole)) {
            try {
                ContentNode node = contentService.getContentNodeById(nodeId);
                return node != null && node.getCreatedBy() == currentUserId;
            } catch (SQLException e) {
                return false;
            }
        }
        return false;
    }

    // ==================== CONTENT NODE CRUD ====================

    /**
     * ⭐⭐⭐ FIXED: This method now CORRECTLY saves assigned users! ⭐⭐⭐
     */
    public int createContentNode(String title, String description, File pdfFile,
                                 Integer parentNodeId, List<Integer> assignedUserIds) {
        if (!isAdminOrPsychologist()) {
            throw new SecurityException("Access Denied: Only Admin or Psychologist can create content");
        }

        String validationError = ContentValidator.validateContentNode(title, description, pdfFile);
        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }

        try {
            String pdfPath = null;
            if (pdfFile != null) {
                pdfPath = FileUploadManager.uploadPdfFile(pdfFile, 0);
                if (pdfPath == null) {
                    throw new RuntimeException("Failed to upload PDF file");
                }
            }

            // Create node with current user as creator
            ContentNode node = new ContentNode(title, description, pdfPath, currentUserId, parentNodeId);

            // ⭐⭐⭐ CRITICAL FIX: Actually set the assigned users! ⭐⭐⭐
            if (assignedUserIds != null && !assignedUserIds.isEmpty()) {
                node.setAssignedUsersList(assignedUserIds);
                System.out.println("✅ Assigning users: " + assignedUserIds);
                System.out.println("✅ AssignedUsers JSON: " + node.getAssignedUsers());
            } else {
                System.out.println("⚠️ No users assigned to this content");
            }

            CreateContentNodeCommand command = new CreateContentNodeCommand(contentService, node);
            commandHistory.executeCommand(command);

            int newNodeId = command.getCreatedNodeId();
            System.out.println("✅ Content created with ID: " + newNodeId);
            return newNodeId;

        } catch (Exception e) {
            System.err.println("❌ Error creating content node: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int createContentNode(String title, String description, File pdfFile, Integer parentNodeId) {
        return createContentNode(title, description, pdfFile, parentNodeId, new ArrayList<>());
    }

    public void updateContentNode(int nodeId, String title, String description,
                                  File newPdfFile, Integer parentNodeId, List<Integer> assignedUserIds) {
        if (!canModifyNode(nodeId)) {
            throw new SecurityException("Access Denied: You don't have permission to modify this content");
        }

        String validationError = ContentValidator.validateContentNode(title, description, newPdfFile);
        if (!validationError.isEmpty()) {
            throw new IllegalArgumentException(validationError);
        }

        try {
            ContentNode oldNode = contentService.getContentNodeById(nodeId);
            if (oldNode == null) {
                throw new RuntimeException("Content node not found: " + nodeId);
            }

            ContentNode newNode = new ContentNode();
            newNode.setNodeId(nodeId);
            newNode.setTitle(title);
            newNode.setDescription(description);
            newNode.setCreatedBy(oldNode.getCreatedBy());
            newNode.setCreatedAt(oldNode.getCreatedAt());
            newNode.setParentNodeId(parentNodeId);

            // ⭐⭐⭐ FIX: Update assigned users ⭐⭐⭐
            if (assignedUserIds != null) {
                newNode.setAssignedUsersList(assignedUserIds);
                System.out.println("✅ Updating assigned users: " + assignedUserIds);
            } else {
                newNode.setAssignedUsers(oldNode.getAssignedUsers());
            }

            String newPdfPath = oldNode.getPdfPath();
            if (newPdfFile != null) {
                newPdfPath = FileUploadManager.uploadPdfFile(newPdfFile, nodeId);
                if (newPdfPath == null) {
                    throw new RuntimeException("Failed to upload PDF file");
                }
            }
            newNode.setPdfPath(newPdfPath);

            UpdateContentNodeCommand command = new UpdateContentNodeCommand(contentService, newNode, oldNode);
            commandHistory.executeCommand(command);

            System.out.println("✅ Content updated: " + nodeId);

        } catch (SQLException e) {
            System.err.println("❌ Error updating content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void updateContentNode(int nodeId, String title, String description,
                                  File newPdfFile, Integer parentNodeId) {
        updateContentNode(nodeId, title, description, newPdfFile, parentNodeId, null);
    }

    public void deleteContentNode(int nodeId) {
        if (!canModifyNode(nodeId)) {
            throw new SecurityException("Access Denied: You don't have permission to delete this content");
        }

        try {
            ContentNode node = contentService.getContentNodeById(nodeId);
            if (node == null) {
                throw new RuntimeException("Content node not found: " + nodeId);
            }

            deleteChildrenRecursive(nodeId);

            pathService.deleteAccessLogsForNode(nodeId);
            if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
                FileUploadManager.deleteUploadedFile(node.getPdfPath());
            }
            contentService.deleteContentNode(nodeId);

            System.out.println("✅ Content deleted: " + nodeId);

        } catch (SQLException e) {
            System.err.println("❌ Error deleting content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void deleteChildrenRecursive(int parentId) throws SQLException {
        List<ContentNode> children = contentService.getChildNodes(parentId);
        for (ContentNode child : children) {
            deleteChildrenRecursive(child.getNodeId());
            pathService.deleteAccessLogsForNode(child.getNodeId());
            if (child.getPdfPath() != null && !child.getPdfPath().isEmpty()) {
                FileUploadManager.deleteUploadedFile(child.getPdfPath());
            }
            contentService.deleteContentNode(child.getNodeId());
        }
    }

    public ContentNode getContentNode(int nodeId) {
        try {
            ContentNode node = contentService.getContentNodeById(nodeId);
            if (node != null && currentUserId > 0) {
                logAccess(currentUserId, nodeId);
            }
            return node;
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * ⭐⭐⭐ FIXED: Patient sees assigned content! ⭐⭐⭐
     */
    public List<ContentNode> getViewableContentNodes(Integer parentNodeId) {
        try {
            if (isAdminOrPsychologist()) {
                // Admin/Psychologist see ALL content
                if (parentNodeId == null) {
                    return contentService.getRootContentNodes();
                } else {
                    return contentService.getChildNodes(parentNodeId);
                }
            } else {
                // PATIENT: see content assigned to them
                List<ContentNode> viewable = new ArrayList<>();

                // ⭐⭐⭐ CRITICAL: Get assigned content ⭐⭐⭐
                List<ContentNode> assignedContent = contentService.getContentNodesAssignedToUser(currentUserId);
                if (assignedContent != null && !assignedContent.isEmpty()) {
                    viewable.addAll(assignedContent);
                    System.out.println("✅ PATIENT " + currentUserId + " sees " + assignedContent.size() + " assigned content items");
                    for (ContentNode node : assignedContent) {
                        System.out.println("   - " + node.getTitle() + " (ID: " + node.getNodeId() + ")");
                    }
                } else {
                    System.out.println("⚠️ PATIENT " + currentUserId + " has no assigned content");
                }

                // Also show content they created (if any)
                List<ContentNode> ownContent = contentService.getContentNodesByUser(currentUserId);
                if (ownContent != null) {
                    viewable.addAll(ownContent);
                }

                // Filter by parent if needed
                if (parentNodeId != null) {
                    viewable.removeIf(node -> !parentNodeId.equals(node.getParentNodeId()));
                } else {
                    viewable.removeIf(node -> node.getParentNodeId() != null);
                }

                // Remove duplicates
                return viewable.stream()
                        .distinct()
                        .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving content nodes: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<ContentNode> getRootContentNodes() {
        return getViewableContentNodes(null);
    }

    public List<ContentNode> getChildNodes(int parentNodeId) {
        return getViewableContentNodes(parentNodeId);
    }

    public List<ContentNode> getContentByUser(int userId) {
        if (isAdminOrPsychologist()) {
            try {
                return contentService.getContentNodesByUser(userId);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        if (userId != currentUserId) {
            throw new SecurityException("Access Denied: You can only view your own content");
        }

        try {
            return contentService.getContentNodesByUser(currentUserId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ContentNode> searchContent(String searchTerm) {
        try {
            List<ContentNode> results = contentService.searchByTitle(searchTerm);

            if (isAdminOrPsychologist()) {
                return results;
            } else {
                return results.stream()
                        .filter(node -> node.getCreatedBy() == currentUserId ||
                                node.isUserAssigned(currentUserId))
                        .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void logAccess(int userId, int nodeId) {
        if (userId <= 0 || nodeId <= 0) return;
        try {
            pathService.logAccess(userId, nodeId);
            System.out.println("✅ Access logged: User " + userId + " viewed Node " + nodeId);
        } catch (SQLException e) {
            System.err.println("❌ Error logging access: " + e.getMessage());
        }
    }

    public List<user> getAllPatients() {
        return userservice.getAllPatients();
    }

    public void openPdfFile(String pdfPath) {
        if (pdfPath == null || pdfPath.isEmpty()) {
            throw new RuntimeException("No PDF file associated with this content");
        }
        try {
            File pdfFile = new File(pdfPath);
            if (!pdfFile.exists()) {
                throw new RuntimeException("PDF file not found: " + pdfPath);
            }
            java.awt.Desktop.getDesktop().open(pdfFile);
            System.out.println("✅ Opened PDF: " + pdfPath);
        } catch (Exception e) {
            throw new RuntimeException("Could not open PDF file: " + e.getMessage());
        }
    }

    // ==================== CONTENT PATH CRUD METHODS ====================

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

    public List<ContentPath> getAccessLogsByNode(int nodeId) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view content access logs");
        }
        try {
            return pathService.getAccessLogsByNode(nodeId, true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting node access logs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<ContentPath> getAccessLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view access logs by date");
        }
        try {
            return pathService.getAccessLogsByDateRange(startDate, endDate, true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting access logs by date: " + e.getMessage());
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

    public void deleteAccessLogsByUser(int userId) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can delete user access logs");
        }
        try {
            pathService.deleteAccessLogsByUser(userId, true);
            System.out.println("✅ All access logs deleted for user: " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Error deleting user access logs: " + e.getMessage());
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

    public void clearAllAccessLogs() {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can clear all access logs");
        }
        try {
            pathService.clearAllAccessLogs(true);
            System.out.println("✅ ALL access logs cleared!");
        } catch (SQLException e) {
            System.err.println("❌ Error clearing access logs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public int getViewCountForNode(int nodeId) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }
        try {
            return pathService.getViewCountForNode(nodeId, true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting view count: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Object[]> getMostViewedContent(int limit) {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }
        try {
            return pathService.getMostViewedContent(limit, true);
        } catch (SQLException e) {
            System.err.println("❌ Error getting most viewed content: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

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

    // ==================== UNDO/REDO METHODS ====================

    public boolean undo() {
        boolean result = commandHistory.undo();
        System.out.println("↩️ Undo performed: " + result);
        return result;
    }

    public boolean redo() {
        boolean result = commandHistory.redo();
        System.out.println("↪️ Redo performed: " + result);
        return result;
    }

    public boolean canUndo() {
        return commandHistory.canUndo();
    }

    public boolean canRedo() {
        return commandHistory.canRedo();
    }

    public String getUndoDescription() {
        return commandHistory.getUndoDescription();
    }

    public String getRedoDescription() {
        return commandHistory.getRedoDescription();
    }

    public void clearHistory() {
        commandHistory.clear();
    }
    // ==================== CONTENT PATH STATISTICS & EXPORT METHODS ====================

    /**
     * Get ContentPathService instance (for export methods)
     */
    public ContentPathService getPathService() {
        return pathService;
    }

    /**
     * Get patient's own access history
     */
    public List<ContentPath> getMyAccessHistory() {
        try {
            return pathService.getAccessLogsByUser(currentUserId, currentUserId, isAdmin());
        } catch (SQLException e) {
            System.err.println("❌ Error getting access history: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get views over time for chart (last 30 days)
     */
    public Map<LocalDate, Long> getViewsOverTime() throws SQLException {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }

        List<ContentPath> logs = pathService.getAllAccessLogs(true);

        return logs.stream()
                .filter(log -> log.getAccessedAt() != null)
                .collect(Collectors.groupingBy(
                        log -> log.getAccessedAt().toLocalDate(),
                        Collectors.counting()
                ));
    }

    /**
     * Get unique users count
     */
    public long getUniqueUsersCount() throws SQLException {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }

        List<ContentPath> logs = pathService.getAllAccessLogs(true);
        return logs.stream().map(ContentPath::getUserId).distinct().count();
    }

    /**
     * Get views in last 30 days
     */
    public long getViewsLast30Days() throws SQLException {
        if (!isAdmin()) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }

        List<ContentPath> logs = pathService.getAllAccessLogs(true);
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        return logs.stream()
                .filter(log -> log.getAccessedAt() != null)
                .filter(log -> log.getAccessedAt().toLocalDate().isAfter(thirtyDaysAgo))
                .count();
    }
}