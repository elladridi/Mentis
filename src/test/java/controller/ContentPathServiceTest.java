package controller;

import models.ContentNode;
import models.ContentPath;
import models.user;
import services.ContentNodeService;
import services.ContentPathService;
import services.userservice;
import utils.MyDB;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContentPathService
 * Tests access logging, retrieval, filtering, and deletion
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContentPathServiceTest {

    private static ContentPathService contentPathService;
    private static ContentNodeService contentNodeService;
    private static Connection connection;

    private static int testAdminId;
    private static int testPatientId;
    private static int testNodeId;
    private static int testPathId;

    @BeforeAll
    public static void setup() throws SQLException {
        System.out.println("🔧 Setting up ContentPathService tests...");

        connection = MyDB.getInstance().getConnection();
        contentPathService = new ContentPathService();
        contentNodeService = new ContentNodeService();

        // Create test admin user
        user testAdmin = new user(
                "Path", "TestAdmin", "111222333", "1980-01-01",
                "admin", "path.admin@mentis.com", "password123"
        );

        if (userservice.registeruser(testAdmin)) {
            testAdminId = userservice.getuserByEmail("path.admin@mentis.com").getId();
            System.out.println(" Created test admin user with ID: " + testAdminId);
        }

        // Create test patient user
        user testPatient = new user(
                "Path", "TestPatient", "444555666", "1990-01-01",
                "patient", "path.patient@mentis.com", "password123"
        );

        if (userservice.registeruser(testPatient)) {
            testPatientId = userservice.getuserByEmail("path.patient@mentis.com").getId();
            System.out.println(" Created test patient user with ID: " + testPatientId);
        }

        // Create test content node
        ContentNode node = new ContentNode(
                "Path Test Content",
                "Content for testing access logs",
                null,
                testAdminId,
                null
        );

        testNodeId = contentNodeService.createContentNode(node);
        System.out.println("Created test content node with ID: " + testNodeId);

        assertTrue(testAdminId > 0, "Test admin user should be created");
        assertTrue(testPatientId > 0, "Test patient user should be created");
        assertTrue(testNodeId > 0, "Test content node should be created");
    }

    @AfterAll
    public static void cleanup() throws SQLException {
        System.out.println("🧹 Cleaning up ContentPathService tests...");

        // Delete test content node
        if (testNodeId > 0) {
            try {
                contentNodeService.deleteContentNode(testNodeId);
                System.out.println("Deleted test node ID: " + testNodeId);
            } catch (SQLException e) {
                System.err.println(" Failed to delete test node: " + e.getMessage());
            }
        }

        // Delete test users
        if (testAdminId > 0) {
            userservice.deleteuser(testAdminId);
            System.out.println("✅ Deleted test admin user ID: " + testAdminId);
        }

        if (testPatientId > 0) {
            userservice.deleteuser(testPatientId);
            System.out.println("✅ Deleted test patient user ID: " + testPatientId);
        }
    }

    // ==================== TEST 1: LOG ACCESS ====================

    @Test
    @Order(1)
    public void testLogAccess() throws SQLException {
        System.out.println("\n📋 TEST 1: Log Access");

        contentPathService.logAccess(testPatientId, testNodeId);

        // Verify by getting logs for this user
        List<ContentPath> logs = contentPathService.getAccessLogsByUser(
                testPatientId, testPatientId, false
        );

        assertNotNull(logs, "Logs list should not be null");
        assertTrue(logs.size() > 0, "Should have at least 1 log entry");

        boolean found = logs.stream()
                .anyMatch(log -> log.getUserId() == testPatientId && log.getNodeId() == testNodeId);

        assertTrue(found, "Access log should exist for test patient and test node");

        // Store path ID for later tests
        if (!logs.isEmpty()) {
            testPathId = logs.get(0).getPathId();
            System.out.println("✅ Access logged with Path ID: " + testPathId);
        }

        System.out.println("✅ Access logged: User " + testPatientId + " viewed Node " + testNodeId);
    }

    // ==================== TEST 2: GET ACCESS LOGS BY USER ====================

    @Test
    @Order(2)
    public void testGetAccessLogsByUser() throws SQLException {
        System.out.println("\n📋 TEST 2: Get Access Logs By User");

        // Test patient viewing their own logs
        List<ContentPath> patientLogs = contentPathService.getAccessLogsByUser(
                testPatientId, testPatientId, false
        );

        assertNotNull(patientLogs, "Patient logs should not be null");
        assertTrue(patientLogs.size() > 0, "Patient should have at least 1 log");

        // Test admin viewing patient's logs
        List<ContentPath> adminViewLogs = contentPathService.getAccessLogsByUser(
                testPatientId, testAdminId, true
        );

        assertNotNull(adminViewLogs, "Admin view of patient logs should not be null");
        assertEquals(patientLogs.size(), adminViewLogs.size(), "Admin should see same logs as patient");

        System.out.println("✅ Retrieved " + patientLogs.size() + " logs for user " + testPatientId);

        // Test security - patient trying to view another user's logs
        assertThrows(SecurityException.class, () -> {
            contentPathService.getAccessLogsByUser(testAdminId, testPatientId, false);
        }, "Patient should not be able to view admin logs");

        System.out.println("✅ Security check passed - patient cannot view other users' logs");
    }

    // ==================== TEST 3: GET ALL ACCESS LOGS (ADMIN ONLY) ====================

    @Test
    @Order(3)
    public void testGetAllAccessLogs() throws SQLException {
        System.out.println("\n📋 TEST 3: Get All Access Logs (Admin Only)");

        // Admin should be able to get all logs
        List<ContentPath> allLogs = contentPathService.getAllAccessLogs(true);

        assertNotNull(allLogs, "All logs should not be null");
        assertTrue(allLogs.size() > 0, "Should have at least 1 log");

        System.out.println("✅ Admin retrieved " + allLogs.size() + " total logs");

        // Non-admin should NOT be able to get all logs
        assertThrows(SecurityException.class, () -> {
            contentPathService.getAllAccessLogs(false);
        }, "Non-admin should not be able to view all logs");

        System.out.println("✅ Security check passed - non-admin cannot view all logs");
    }

    // ==================== TEST 4: GET ACCESS LOGS BY NODE ====================

    @Test
    @Order(4)
    public void testGetAccessLogsByNode() throws SQLException {
        System.out.println("\n📋 TEST 4: Get Access Logs By Node");

        // Admin should be able to get logs by node
        List<ContentPath> nodeLogs = contentPathService.getAccessLogsByNode(testNodeId, true);

        assertNotNull(nodeLogs, "Node logs should not be null");

        boolean found = nodeLogs.stream()
                .anyMatch(log -> log.getUserId() == testPatientId);

        assertTrue(found, "Patient's access should be in node logs");

        System.out.println("✅ Retrieved " + nodeLogs.size() + " logs for node " + testNodeId);

        // Non-admin should NOT be able to get logs by node
        assertThrows(SecurityException.class, () -> {
            contentPathService.getAccessLogsByNode(testNodeId, false);
        }, "Non-admin should not be able to view logs by node");

        System.out.println("✅ Security check passed - non-admin cannot view node logs");
    }

    // ==================== TEST 5: GET ACCESS LOGS BY DATE RANGE ====================

    @Test
    @Order(5)
    public void testGetAccessLogsByDateRange() throws SQLException {
        System.out.println("\n📋 TEST 5: Get Access Logs By Date Range");

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate tomorrow = today.plusDays(1);

        // Admin should be able to filter by date range
        List<ContentPath> todayLogs = contentPathService.getAccessLogsByDateRange(
                yesterday, tomorrow, true
        );

        assertNotNull(todayLogs, "Date range logs should not be null");

        System.out.println("✅ Retrieved " + todayLogs.size() + " logs from " + yesterday + " to " + tomorrow);

        // Non-admin should NOT be able to filter by date range
        assertThrows(SecurityException.class, () -> {
            contentPathService.getAccessLogsByDateRange(yesterday, tomorrow, false);
        }, "Non-admin should not be able to filter logs by date");

        System.out.println("✅ Security check passed - non-admin cannot filter by date");
    }

    // ==================== TEST 6: GET VIEW COUNT FOR NODE ====================

    @Test
    @Order(6)
    public void testGetViewCountForNode() throws SQLException {
        System.out.println("\n📋 TEST 6: Get View Count For Node");

        int viewCount = contentPathService.getViewCountForNode(testNodeId, true);

        assertTrue(viewCount > 0, "View count should be at least 1");

        System.out.println("✅ Node " + testNodeId + " has been viewed " + viewCount + " times");

        // Non-admin should NOT be able to get view count
        assertThrows(SecurityException.class, () -> {
            contentPathService.getViewCountForNode(testNodeId, false);
        }, "Non-admin should not be able to view statistics");

        System.out.println("✅ Security check passed - non-admin cannot view statistics");
    }

    // ==================== TEST 7: GET MOST VIEWED CONTENT ====================

    @Test
    @Order(7)
    public void testGetMostViewedContent() throws SQLException {
        System.out.println("\n📋 TEST 7: Get Most Viewed Content");

        List<Object[]> mostViewed = contentPathService.getMostViewedContent(5, true);

        assertNotNull(mostViewed, "Most viewed list should not be null");

        System.out.println("✅ Top 5 most viewed content:");
        for (int i = 0; i < mostViewed.size(); i++) {
            Object[] row = mostViewed.get(i);
            System.out.println("   " + (i+1) + ". " + row[1] + " (ID: " + row[0] + ") - " + row[2] + " views");
        }

        // Non-admin should NOT be able to get most viewed
        assertThrows(SecurityException.class, () -> {
            contentPathService.getMostViewedContent(5, false);
        }, "Non-admin should not be able to view statistics");
    }

    // ==================== TEST 8: GET ACTIVE USERS COUNT ====================

    @Test
    @Order(8)
    public void testGetActiveUsersCount() throws SQLException {
        System.out.println("\n📋 TEST 8: Get Active Users Count");

        int activeUsers = contentPathService.getActiveUsersCount(true);

        assertTrue(activeUsers > 0, "Active users count should be at least 1");

        System.out.println("✅ Active users in last 30 days: " + activeUsers);
    }

    // ==================== TEST 9: DELETE ACCESS LOG ====================

    @Test
    @Order(9)
    public void testDeleteAccessLog() throws SQLException {
        System.out.println("\n📋 TEST 9: Delete Access Log");

        assertTrue(testPathId > 0, "Test path ID should be valid");

        // Admin should be able to delete logs
        contentPathService.deleteAccessLog(testPathId, true);

        // Verify deletion
        List<ContentPath> logs = contentPathService.getAccessLogsByUser(
                testPatientId, testAdminId, true
        );

        boolean found = logs.stream()
                .anyMatch(log -> log.getPathId() == testPathId);

        assertFalse(found, "Access log should be deleted");

        System.out.println("✅ Access log " + testPathId + " deleted successfully");

        // Non-admin should NOT be able to delete logs
        assertThrows(SecurityException.class, () -> {
            contentPathService.deleteAccessLog(testPathId, false);
        }, "Non-admin should not be able to delete logs");

        System.out.println("✅ Security check passed - non-admin cannot delete logs");
    }

    // ==================== TEST 10: DELETE OLD ACCESS LOGS ====================

    @Test
    @Order(10)
    public void testDeleteOldAccessLogs() throws SQLException {
        System.out.println("\n📋 TEST 10: Delete Old Access Logs");

        // Admin should be able to delete old logs
        int deleted = contentPathService.deleteOldAccessLogs(1, true);

        System.out.println("✅ Deleted " + deleted + " access logs older than 1 day");

        // Non-admin should NOT be able to delete old logs
        assertThrows(SecurityException.class, () -> {
            contentPathService.deleteOldAccessLogs(1, false);
        }, "Non-admin should not be able to delete old logs");

        System.out.println("✅ Security check passed - non-admin cannot delete old logs");
    }

    // ==================== TEST 11: EXPORT TO CSV ====================

    @Test
    @Order(11)
    public void testExportToCSV() throws SQLException {
        System.out.println("\n📋 TEST 11: Export Access Logs to CSV");

        // This test just verifies the export method doesn't throw exceptions
        // and returns a non-empty string when admin

        String csv = contentPathService.exportAccessLogsToCSV(true);

        assertNotNull(csv, "CSV export should not be null");
        assertTrue(csv.length() > 0, "CSV export should not be empty");
        assertTrue(csv.startsWith("Log ID,User ID,User Name"), "CSV should have correct headers");

        System.out.println("✅ CSV export successful, " + csv.split("\n").length + " lines");

        // Non-admin should NOT be able to export
        assertThrows(SecurityException.class, () -> {
            contentPathService.exportAccessLogsToCSV(false);
        }, "Non-admin should not be able to export logs");
    }

    // ==================== TEST 12: EXPORT TO HTML ====================

    @Test
    @Order(12)
    public void testExportToHTML() throws SQLException {
        System.out.println("\n📋 TEST 12: Export Access Logs to HTML");

        String html = contentPathService.exportAccessLogsToHTML(true);

        assertNotNull(html, "HTML export should not be null");
        assertTrue(html.length() > 0, "HTML export should not be empty");
        assertTrue(html.contains("<!DOCTYPE html>"), "HTML should have DOCTYPE");
        assertTrue(html.contains("<table>"), "HTML should contain a table");

        System.out.println("HTML export successful, " + html.length() + " characters");

        // Non-admin should NOT be able to export
        assertThrows(SecurityException.class, () -> {
            contentPathService.exportAccessLogsToHTML(false);
        }, "Non-admin should not be able to export logs");
    }
}