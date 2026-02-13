package utils;

/**
 * Command Interface - Implements the Command Pattern for Undo/Redo functionality.
 * All actions that can be undone must implement this interface.
 */
public interface Command {
    /**
     * Execute the command
     */
    void execute();

    /**
     * Undo the command (reverse operation)
     */
    void undo();

    /**
     * Get a description of the command for UI display
     */
    String getDescription();
}

