package services;

import models.ContentPath;
import utils.MyDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * ContentPathService - Database operations for ContentPath entities.
 * COMPLETE: Full CRUD with role-based access control
 */
public class ContentPathService {

    private Connection cnx;

    public ContentPathService() {
        cnx = MyDB.getInstance().getConnection();
    }

    // ==================== CREATE ====================

    /**
     * CREATE - Log user access to a content node (AUTOMATIC)
     */
    public void logAccess(int userId, int nodeId) throws SQLException {
        String sql = "INSERT INTO content_path (user_id, node_id, accessed_at) VALUES (?, ?, NOW())";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, nodeId);
        ps.executeUpdate();
    }

    /**
     * CREATE - Manual log entry (ADMIN ONLY)
     */
    public void createAccessLog(int userId, int nodeId, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can manually create access logs");
        }
        String sql = "INSERT INTO content_path (user_id, node_id, accessed_at) VALUES (?, ?, NOW())";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, nodeId);
        ps.executeUpdate();
    }

    // ==================== READ (SELECT) ====================

    /**
     * READ - Get access logs for a specific user
     * Users: only their own logs | Admin: any user's logs
     */
    public List<ContentPath> getAccessLogsByUser(int targetUserId, int requestingUserId, boolean isAdmin) throws SQLException {
        // Security check
        if (!isAdmin && targetUserId != requestingUserId) {
            throw new SecurityException("Access Denied: You can only view your own access logs");
        }

        String sql = "SELECT cp.*, " +
                "cn.title as content_title, " +
                "u.firstname, u.lastname, u.email " +
                "FROM content_path cp " +
                "LEFT JOIN content_node cn ON cp.node_id = cn.node_id " +
                "LEFT JOIN user u ON cp.user_id = u.id " +
                "WHERE cp.user_id = ? " +
                "ORDER BY cp.accessed_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, targetUserId);
        ResultSet rs = ps.executeQuery();

        List<ContentPath> paths = new ArrayList<>();
        while (rs.next()) {
            paths.add(mapResultSetToContentPathWithJoins(rs));
        }
        return paths;
    }

    /**
     * READ - Get access logs for a specific content node (ADMIN ONLY)
     */
    public List<ContentPath> getAccessLogsByNode(int nodeId, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can view access logs by content node");
        }

        String sql = "SELECT cp.*, " +
                "cn.title as content_title, " +
                "u.firstname, u.lastname, u.email " +
                "FROM content_path cp " +
                "LEFT JOIN content_node cn ON cp.node_id = cn.node_id " +
                "LEFT JOIN user u ON cp.user_id = u.id " +
                "WHERE cp.node_id = ? " +
                "ORDER BY cp.accessed_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, nodeId);
        ResultSet rs = ps.executeQuery();

        List<ContentPath> paths = new ArrayList<>();
        while (rs.next()) {
            paths.add(mapResultSetToContentPathWithJoins(rs));
        }
        return paths;
    }

    /**
     * READ - Get ALL access logs (ADMIN ONLY)
     */
    public List<ContentPath> getAllAccessLogs(boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Admin role required to view all access logs");
        }

        String sql = "SELECT cp.*, " +
                "cn.title as content_title, " +
                "u.firstname, u.lastname, u.email " +
                "FROM content_path cp " +
                "LEFT JOIN content_node cn ON cp.node_id = cn.node_id " +
                "LEFT JOIN user u ON cp.user_id = u.id " +
                "ORDER BY cp.accessed_at DESC " +
                "LIMIT 5000";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<ContentPath> paths = new ArrayList<>();
        while (rs.next()) {
            paths.add(mapResultSetToContentPathWithJoins(rs));
        }
        return paths;
    }

    /**
     * READ - Get access logs by date range (ADMIN ONLY)
     */
    public List<ContentPath> getAccessLogsByDateRange(LocalDate startDate, LocalDate endDate, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Admin role required");
        }

        String sql = "SELECT cp.*, " +
                "cn.title as content_title, " +
                "u.firstname, u.lastname, u.email " +
                "FROM content_path cp " +
                "LEFT JOIN content_node cn ON cp.node_id = cn.node_id " +
                "LEFT JOIN user u ON cp.user_id = u.id " +
                "WHERE DATE(cp.accessed_at) BETWEEN ? AND ? " +
                "ORDER BY cp.accessed_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, Date.valueOf(startDate));
        ps.setDate(2, Date.valueOf(endDate));
        ResultSet rs = ps.executeQuery();

        List<ContentPath> paths = new ArrayList<>();
        while (rs.next()) {
            paths.add(mapResultSetToContentPathWithJoins(rs));
        }
        return paths;
    }

    // ==================== DELETE ====================

    /**
     * DELETE - Delete a single access log by ID (ADMIN ONLY)
     */
    public void deleteAccessLog(int pathId, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can delete access logs");
        }

        String sql = "DELETE FROM content_path WHERE path_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, pathId);
        ps.executeUpdate();
    }

    /**
     * DELETE - Delete all logs for a specific user (ADMIN ONLY)
     */
    public void deleteAccessLogsByUser(int userId, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can delete user access logs");
        }

        String sql = "DELETE FROM content_path WHERE user_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    /**
     * DELETE - Delete all logs for a specific content node
     * Called automatically when content is deleted (no role check needed)
     */
    public void deleteAccessLogsForNode(int nodeId) throws SQLException {
        String sql = "DELETE FROM content_path WHERE node_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, nodeId);
        ps.executeUpdate();
    }

    /**
     * DELETE - Delete old access logs (ADMIN ONLY)
     */
    public int deleteOldAccessLogs(int daysOld, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can delete old access logs");
        }

        String sql = "DELETE FROM content_path WHERE accessed_at < DATE_SUB(NOW(), INTERVAL ? DAY)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, daysOld);
        return ps.executeUpdate();
    }

    /**
     * DELETE - Clear ALL access logs (ADMIN ONLY - DANGEROUS!)
     */
    public void clearAllAccessLogs(boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can clear all access logs");
        }

        String sql = "TRUNCATE TABLE content_path";
        Statement st = cnx.createStatement();
        st.executeUpdate(sql);
    }

    // ==================== STATISTICS ====================

    /**
     * STATS - Get total view count for content (ADMIN ONLY)
     */
    public int getViewCountForNode(int nodeId, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }

        String sql = "SELECT COUNT(*) as count FROM content_path WHERE node_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, nodeId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }

    /**
     * STATS - Get most viewed content (ADMIN ONLY)
     */
    public List<Object[]> getMostViewedContent(int limit, boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }

        String sql = "SELECT cn.node_id, cn.title, COUNT(cp.path_id) as view_count " +
                "FROM content_node cn " +
                "LEFT JOIN content_path cp ON cn.node_id = cp.node_id " +
                "GROUP BY cn.node_id, cn.title " +
                "ORDER BY view_count DESC " +
                "LIMIT ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();

        List<Object[]> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new Object[]{
                    rs.getInt("node_id"),
                    rs.getString("title"),
                    rs.getInt("view_count")
            });
        }
        return results;
    }

    /**
     * STATS - Get active users count (last 30 days) (ADMIN ONLY)
     */
    public int getActiveUsersCount(boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can view statistics");
        }

        String sql = "SELECT COUNT(DISTINCT user_id) as count FROM content_path " +
                "WHERE accessed_at > DATE_SUB(NOW(), INTERVAL 30 DAY)";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        if (rs.next()) {
            return rs.getInt("count");
        }
        return 0;
    }

    // ==================== HELPER METHODS ====================

    private ContentPath mapResultSetToContentPath(ResultSet rs) throws SQLException {
        ContentPath path = new ContentPath();
        path.setPathId(rs.getInt("path_id"));
        path.setUserId(rs.getInt("user_id"));
        path.setNodeId(rs.getInt("node_id"));
        path.setAccessedAt(rs.getTimestamp("accessed_at").toLocalDateTime());
        return path;
    }

    private ContentPath mapResultSetToContentPathWithJoins(ResultSet rs) throws SQLException {
        ContentPath path = mapResultSetToContentPath(rs);

        // Set JOIN fields
        try {
            path.setContentTitle(rs.getString("content_title"));
        } catch (SQLException e) {
            // Column doesn't exist in this query
        }

        try {
            path.setUserFirstName(rs.getString("firstname"));
            path.setUserLastName(rs.getString("lastname"));
            path.setUserEmail(rs.getString("email"));
        } catch (SQLException e) {
            // Column doesn't exist in this query
        }

        return path;
    }
    // ==================== EXPORT METHODS ====================

    /**
     * EXPORT - Export access logs to CSV format (ADMIN ONLY)
     */
    // ==================== EXPORT METHODS ====================

    /**
     * EXPORT - Export access logs to CSV format (ADMIN ONLY)
     */
    public String exportAccessLogsToCSV(boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can export access logs");
        }

        List<ContentPath> logs = getAllAccessLogs(true);

        StringBuilder csv = new StringBuilder();
        csv.append("Log ID,User ID,User Name,Content ID,Content Title,Accessed At\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (ContentPath log : logs) {
            csv.append(log.getPathId()).append(",");
            csv.append(log.getUserId()).append(",");
            csv.append("\"").append(log.getUserFullName().replace("\"", "\"\"")).append("\",");
            csv.append(log.getNodeId()).append(",");
            csv.append("\"").append(log.getContentTitle() != null ? log.getContentTitle().replace("\"", "\"\"") : "Unknown").append("\",");
            csv.append(log.getAccessedAt().format(formatter)).append("\n");
        }

        return csv.toString();
    }

    /**
     * EXPORT - Export access logs to HTML format (ADMIN ONLY)
     */
    public String exportAccessLogsToHTML(boolean isAdmin) throws SQLException {
        if (!isAdmin) {
            throw new SecurityException("Access Denied: Only Admin can export access logs");
        }

        List<ContentPath> logs = getAllAccessLogs(true);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<title>Mentis - Content Access Logs Export</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 30px; background-color: #f5f5f5; }\n");
        html.append(".container { background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("h1 { color: #588b71; border-bottom: 2px solid #588b71; padding-bottom: 10px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n");
        html.append("th { background-color: #588b71; color: white; padding: 12px; text-align: left; }\n");
        html.append("td { padding: 10px; border-bottom: 1px solid #ddd; }\n");
        html.append("tr:hover { background-color: #f5f5f5; }\n");
        html.append(".footer { margin-top: 30px; color: #666; font-size: 12px; text-align: center; }\n");
        html.append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("<div class='container'>\n");
        html.append("<h1>🧠 Mentis - Content Access Logs</h1>\n");
        html.append("<p>Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</p>\n");
        html.append("<p>Total Logs: ").append(logs.size()).append("</p>\n");
        html.append("<table>\n");
        html.append("<tr><th>Log ID</th><th>User</th><th>Content</th><th>Accessed At</th></tr>\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (ContentPath log : logs) {
            html.append("<tr>");
            html.append("<td>").append(log.getPathId()).append("</td>");
            html.append("<td>").append(log.getUserFullName()).append(" (ID: ").append(log.getUserId()).append(")</td>");
            html.append("<td>").append(log.getContentTitle() != null ? log.getContentTitle() : "Content " + log.getNodeId()).append("</td>");
            html.append("<td>").append(log.getAccessedAt().format(formatter)).append("</td>");
            html.append("</tr>\n");
        }

        html.append("</table>\n");
        html.append("<div class='footer'>Mentis - Mental Health Companion</div>\n");
        html.append("</div>\n");
        html.append("</body>\n</html>");

        return html.toString();
    }
}