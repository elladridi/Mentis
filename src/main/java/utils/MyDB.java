package utils;
import java.sql.*;

public class MyDB {
    private final String URL = "jdbc:mysql://localhost:3306/PI_java";
    private final String USER = "root";
    private final String PWD = "";
    private Connection connection;
    private static MyDB instance;

    private MyDB() {
        try {
            connection = DriverManager.getConnection(URL, USER, PWD);
            System.out.println("Connexion établie !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null) instance = new MyDB();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}