package utils.commands;

import models.ContentNode;
import services.ContentNodeService;
import utils.Command;
import utils.FileUploadManager;

import java.sql.SQLException;

/**
 * UpdateContentNodeCommand - Command to update a content node with undo support.
 * Stores the old state and can restore it on undo. Handles PDF file changes.
 */
public class UpdateContentNodeCommand implements Command {

    private ContentNodeService service;
    private ContentNode newNode;
    private ContentNode oldNode;  // Backup of original state

    public UpdateContentNodeCommand(ContentNodeService service, ContentNode newNode, ContentNode oldNode) {
        this.service = service;
        this.newNode = newNode;
        // Create a deep copy of the old node for backup
        this.oldNode = copyContentNode(oldNode);
    }

    @Override
    public void execute() {
        try {
            // If PDF changed, delete the old file
            if (oldNode.getPdfPath() != null && !oldNode.getPdfPath().isEmpty() &&
                (newNode.getPdfPath() == null || !newNode.getPdfPath().equals(oldNode.getPdfPath()))) {
                FileUploadManager.deleteUploadedFile(oldNode.getPdfPath());
            }

            service.updateContentNode(newNode);
            System.out.println("Content node updated: " + newNode.getNodeId());
        } catch (SQLException e) {
            System.err.println("Error updating content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void undo() {
        try {
            // If new PDF exists and is different from old, delete it
            if (newNode.getPdfPath() != null && !newNode.getPdfPath().isEmpty() &&
                (oldNode.getPdfPath() == null || !oldNode.getPdfPath().equals(newNode.getPdfPath()))) {
                FileUploadManager.deleteUploadedFile(newNode.getPdfPath());
            }

            service.updateContentNode(oldNode);
            System.out.println("Content node reverted (undo): " + oldNode.getNodeId());
        } catch (SQLException e) {
            System.err.println("Error undoing update content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Update content: " + newNode.getTitle();
    }

    private ContentNode copyContentNode(ContentNode node) {
        ContentNode copy = new ContentNode();
        copy.setNodeId(node.getNodeId());
        copy.setTitle(node.getTitle());
        copy.setDescription(node.getDescription());
        copy.setPdfPath(node.getPdfPath());
        copy.setCreatedAt(node.getCreatedAt());
        copy.setCreatedBy(node.getCreatedBy());
        copy.setParentNodeId(node.getParentNodeId());
        return copy;
    }
}

