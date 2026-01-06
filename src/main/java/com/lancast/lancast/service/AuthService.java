package com.lancast.lancast.service;

import com.lancast.lancast.dao.UserDAO;
import com.lancast.lancast.dao.UserDAOImpl;
import com.lancast.lancast.model.User;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Service for user authentication.
 * Uses UserDAO for data access operations.
 */
public class AuthService {

    private static final String SESSION_FILE = "session.properties";
    private static User currentUser = null;
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
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

        String normalizedUsername = username.trim().toLowerCase();
        String passwordHash = hashPassword(password);

        boolean created = userDAO.create(normalizedUsername, passwordHash);

        if (created) {
            User user = userDAO.findByUsername(normalizedUsername);
            if (user != null) {
                currentUser = user;
                System.out.println("User registered: " + username);
                return true;
            }
        }

        return false;
    }

    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        String normalizedUsername = username.trim().toLowerCase();
        String passwordHash = hashPassword(password);

        User user = userDAO.authenticate(normalizedUsername, passwordHash);

        if (user != null) {
            currentUser = user;
            System.out.println("User logged in: " + currentUser.getUsername());
            return true;
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

        User user = userDAO.findByUsername(username.trim().toLowerCase());

        if (user != null) {
            currentUser = user;
            System.out.println("Session restored for: " + currentUser.getUsername());
            return true;
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
