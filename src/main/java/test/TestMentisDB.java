package test;

import models.user;
import services.userservice;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestMentisDB {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║   MENTIS Database Test (mentis.user table)        ║");
        System.out.println("╚════════════════════════════════════════════════════╝\n");

        // Test 1: Database Connection
        testConnection();

        // Test 2: Check table exists
        checkTable();

        // Test 3: Show current users
        showUsers();

        // Test 4: Register a new user

        // Test 5: Show users again
        showUsers();

        // Test 6: Test login
        testLogin();

        System.out.println("\n╔════════════════════════════════════════════════════╗");
        System.out.println("║                  Tests Complete!                   ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }

    private static void testConnection() {
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("TEST 1: Database Connection");
        System.out.println("─────────────────────────────────────────────────────");

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connected to database: mentis");
                System.out.println("   URL: " + conn.getMetaData().getURL());
                conn.close();
            }
        } catch (Exception e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
        }
        System.out.println();
    }

    private static void checkTable() {
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("TEST 2: Check 'user' Table");
        System.out.println("─────────────────────────────────────────────────────");

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet tables = stmt.executeQuery("SHOW TABLES LIKE 'user'");
            if (tables.next()) {
                System.out.println("✅ Table 'user' exists");

                // Show structure
                System.out.println("\n   Table Structure:");
                ResultSet columns = stmt.executeQuery("DESCRIBE user");
                while (columns.next()) {
                    System.out.printf("   %-15s %-20s%n",
                            columns.getString("Field"),
                            columns.getString("Type"));
                }
            } else {
                System.out.println("❌ Table 'user' does NOT exist!");
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void showUsers() {
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("Current Users in Database");
        System.out.println("─────────────────────────────────────────────────────");

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM user");

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.printf("\nUser #%d:%n", count);
                System.out.println("   ID: " + rs.getInt("id"));
                System.out.println("   Name: " + rs.getString("firstname") + " " + rs.getString("lastname"));
                System.out.println("   Email: " + rs.getString("email"));
                System.out.println("   Type: " + rs.getString("type"));
            }

            if (count == 0) {
                System.out.println("📝 No users in database");
            } else {
                System.out.println("\n✅ Total users: " + count);
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        System.out.println();
    }

       private static void testLogin() {
        System.out.println("─────────────────────────────────────────────────────");
        System.out.println("TEST 4: Login Test");
        System.out.println("─────────────────────────────────────────────────────");

        try {
            Connection conn = DatabaseConnection.getConnection();
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM user ORDER BY id DESC LIMIT 1");

            if (rs.next()) {
                String email = rs.getString("email");
                System.out.println("Testing login with last registered user:");
                System.out.println("   Email: " + email);
                System.out.println();

                user loggedIn = userservice.loginuser(email, "secure123");

                if (loggedIn != null) {
                    System.out.println("✅ Login successful!");
                    System.out.println("   Welcome: " + loggedIn.getFirstname() + " " + loggedIn.getLastname());
                } else {
                    System.out.println("❌ Login failed!");
                }
            }

            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        System.out.println();
    }
}