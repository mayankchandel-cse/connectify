package com.connectify.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — single place that opens JDBC connections to MySQL.
 * Every DAO calls DBConnection.getConnection().
 *
 * Setup: put mysql-connector-j-8.0.33.jar on the classpath and
 * change PASS below to your own MySQL root password.
 */
public class DBConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/connectify?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASS = "root"; // TODO: change to your MySQL password

    static {
        try {
            // Load the MySQL JDBC driver (Connector/J 8.x)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found! Add mysql-connector-j-8.0.33.jar to the classpath.");
            e.printStackTrace();
        }
    }

    /** Open a new connection. Callers must close it (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Quietly close a connection (null-safe). Prefer try-with-resources instead. */
    public static void close(Connection con) {
        if (con != null) {
            try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /** Quick self-test: run `java com.connectify.util.DBConnection`. */
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            System.out.println("Connected to MySQL: " + con.getMetaData().getDatabaseProductName()
                    + " " + con.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
