package utils.commands;

import models.ContentNode;
import services.ContentNodeService;
import utils.Command;
import utils.FileUploadManager;

import java.sql.SQLException;

/**
 * CreateContentNodeCommand - Command to create a content node with undo support.
 * Undoing removes the node and deletes the associated PDF file if one exists.
 */
public class CreateContentNodeCommand implements Command {

    private ContentNodeService service;
    private ContentNode node;
    private int createdNodeId = -1;

    public CreateContentNodeCommand(ContentNodeService service, ContentNode node) {
        this.service = service;
        this.node = node;
    }

    @Override
    public void execute() {
        try {
            createdNodeId = service.createContentNode(node);
            if (createdNodeId > 0) {
                System.out.println("Content node created with ID: " + createdNodeId);
            } else {
                throw new SQLException("Failed to create content node");
            }
        } catch (SQLException e) {
            System.err.println("Error creating content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void undo() {
        try {
            if (createdNodeId > 0) {
                // Delete associated PDF file if exists
                if (node.getPdfPath() != null && !node.getPdfPath().isEmpty()) {
                    FileUploadManager.deleteUploadedFile(node.getPdfPath());
                }

                service.deleteContentNode(createdNodeId);
                System.out.println("Content node deleted (undo): " + createdNodeId);
            }
        } catch (SQLException e) {
            System.err.println("Error undoing create content node: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Create content: " + node.getTitle();
    }

    public int getCreatedNodeId() {
        return createdNodeId;
    }
}

