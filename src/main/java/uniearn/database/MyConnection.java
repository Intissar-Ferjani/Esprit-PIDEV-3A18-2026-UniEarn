package uniearn.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton class for managing database connection
 * Ensures only ONE database connection exists throughout the application
 */
public class MyConnection {

    // Database credentials
    private final String url = "jdbc:mysql://localhost:3306/uniearn_db";
    private final String login = "root";
    private final String pwd = "";

    // Single connection instance
    private Connection cnx;

    // Single MyConnection instance (Singleton)
    private static MyConnection instance;

    /**
     * Private constructor - prevents external instantiation
     * This is KEY to Singleton pattern
     */
    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the single instance of MyConnection
     * Thread-safe implementation
     */
    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    /**
     * Get the database connection
     * Always returns the same connection
     */
    public Connection getCnx() {
        return cnx;
    }

    /**
     * Check if connection is still alive
     */
    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Reconnect if connection is lost
     */
    public void reconnect() {
        try {
            if (!isConnected()) {
                cnx = DriverManager.getConnection(url, login, pwd);
                System.out.println("Reconnected to database successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Reconnection failed: " + e.getMessage());
        }
    }
}