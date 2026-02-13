package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FileUploadManager - Handles PDF file uploads and storage.
 * Manages file copying, path generation, and cleanup.
 */
public class FileUploadManager {

    private static final String UPLOAD_DIRECTORY = "uploads/content/";
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Create upload directory if it doesn't exist
     */
    public static boolean ensureUploadDirectoryExists() {
        try {
            Path path = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Created upload directory: " + UPLOAD_DIRECTORY);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save uploaded PDF file to storage
     * @param sourceFile Original PDF file
     * @param nodeId Content node ID (used in filename)
     * @return Path to stored file, or null if failed
     */
    public static String uploadPdfFile(File sourceFile, int nodeId) {
        if (!ensureUploadDirectoryExists()) {
            System.err.println("Cannot ensure upload directory exists");
            return null;
        }

        try {
            // Generate unique filename: nodeId_timestamp_originalName
            String timestamp = LocalDateTime.now().format(dateFormatter);
            String fileName = String.format("%d_%s_%s", nodeId, timestamp, sourceFile.getName());
            Path destinationPath = Paths.get(UPLOAD_DIRECTORY, fileName);

            // Copy file to destination
            Files.copy(sourceFile.toPath(), destinationPath);
            System.out.println("File uploaded successfully: " + destinationPath);

            return destinationPath.toString();
        } catch (IOException e) {
            System.err.println("Failed to upload file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Delete uploaded PDF file
     * @param filePath Path to file to delete
     * @return true if deleted, false otherwise
     */
    public static boolean deleteUploadedFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted: " + filePath);
                return true;
            } else {
                System.err.println("File not found: " + filePath);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if uploaded file exists
     */
    public static boolean fileExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * Get the upload directory path
     */
    public static String getUploadDirectory() {
        return UPLOAD_DIRECTORY;
    }

    /**
     * Ensure upload directory is clean (optional cleanup of old files)
     */
    public static void cleanupOldFiles(int dayThreshold) {
        try {
            Path directory = Paths.get(UPLOAD_DIRECTORY);
            if (!Files.exists(directory)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - (dayThreshold * 24 * 60 * 60 * 1000L);
            Files.list(directory)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toFile().lastModified() < cutoffTime)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Deleted old file: " + path);
                        } catch (IOException e) {
                            System.err.println("Failed to delete old file: " + path);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }
}

