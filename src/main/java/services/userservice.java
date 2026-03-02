package services;

import models.user;
import utils.DatabaseConnection;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class userservice {
    private static void closeResources(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Error closing resources: " + e.getMessage());
        }
    }

    public static boolean registeruser(user user) {

        String sql = "INSERT INTO `user` (firstname, lastname, phone, dateofbirth, type, email, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("DEBUG: Attempting to register user...");
            System.out.println("  Email: " + user.getEmail());

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getDateofbirth());
            stmt.setString(5, user.getType());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, hashPassword(user.getPassword()));

            int rows = stmt.executeUpdate();
            System.out.println("DEBUG: Rows affected: " + rows);

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error registering user: " + e.getMessage());
            return false;
        }
    }

    /* ===================== LOGIN ===================== */
    public static user loginuser(String email, String password) {

        String sql = "SELECT * FROM `user` WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");

                if (dbPassword.equals(hashPassword(password))) {
                    return new user(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("phone"),
                            rs.getString("dateofbirth"),
                            rs.getString("type"),
                            rs.getString("email"),
                            rs.getString("password")
                    );
                }
            }

            return null;

        } catch (SQLException e) {
            System.err.println("❌ Login error: " + e.getMessage());
            return null;
        }
    }

    /* ===================== EMAIL EXISTS ===================== */
    public static boolean emailExists(String email) {

        String sql = "SELECT COUNT(*) FROM `user` WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("❌ Email check error: " + e.getMessage());
            return false;
        }
    }

    /* ===================== GET BY ID ===================== */
    public static user getuserById(int id) {

        String sql = "SELECT * FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new user(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        rs.getString("dateofbirth"),
                        rs.getString("type"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }

            return null;

        } catch (SQLException e) {
            System.err.println("❌ Get user error: " + e.getMessage());
            return null;
        }
    }

    /* ===================== UPDATE ===================== */
    public static boolean updateuser(user user) {

        String sql = "UPDATE `user` SET firstname=?, lastname=?, phone=?, dateofbirth=?, type=?, email=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstName());
            stmt.setString(2, user.getLastName());
            stmt.setString(3, user.getPhone());
            stmt.setString(4, user.getDateofbirth());
            stmt.setString(5, user.getType());
            stmt.setString(6, user.getEmail());
            stmt.setInt(7, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Update error: " + e.getMessage());
            return false;
        }
    }

    /* ===================== DELETE ===================== */
    public static boolean deleteuser(int id) {

        String sql = "DELETE FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Delete error: " + e.getMessage());
            return false;
        }
    }

    /* ===================== PASSWORD HASH ===================== */
    private static String hashPassword(String password) {

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    /* ===================== VALIDATIONS ===================== */
    public static boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null &&
                phone.matches("^[0-9+\\-\\s()]{8,20}$");
    }

    public static boolean isValidDate(String date) {
        return date != null &&
                date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    public static user getuserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return new user(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        rs.getString("dateofbirth"),
                        rs.getString("type"),
                        rs.getString("email"),
                        rs.getString("password")
                );
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            return null;
        } finally {
            closeResources(conn, stmt, rs);
        }
    }

    public static boolean updateUserPassword(String email, String newPassword) {

        String sql = "UPDATE user SET password=? WHERE email=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hashPassword(newPassword));
            stmt.setString(2, email);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ Password updated for: " + email);
                return true;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("❌ Error updating password: " + e.getMessage());
            return false;
        }
    }

    public static List<user> getAllPatients() {
        List<user> patients = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE LOWER(type) = 'patient' ORDER BY firstname, lastname";

        System.out.println("🔍 Fetching patients from database...");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                // GET EACH FIELD INDIVIDUALLY TO VERIFY
                int id = rs.getInt("id");
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");
                String phone = rs.getString("phone");
                String dob = rs.getString("dateofbirth");
                String type = rs.getString("type");
                String email = rs.getString("email");
                String password = rs.getString("password");

                // DEBUG: Print what we got from database
                System.out.println("  - RAW DB RECORD: ID=" + id + ", Name=" + firstName + " " + lastName + ", Type=" + type);

                user u = new user(id, firstName, lastName, phone, dob, type, email, password);
                patients.add(u);
                count++;
            }

            System.out.println("✅ Total patients loaded: " + count);

        } catch (SQLException e) {
            System.err.println("❌ Error getting patients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }
    // Add these methods to services/userservice.java

    public static boolean saveFaceData(int userId, String facePath) {
        String sql = "UPDATE `user` SET face_data = ?, face_enabled = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, facePath);
            stmt.setInt(2, userId);

            int rows = stmt.executeUpdate();
            System.out.println("✅ Face data saved for user: " + userId + ", rows affected: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error saving face data: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Check if user has face ID enabled
     */
    public static boolean hasFaceEnabled(int userId) {
        String sql = "SELECT face_enabled FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getBoolean("face_enabled");

        } catch (SQLException e) {
            System.err.println("❌ Error checking face enabled: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get face data path for user
     */
    public static String getFaceDataPath(int userId) {
        String sql = "SELECT face_data FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("face_data");
            }
            return null;

        } catch (SQLException e) {
            System.err.println("❌ Error getting face data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Disable face ID for user
     */
    public static boolean disableFaceID(int userId) {
        String sql = "UPDATE `user` SET face_enabled = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error disabling face ID: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update face_enabled status without changing face_data
     */
    public static boolean setFaceEnabled(int userId, boolean enabled) {
        String sql = "UPDATE `user` SET face_enabled = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, enabled);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error updating face enabled status: " + e.getMessage());
            return false;
        }
    }

}