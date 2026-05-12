package services;

import models.user;
import utils.z.DatabaseConnectionMentis;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

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

    private static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(13));
    }

    private static boolean verifyPassword(String plainPassword, String dbPassword) {
        if (dbPassword == null || dbPassword.isEmpty()) return false;

        try {
            // Symfony/PHP BCrypt uses $2y$
            // Java jBCrypt expects $2a$
            if (dbPassword.startsWith("$2y$")) {
                dbPassword = "$2a$" + dbPassword.substring(4);
            }

            return BCrypt.checkpw(plainPassword, dbPassword);

        } catch (Exception e) {
            System.err.println("❌ Password verification error: " + e.getMessage());
            return false;
        }
    }

    public static boolean registeruser(user user) {
        String sql = "INSERT INTO `user` (firstname, lastname, phone, dateofbirth, type, email, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstname());
            stmt.setString(2, user.getLastname());
            stmt.setString(3, user.getPhone());

            if (user.getDateofbirth() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(user.getDateofbirth()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }

            stmt.setString(5, user.getType());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, hashPassword(user.getPassword()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error registering user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static user loginuser(String email, String password) {
        String sql = "SELECT * FROM `user` WHERE email = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");

                if (verifyPassword(password, dbPassword)) {
                    LocalDate dob = null;
                    java.sql.Date sqlDate = rs.getDate("dateofbirth");

                    if (sqlDate != null) {
                        dob = sqlDate.toLocalDate();
                    }

                    return new user(
                            rs.getInt("id"),
                            rs.getString("firstname"),
                            rs.getString("lastname"),
                            rs.getString("phone"),
                            dob,
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

    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM `user` WHERE email = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("❌ Email check error: " + e.getMessage());
            return false;
        }
    }

    public static user getuserById(int id) {
        String sql = "SELECT * FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate dob = null;
                java.sql.Date sqlDate = rs.getDate("dateofbirth");

                if (sqlDate != null) {
                    dob = sqlDate.toLocalDate();
                }

                return new user(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        dob,
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

    public static user getuserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnectionMentis.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate dob = null;
                java.sql.Date sqlDate = rs.getDate("dateofbirth");

                if (sqlDate != null) {
                    dob = sqlDate.toLocalDate();
                }

                return new user(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        dob,
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

    public static boolean updateuser(user user) {
        String sql = "UPDATE `user` SET firstname=?, lastname=?, phone=?, dateofbirth=?, type=?, email=? WHERE id=?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFirstname());
            stmt.setString(2, user.getLastname());
            stmt.setString(3, user.getPhone());

            if (user.getDateofbirth() != null) {
                stmt.setDate(4, java.sql.Date.valueOf(user.getDateofbirth()));
            } else {
                stmt.setNull(4, java.sql.Types.DATE);
            }

            stmt.setString(5, user.getType());
            stmt.setString(6, user.getEmail());
            stmt.setInt(7, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Update error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteuser(int id) {
        String sql = "DELETE FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Delete error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserPassword(String email, String newPassword) {
        String sql = "UPDATE user SET password=? WHERE email=?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
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

    public static boolean isValidEmail(String email) {
        return email != null &&
                email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPhone(String phone) {
        return phone != null &&
                phone.matches("^[0-9+\\-\\s()]{8,20}$");
    }

    public static boolean isValidDate(LocalDate date) {
        return date != null;
    }

    public static boolean isValidDate(String date) {
        return date != null && date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    public static List<user> getAllPatients() {
        List<user> patients = new ArrayList<>();

        String sql = "SELECT * FROM `user` WHERE LOWER(type) = 'patient' ORDER BY firstname, lastname";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate dob = null;
                java.sql.Date sqlDate = rs.getDate("dateofbirth");

                if (sqlDate != null) {
                    dob = sqlDate.toLocalDate();
                }

                user u = new user(
                        rs.getInt("id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("phone"),
                        dob,
                        rs.getString("type"),
                        rs.getString("email"),
                        rs.getString("password")
                );

                patients.add(u);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting patients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    public static boolean saveFaceData(int userId, String facePath) {
        String sql = "UPDATE `user` SET face_data = ?, face_enabled = TRUE WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
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

    public static boolean hasFaceEnabled(int userId) {
        String sql = "SELECT face_enabled FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            return rs.next() && rs.getBoolean("face_enabled");

        } catch (SQLException e) {
            System.err.println("❌ Error checking face enabled: " + e.getMessage());
            return false;
        }
    }

    public static String getFaceDataPath(int userId) {
        String sql = "SELECT face_data FROM `user` WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
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

    public static boolean disableFaceID(int userId) {
        String sql = "UPDATE `user` SET face_enabled = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Error disabling face ID: " + e.getMessage());
            return false;
        }
    }

    public static boolean setFaceEnabled(int userId, boolean enabled) {
        String sql = "UPDATE `user` SET face_enabled = ? WHERE id = ?";

        try (Connection conn = DatabaseConnectionMentis.getConnection();
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