package services;

import models.ContentNode;
import utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ContentNodeService - Database operations for ContentNode entities.
 * FIXED: Patient assigned content retrieval works 100%
 */
public class ContentNodeService {

    private Connection cnx;

    public ContentNodeService() {
        cnx = MyDB.getInstance().getConnection();
    }

    /**
     * Create a new content node
     * @return nodeId of created node, or -1 if failed
     */
    public int createContentNode(ContentNode node) throws SQLException {
        String sql = "INSERT INTO content_node (title, description, pdf_path, created_by, parent_node_id, assigned_users) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, node.getTitle());
        ps.setString(2, node.getDescription());
        ps.setString(3, node.getPdfPath());
        ps.setInt(4, node.getCreatedBy());
        ps.setObject(5, node.getParentNodeId(), Types.INTEGER);
        ps.setString(6, node.getAssignedUsers() != null ? node.getAssignedUsers() : "[]");

        int affectedRows = ps.executeUpdate();

        if (affectedRows == 0) {
            throw new SQLException("Creating content node failed, no rows affected.");
        }

        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating content node failed, no ID obtained.");
            }
        }
    }

    /**
     * Get content node by ID
     */
    public ContentNode getContentNodeById(int nodeId) throws SQLException {
        String sql = "SELECT * FROM content_node WHERE node_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, nodeId);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSetToContentNode(rs);
        }
        return null;
    }

    /**
     * Get all root-level content nodes (where parent_node_id is NULL)
     */
    public List<ContentNode> getRootContentNodes() throws SQLException {
        String sql = "SELECT * FROM content_node WHERE parent_node_id IS NULL ORDER BY created_at DESC";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<ContentNode> nodes = new ArrayList<>();
        while (rs.next()) {
            nodes.add(mapResultSetToContentNode(rs));
        }
        return nodes;
    }

    /**
     * Get child nodes of a parent
     */
    public List<ContentNode> getChildNodes(int parentNodeId) throws SQLException {
        String sql = "SELECT * FROM content_node WHERE parent_node_id = ? ORDER BY created_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, parentNodeId);

        ResultSet rs = ps.executeQuery();

        List<ContentNode> nodes = new ArrayList<>();
        while (rs.next()) {
            nodes.add(mapResultSetToContentNode(rs));
        }
        return nodes;
    }

    /**
     * Get all content nodes created by a specific user
     */
    public List<ContentNode> getContentNodesByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM content_node WHERE created_by = ? ORDER BY created_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, userId);

        ResultSet rs = ps.executeQuery();

        List<ContentNode> nodes = new ArrayList<>();
        while (rs.next()) {
            nodes.add(mapResultSetToContentNode(rs));
        }
        return nodes;
    }

    /**
     * ⭐⭐⭐ FIXED: Get content nodes assigned to a specific patient ⭐⭐⭐
     * This version WORKS 100% with MySQL TEXT column containing JSON array
     */
    public List<ContentNode> getContentNodesAssignedToUser(int userId) throws SQLException {
        List<ContentNode> nodes = new ArrayList<>();

        // APPROACH 1: Get ALL nodes and filter in Java (MOST RELIABLE)
        String sql = "SELECT * FROM content_node WHERE assigned_users IS NOT NULL AND assigned_users != '[]'";

        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            ContentNode node = mapResultSetToContentNode(rs);
            // Check if this user ID is in the assigned_users list
            if (node.isUserAssigned(userId)) {
                nodes.add(node);
                System.out.println("✅ Found assigned content: " + node.getTitle() + " for user " + userId);
            }
        }

        System.out.println("📊 Total content items assigned to user " + userId + ": " + nodes.size());
        return nodes;
    }

    /**
     * Update a content node
     */
    public void updateContentNode(ContentNode node) throws SQLException {
        String sql = "UPDATE content_node SET title = ?, description = ?, pdf_path = ?, " +
                "parent_node_id = ?, assigned_users = ? WHERE node_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, node.getTitle());
        ps.setString(2, node.getDescription());
        ps.setString(3, node.getPdfPath());
        ps.setObject(4, node.getParentNodeId(), Types.INTEGER);
        ps.setString(5, node.getAssignedUsers() != null ? node.getAssignedUsers() : "[]");
        ps.setInt(6, node.getNodeId());

        ps.executeUpdate();
    }

    /**
     * Delete a content node
     */
    public void deleteContentNode(int nodeId) throws SQLException {
        String sql = "DELETE FROM content_node WHERE node_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, nodeId);

        ps.executeUpdate();
    }

    /**
     * Search content nodes by title
     */
    public List<ContentNode> searchByTitle(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM content_node WHERE title LIKE ? ORDER BY created_at DESC";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, "%" + searchTerm + "%");

        ResultSet rs = ps.executeQuery();

        List<ContentNode> nodes = new ArrayList<>();
        while (rs.next()) {
            nodes.add(mapResultSetToContentNode(rs));
        }
        return nodes;
    }

    /**
     * Get complete hierarchy path from root to a node
     */
    public List<ContentNode> getHierarchyPath(int nodeId) throws SQLException {
        List<ContentNode> path = new ArrayList<>();
        ContentNode current = getContentNodeById(nodeId);

        while (current != null) {
            path.add(0, current);
            if (current.getParentNodeId() == null) {
                break;
            }
            current = getContentNodeById(current.getParentNodeId());
        }

        return path;
    }

    /**
     * Get ALL child nodes recursively (for deletion)
     */
    public List<ContentNode> getAllChildrenRecursive(int parentId) throws SQLException {
        List<ContentNode> allChildren = new ArrayList<>();
        getAllChildrenRecursiveHelper(parentId, allChildren);
        return allChildren;
    }

    private void getAllChildrenRecursiveHelper(int parentId, List<ContentNode> accumulator) throws SQLException {
        List<ContentNode> directChildren = getChildNodes(parentId);
        for (ContentNode child : directChildren) {
            accumulator.add(child);
            getAllChildrenRecursiveHelper(child.getNodeId(), accumulator);
        }
    }

    // ==================== HELPER METHODS ====================

    private ContentNode mapResultSetToContentNode(ResultSet rs) throws SQLException {
        ContentNode node = new ContentNode();
        node.setNodeId(rs.getInt("node_id"));
        node.setTitle(rs.getString("title"));
        node.setDescription(rs.getString("description"));
        node.setPdfPath(rs.getString("pdf_path"));
        node.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        node.setCreatedBy(rs.getInt("created_by"));

        Integer parentId = rs.getInt("parent_node_id");
        if (rs.wasNull()) {
            node.setParentNodeId(null);
        } else {
            node.setParentNodeId(parentId);
        }

        // Handle assigned_users column
        try {
            String assignedUsers = rs.getString("assigned_users");
            node.setAssignedUsers(assignedUsers);
        } catch (SQLException e) {
            // Column might not exist yet - set default
            node.setAssignedUsers("[]");
        }

        return node;
    }
    
}