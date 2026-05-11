package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConfigManager - Flexible database configuration manager
 * 
 * Supports both MySQL and H2 (embedded) databases.
 * Automatically attempts MySQL connection first, then falls back to H2 if MySQL is unavailable.
 * 
 * This allows the application to:
 * 1. Run with MySQL when available (production/configured systems)
 * 2. Fall back to H2 embedded database for development/testing
 */
public class DatabaseConfigManager {

    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/mentis";
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASSWORD = "";

    private static final String H2_URL = "jdbc:h2:./mentis_db";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    private static DatabaseType activeDatabase = null;

    public enum DatabaseType {
        MYSQL("MySQL"),
        H2("H2 (Embedded)");

        private final String displayName;

        DatabaseType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Get a database connection - tries MySQL first, then falls back to H2
     * @return Connection object
     * @throws SQLException if both MySQL and H2 connections fail
     */
    public static Connection getConnection() throws SQLException {
        // Try MySQL first
        if (tryMySQLConnection()) {
            if (activeDatabase != DatabaseType.MYSQL) {
                activeDatabase = DatabaseType.MYSQL;
                System.out.println("✓ Connected to MySQL database");
            }
            return createMySQLConnection();
        }

        // Fall back to H2
        if (activeDatabase != DatabaseType.H2) {
            activeDatabase = DatabaseType.H2;
            System.out.println("⚠ MySQL unavailable - Using H2 embedded database");
            System.out.println("  For production, please configure MySQL");
        }
        return createH2Connection();
    }

    /**
     * Get the currently active database type
     * @return DatabaseType enum value
     */
    public static DatabaseType getActiveDatabaseType() {
        if (activeDatabase == null) {
            try {
                // Trigger database detection
                getConnection().close();
            } catch (SQLException e) {
                // Ignore - will use H2
            }
        }
        return activeDatabase != null ? activeDatabase : DatabaseType.H2;
    }

    /**
     * Test if MySQL is available
     * @return true if MySQL connection successful
     */
    private static boolean tryMySQLConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD).close();
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        }
    }

    /**
     * Create MySQL connection
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private static Connection createMySQLConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        }
    }

    /**
     * Create H2 connection (embedded)
     * @return Connection object
     * @throws SQLException if connection fails
     */
    private static Connection createH2Connection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
            Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
            
            // Initialize schema on first connection
            initializeH2Schema(conn);
            
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 JDBC Driver not found", e);
        }
    }

    /**
     * Initialize H2 database schema
     * @param conn Connection object
     */
    private static void initializeH2Schema(Connection conn) {
        try {
            // Create basic schema if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS mentis_user (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(255) UNIQUE, " +
                    "email VARCHAR(255), " +
                    "password VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            conn.createStatement().execute(sql);
            System.out.println("✓ H2 schema initialized");
        } catch (SQLException e) {
            System.err.println("Warning: Could not initialize H2 schema: " + e.getMessage());
        }
    }

    /**
     * Test database connection
     * @return true if connection successful
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("✓ Database test successful (" + getActiveDatabaseType().getDisplayName() + ")");
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("✗ Database test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close a connection safely
     * @param conn Connection to close
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
