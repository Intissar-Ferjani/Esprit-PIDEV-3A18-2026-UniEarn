package uniearn.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class MyConnection {

    // Database credentials
    private final String url = "jdbc:mysql://localhost:3306/uniearn_db";
    private final String login = "root";
    private final String pwd = "";

    private Connection cnx;

    // Single MyConnection instance (Singleton)
    private static MyConnection instance;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connected to database successfully!");
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //get single instance of MyConnection -> Thread-safe
    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    //always returns the same connection, auto-reconnects if closed
    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                System.out.println("Connection lost — reconnecting...");
                cnx = DriverManager.getConnection(url, login, pwd);
                System.out.println("Reconnected to database successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Auto-reconnect failed: " + e.getMessage());
        }
        return cnx;
    }

    public boolean isConnected() {
        try {
            return cnx != null && !cnx.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // if cnx is lost
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