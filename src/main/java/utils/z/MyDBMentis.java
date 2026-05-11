package utils.z;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
/**
 * Simple Singleton JDBC helper used by goal/mood/assessment services.
 *
 * This version is cleaned up and points to the same database as
 * {@link DatabaseConnectionMentis} so that all modules share one schema.
 *
 * Usage (unchanged API):
 *   Connection cnx = MyDBMentis.getInstance().getConnection();
 */
public class MyDBMentis {
    private static final String URL = AppConfig.dbUrl();
    private static final String USERNAME = AppConfig.dbUser();
    private static final String PASSWORD = AppConfig.dbPassword();
 
    private Connection connection;
    private static MyDBMentis instance;
 
    private MyDBMentis() {
        try {
            // Ensure driver is loaded (mainly for some older environments)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("MyDBMentis connected to: " + URL);
        } catch (ClassNotFoundException e) {
            System.err.println("MyDBMentis - MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("MyDBMentis - Connection error: " + e.getMessage());
        }
    }
 
    public static MyDBMentis getInstance() {
        if (instance == null) {
            synchronized (MyDBMentis.class) {
                if (instance == null) {
                    instance = new MyDBMentis();
                }
            }
        }
        return instance;
    }
 
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("MyDBMentis - Unable to reconnect: " + e.getMessage(), e);
        }
        return connection;
    }
}
