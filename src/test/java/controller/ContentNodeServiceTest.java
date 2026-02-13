package controller;

import models.ContentNode;
import models.user;
import services.ContentNodeService;
import services.userservice;
import utils.MyDB;

import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ContentNodeService
 * Tests CRUD operations, hierarchy, and patient assignment
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContentNodeServiceTest {

    private static ContentNodeService contentNodeService;
    private static Connection connection;
    private static int testUserId;
    private static int testPatientId;
    private static int testNodeId;

    @BeforeAll
    public static void setup() throws SQLException {
        System.out.println("🔧 Setting up ContentNodeService tests...");

        // Get database connection
        connection = MyDB.getInstance().getConnection();
        contentNodeService = new ContentNodeService();

        // Create test admin user
        user testUser = new user(
                "Test", "Admin", "123456789", "1990-01-01",
                "admin", "test.admin@mentis.com", "password123"
        );

        if (userservice.registeruser(testUser)) {
            testUserId = userservice.getuserByEmail("test.admin@mentis.com").getId();
            System.out.println("✅ Created test admin user with ID: " + testUserId);
        }

        // Create test patient user
        user testPatient = new user(
                "Test", "Patient", "987654321", "1995-01-01",
                "patient", "test.patient@mentis.com", "password123"
        );

        if (userservice.registeruser(testPatient)) {
            testPatientId = userservice.getuserByEmail("test.patient@mentis.com").getId();
            System.out.println("✅ Created test patient user with ID: " + testPatientId);
        }

        assertTrue(testUserId > 0, "Test admin user should be created");
        assertTrue(testPatientId > 0, "Test patient user should be created");
    }

    @AfterAll
    public static void cleanup() throws SQLException {
        System.out.println("🧹 Cleaning up ContentNodeService tests...");

        // Delete test content nodes
        if (testNodeId > 0) {
            try {
                contentNodeService.deleteContentNode(testNodeId);
                System.out.println("✅ Deleted test node ID: " + testNodeId);
            } catch (SQLException e) {
                System.err.println("❌ Failed to delete test node: " + e.getMessage());
            }
        }

        // Delete test users
        if (testUserId > 0) {
            userservice.deleteuser(testUserId);
            System.out.println("✅ Deleted test admin user ID: " + testUserId);
        }

        if (testPatientId > 0) {
            userservice.deleteuser(testPatientId);
            System.out.println("✅ Deleted test patient user ID: " + testPatientId);
        }
    }

    // ==================== TEST 1: CREATE CONTENT NODE ====================

    @Test
    @Order(1)
    public void testCreateContentNode() throws SQLException {
        System.out.println("\n📋 TEST 1: Create Content Node");

        ContentNode node = new ContentNode(
                "Test Content Title",
                "This is a test content description for unit testing",
                null, // No PDF
                testUserId,
                null  // Root node
        );

        testNodeId = contentNodeService.createContentNode(node);

        assertTrue(testNodeId > 0, "Content node should be created with positive ID");
        System.out.println("✅ Content node created with ID: " + testNodeId);
    }

    // ==================== TEST 2: GET CONTENT NODE BY ID ====================

    @Test
    @Order(2)
    public void testGetContentNodeById() throws SQLException {
        System.out.println("\n📋 TEST 2: Get Content Node By ID");

        assertTrue(testNodeId > 0, "Test node ID should be valid");

        ContentNode node = contentNodeService.getContentNodeById(testNodeId);

        assertNotNull(node, "Content node should not be null");
        assertEquals(testNodeId, node.getNodeId(), "Node ID should match");
        assertEquals("Test Content Title", node.getTitle(), "Title should match");
        assertEquals("This is a test content description for unit testing", node.getDescription(), "Description should match");
        assertEquals(testUserId, node.getCreatedBy(), "Created by should match");
        assertNull(node.getParentNodeId(), "Parent node ID should be null for root node");

        System.out.println("✅ Retrieved content node: " + node.getTitle());
    }

    // ==================== TEST 3: UPDATE CONTENT NODE ====================

    @Test
    @Order(3)
    public void testUpdateContentNode() throws SQLException {
        System.out.println("\n📋 TEST 3: Update Content Node");

        assertTrue(testNodeId > 0, "Test node ID should be valid");

        ContentNode node = contentNodeService.getContentNodeById(testNodeId);
        node.setTitle("Updated Test Title");
        node.setDescription("This description has been updated");

        contentNodeService.updateContentNode(node);

        ContentNode updatedNode = contentNodeService.getContentNodeById(testNodeId);

        assertEquals("Updated Test Title", updatedNode.getTitle(), "Title should be updated");
        assertEquals("This description has been updated", updatedNode.getDescription(), "Description should be updated");

        System.out.println("✅ Content node updated successfully");
    }

    // ==================== TEST 4: ASSIGN PATIENTS TO CONTENT ====================

    @Test
    @Order(4)
    public void testAssignPatientsToContent() throws SQLException {
        System.out.println("\n📋 TEST 4: Assign Patients to Content");

        assertTrue(testNodeId > 0, "Test node ID should be valid");
        assertTrue(testPatientId > 0, "Test patient ID should be valid");

        ContentNode node = contentNodeService.getContentNodeById(testNodeId);

        // Assign test patient
        node.assignUser(testPatientId);
        contentNodeService.updateContentNode(node);

        ContentNode updatedNode = contentNodeService.getContentNodeById(testNodeId);

        assertTrue(updatedNode.isUserAssigned(testPatientId), "Patient should be assigned to content");
        assertEquals(1, updatedNode.getAssignedUsersList().size(), "Should have 1 assigned patient");
        assertEquals(testPatientId, updatedNode.getAssignedUsersList().get(0), "Assigned patient ID should match");

        System.out.println("✅ Patient " + testPatientId + " assigned to content " + testNodeId);
        System.out.println("   AssignedUsers JSON: " + updatedNode.getAssignedUsers());
    }

    // ==================== TEST 5: GET CONTENT ASSIGNED TO PATIENT ====================

    @Test
    @Order(5)
    public void testGetContentAssignedToPatient() throws SQLException {
        System.out.println("\n📋 TEST 5: Get Content Assigned to Patient");

        assertTrue(testPatientId > 0, "Test patient ID should be valid");

        List<ContentNode> assignedContent = contentNodeService.getContentNodesAssignedToUser(testPatientId);

        assertNotNull(assignedContent, "Assigned content list should not be null");
        assertTrue(assignedContent.size() > 0, "Patient should have at least 1 assigned content");

        boolean found = assignedContent.stream()
                .anyMatch(node -> node.getNodeId() == testNodeId);

        assertTrue(found, "Test content should be in assigned content list");

        System.out.println("✅ Patient " + testPatientId + " has " + assignedContent.size() + " assigned content items");
    }

    // ==================== TEST 6: CREATE CHILD NODE ====================

    @Test
    @Order(6)
    public void testCreateChildNode() throws SQLException {
        System.out.println("\n📋 TEST 6: Create Child Node");

        assertTrue(testNodeId > 0, "Parent node ID should be valid");

        ContentNode childNode = new ContentNode(
                "Test Child Content",
                "This is a child content node",
                null,
                testUserId,
                testNodeId // Parent ID
        );

        int childNodeId = contentNodeService.createContentNode(childNode);

        assertTrue(childNodeId > 0, "Child node should be created with positive ID");

        // Verify parent-child relationship
        List<ContentNode> children = contentNodeService.getChildNodes(testNodeId);

        assertTrue(children.size() > 0, "Parent should have at least 1 child");

        boolean found = children.stream()
                .anyMatch(node -> node.getNodeId() == childNodeId);

        assertTrue(found, "Child node should be in parent's children list");

        System.out.println("✅ Child node created with ID: " + childNodeId);

        // Clean up child node
        contentNodeService.deleteContentNode(childNodeId);
        System.out.println("✅ Deleted child node: " + childNodeId);
    }

    // ==================== TEST 7: SEARCH CONTENT BY TITLE ====================

    @Test
    @Order(7)
    public void testSearchContentByTitle() throws SQLException {
        System.out.println("\n📋 TEST 7: Search Content By Title");

        List<ContentNode> results = contentNodeService.searchByTitle("Updated Test");

        assertNotNull(results, "Search results should not be null");
        assertTrue(results.size() > 0, "Should find at least 1 content item");

        boolean found = results.stream()
                .anyMatch(node -> node.getNodeId() == testNodeId);

        assertTrue(found, "Test content should be in search results");

        System.out.println("✅ Found " + results.size() + " content items matching 'Updated Test'");
    }

    // ==================== TEST 8: GET ROOT CONTENT NODES ====================

    @Test
    @Order(8)
    public void testGetRootContentNodes() throws SQLException {
        System.out.println("\n📋 TEST 8: Get Root Content Nodes");

        List<ContentNode> rootNodes = contentNodeService.getRootContentNodes();

        assertNotNull(rootNodes, "Root nodes list should not be null");

        boolean found = rootNodes.stream()
                .anyMatch(node -> node.getNodeId() == testNodeId);

        assertTrue(found, "Test content should be in root nodes list");

        System.out.println("✅ Found " + rootNodes.size() + " root content nodes");
    }

    // ==================== TEST 9: UNASSIGN PATIENT ====================

    @Test
    @Order(9)
    public void testUnassignPatient() throws SQLException {
        System.out.println("\n📋 TEST 9: Unassign Patient from Content");

        assertTrue(testNodeId > 0, "Test node ID should be valid");
        assertTrue(testPatientId > 0, "Test patient ID should be valid");

        ContentNode node = contentNodeService.getContentNodeById(testNodeId);

        // Remove patient assignment
        node.unassignUser(testPatientId);
        contentNodeService.updateContentNode(node);

        ContentNode updatedNode = contentNodeService.getContentNodeById(testNodeId);

        assertFalse(updatedNode.isUserAssigned(testPatientId), "Patient should NOT be assigned to content");
        assertEquals(0, updatedNode.getAssignedUsersList().size(), "Should have 0 assigned patients");

        System.out.println("✅ Patient " + testPatientId + " unassigned from content " + testNodeId);
    }

    // ==================== TEST 10: DELETE CONTENT NODE ====================

    @Test
    @Order(10)
    public void testDeleteContentNode() throws SQLException {
        System.out.println("\n📋 TEST 10: Delete Content Node");

        assertTrue(testNodeId > 0, "Test node ID should be valid");

        contentNodeService.deleteContentNode(testNodeId);

        ContentNode deletedNode = contentNodeService.getContentNodeById(testNodeId);

        assertNull(deletedNode, "Content node should be deleted");

        System.out.println("✅ Content node " + testNodeId + " deleted successfully");

        // Reset testNodeId since it's deleted
        testNodeId = 0;
    }
}