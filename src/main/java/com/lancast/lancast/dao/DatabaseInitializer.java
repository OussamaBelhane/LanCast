package com.lancast.lancast.dao;

import java.sql.*;

/**
 * Initializes the database tables.
 * Should be called once at application startup.
 */
public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:lancast.db";
    private static boolean initialized = false;

    /**
     * Initializes all database tables if they don't exist.
     * This method is idempotent and safe to call multiple times.
     */
    public static void initialize() {
        if (initialized) {
            return;
        }

        createUsersTable();
        createTransferLogsTable();
        initialized = true;
    }

    private static void createUsersTable() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password_hash TEXT NOT NULL,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        executeSQL(sql, "users");
    }

    private static void createTransferLogsTable() {
        String sql = """
                    CREATE TABLE IF NOT EXISTS transfer_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        client_ip TEXT,
                        file_name TEXT,
                        device_type TEXT,
                        user_id INTEGER,
                        timestamp TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        executeSQL(sql, "transfer_logs");
    }

    private static void executeSQL(String sql, String tableName) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error initializing " + tableName + " table: " + e.getMessage());
        }
    }
}
