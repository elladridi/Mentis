package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Simple Singleton JDBC helper used by goal/mood/assessment services.
 *
 * This version is cleaned up and points to the same database as
 * {@link DatabaseConnection} so that all modules share one schema.
 *
 * Usage (unchanged API):
 *   Connection cnx = MyDB.getInstance().getConnection();
 */
public class MyDB {

    // Use the same DB as DatabaseConnection: jdbc:mysql://localhost:3306/Mentis
    private static final String URL = "jdbc:mysql://localhost:3306/Mentis";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private Connection connection;
    private static MyDB instance;

    private MyDB() {
        try {
            // Ensure driver is loaded (mainly for some older environments)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("MyDB connected to: " + URL);
        } catch (ClassNotFoundException e) {
            System.err.println("MyDB - MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("MyDB - Connection error: " + e.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null) {
            synchronized (MyDB.class) {
                if (instance == null) {
                    instance = new MyDB();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}

