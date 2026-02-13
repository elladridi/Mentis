package utils;

import java.util.Stack;

/**
 * CommandHistory Manager - Manages undo/redo history using Command Pattern.
 * Maintains stacks for both executed (undo) and undone (redo) commands.
 */
public class CommandHistory {
    private Stack<Command> undoStack;
    private Stack<Command> redoStack;
    private int maxHistorySize = 50;  // Limit history to prevent memory issues

    public CommandHistory() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    /**
     * Execute a command and add it to undo history
     */
    public void executeCommand(Command command) {
        try {
            command.execute();
            undoStack.push(command);
            // Clear redo stack when new command is executed
            redoStack.clear();

            // Maintain max history size
            if (undoStack.size() > maxHistorySize) {
                undoStack.remove(0);
            }
        } catch (Exception e) {
            System.err.println("Error executing command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Undo the last command
     */
    public boolean undo() {
        if (!undoStack.isEmpty()) {
            try {
                Command command = undoStack.pop();
                command.undo();
                redoStack.push(command);
                return true;
            } catch (Exception e) {
                System.err.println("Error undoing command: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Redo the last undone command
     */
    public boolean redo() {
        if (!redoStack.isEmpty()) {
            try {
                Command command = redoStack.pop();
                command.execute();
                undoStack.push(command);
                return true;
            } catch (Exception e) {
                System.err.println("Error redoing command: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    /**
     * Check if undo is available
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Check if redo is available
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Get description of last undoable command
     */
    public String getUndoDescription() {
        if (!undoStack.isEmpty()) {
            return "Undo: " + undoStack.peek().getDescription();
        }
        return "Nothing to undo";
    }

    /**
     * Get description of last redoable command
     */
    public String getRedoDescription() {
        if (!redoStack.isEmpty()) {
            return "Redo: " + redoStack.peek().getDescription();
        }
        return "Nothing to redo";
    }

    /**
     * Clear all history
     */
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public int getMaxHistorySize() {
        return maxHistorySize;
    }

    public void setMaxHistorySize(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }
}

