package com.lancast.lancast.service;

import com.lancast.lancast.model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Properties;

/**
 * Service for user authentication using SQLite database.
 */
public class AuthService {

    private static final String DB_URL = "jdbc:sqlite:lancast.db";
    private static final String SESSION_FILE = "session.properties";
    private static User currentUser = null;

    public AuthService() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password_hash TEXT NOT NULL,
                        created_at TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
        } catch (SQLException e) {
            System.err.println("Error initializing users table: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public boolean signup(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO users(username, password_hash) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int userId = rs.getInt(1);
                currentUser = new User(userId, username.trim().toLowerCase(), null);
                System.out.println("User registered: " + username);
                return true;
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Username already exists: " + username);
            } else {
                System.err.println("Error registering user: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT id, username, created_at FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, hashPassword(password));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentUser = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("created_at"));
                System.out.println("User logged in: " + currentUser.getUsername());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
        }
        return false;
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("User logged out: " + currentUser.getUsername());
        }
        currentUser = null;
        clearSession();
    }

    public static void saveSession(String username) {
        try (FileOutputStream fos = new FileOutputStream(SESSION_FILE)) {
            Properties props = new Properties();
            props.setProperty("username", username);
            props.store(fos, "LanCast Session");
            System.out.println("Session saved for: " + username);
        } catch (IOException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }

    public static String loadSession() {
        File sessionFile = new File(SESSION_FILE);
        if (!sessionFile.exists()) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(sessionFile)) {
            Properties props = new Properties();
            props.load(fis);
            return props.getProperty("username");
        } catch (IOException e) {
            System.err.println("Error loading session: " + e.getMessage());
            return null;
        }
    }

    public static void clearSession() {
        File sessionFile = new File(SESSION_FILE);
        if (sessionFile.exists()) {
            sessionFile.delete();
            System.out.println("Session cleared");
        }
    }

    public boolean restoreSession() {
        String username = loadSession();
        if (username == null) {
            return false;
        }

        String sql = "SELECT id, username, created_at FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                currentUser = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("created_at"));
                System.out.println("Session restored for: " + currentUser.getUsername());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error restoring session: " + e.getMessage());
        }
        clearSession();
        return false;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }
}
