package services;

import models.user;
import utils.DatabaseConnection;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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


    /* ===================== REGISTER ===================== */
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
            return password; // dev fallback only
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
    // Add these methods to your userservice.java class

    /**
     * Get user by email
     * @param email User's email
     * @return user object if found, null otherwise
     */
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

}
