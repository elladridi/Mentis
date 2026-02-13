package utils;

import java.io.File;
import java.util.regex.Pattern;

/**
 * ContentValidator - Validates input for content node creation and editing.
 * Provides clear error messages for invalid inputs.
 */
public class ContentValidator {

    // Validation constraints
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 5000;
    private static final long MAX_PDF_SIZE = 50 * 1024 * 1024; // 50 MB
    private static final Pattern PDF_EXTENSION = Pattern.compile(".*\\.pdf$", Pattern.CASE_INSENSITIVE);

    /**
     * Validate title field
     * @return error message if invalid, empty string if valid
     */
    public static String validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Title is required and cannot be empty.";
        }

        int length = title.trim().length();
        if (length < MIN_TITLE_LENGTH) {
            return String.format("Title must be at least %d characters long.", MIN_TITLE_LENGTH);
        }

        if (length > MAX_TITLE_LENGTH) {
            return String.format("Title cannot exceed %d characters (current: %d).", MAX_TITLE_LENGTH, length);
        }

        return ""; // Valid
    }

    /**
     * Validate description field
     * @return error message if invalid, empty string if valid
     */
    public static String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "Description is required and cannot be empty.";
        }

        int length = description.trim().length();
        if (length < MIN_DESCRIPTION_LENGTH) {
            return String.format("Description must be at least %d characters long.", MIN_DESCRIPTION_LENGTH);
        }

        if (length > MAX_DESCRIPTION_LENGTH) {
            return String.format("Description cannot exceed %d characters (current: %d).", MAX_DESCRIPTION_LENGTH, length);
        }

        return ""; // Valid
    }

    /**
     * Validate PDF file
     * @return error message if invalid, empty string if valid
     */
    public static String validatePdfFile(File file) {
        if (file == null) {
            return "File is null.";
        }

        if (!file.exists()) {
            return "File does not exist: " + file.getAbsolutePath();
        }

        if (!file.isFile()) {
            return "Path is not a file: " + file.getAbsolutePath();
        }

        // Check file extension
        if (!PDF_EXTENSION.matcher(file.getName()).matches()) {
            return "File must be a PDF. Current file: " + file.getName();
        }

        // Check file size
        long fileSize = file.length();
        if (fileSize > MAX_PDF_SIZE) {
            return String.format("File size exceeds maximum limit of %d MB (current: %.2f MB).",
                    MAX_PDF_SIZE / (1024 * 1024),
                    (double) fileSize / (1024 * 1024));
        }

        // Check if file is readable
        if (!file.canRead()) {
            return "File is not readable: " + file.getAbsolutePath();
        }

        return ""; // Valid
    }

    /**
     * Validate all content node fields
     * @return error message if any field is invalid, empty string if all valid
     */
    public static String validateContentNode(String title, String description, File pdfFile) {
        // Validate title
        String titleError = validateTitle(title);
        if (!titleError.isEmpty()) {
            return titleError;
        }

        // Validate description
        String descError = validateDescription(description);
        if (!descError.isEmpty()) {
            return descError;
        }

        // Validate PDF if provided (optional)
        if (pdfFile != null) {
            String pdfError = validatePdfFile(pdfFile);
            if (!pdfError.isEmpty()) {
                return pdfError;
            }
        }

        return ""; // All valid
    }

    /**
     * Get formatted file size for display
     */
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Get maximum PDF size in MB for display
     */
    public static long getMaxPdfSizeInMB() {
        return MAX_PDF_SIZE / (1024 * 1024);
    }
}

