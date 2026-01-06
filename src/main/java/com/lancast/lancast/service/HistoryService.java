package com.lancast.lancast.service;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lancast.lancast.model.TransferLog;

/**
 * Service for logging file transfers using SQLite database.
 */
public class HistoryService {

    private static final String DB_URL = "jdbc:sqlite:lancast.db";
    private static boolean initialized = false;

    public HistoryService() {
        if (!initialized) {
            initializeDatabase();
            initialized = true;
        }
    }

    private void initializeDatabase() {
        String createTable = """
                    CREATE TABLE IF NOT EXISTS transfer_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        client_ip TEXT,
                        file_name TEXT,
                        device_type TEXT,
                        user_id INTEGER,
                        timestamp TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTable);
        } catch (SQLException e) {
            System.err.println("Error initializing transfer_logs: " + e.getMessage());
        }
    }

    public void logTransfer(String ip, String fileName, String deviceType, int userId) {
        String sql = "INSERT INTO transfer_logs(client_ip, file_name, device_type, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setString(2, fileName);
            pstmt.setString(3, deviceType);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error logging transfer: " + e.getMessage());
        }
    }

    public void logTransfer(String ip, String fileName, String deviceType) {
        logTransfer(ip, fileName, deviceType, AuthService.getCurrentUserId());
    }

    public List<TransferLog> getAllLogsForUser(int userId) {
        List<TransferLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM transfer_logs WHERE user_id = ? ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(new TransferLog(
                        rs.getInt("id"),
                        rs.getString("client_ip"),
                        rs.getString("file_name"),
                        rs.getString("device_type"),
                        rs.getInt("user_id"),
                        rs.getString("timestamp")));
            }

        } catch (SQLException e) {
            System.err.println("Error reading logs: " + e.getMessage());
        }

        return logs;
    }

    public List<TransferLog> getAllLogs() {
        if (AuthService.isLoggedIn()) {
            return getAllLogsForUser(AuthService.getCurrentUserId());
        }

        List<TransferLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM transfer_logs ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(new TransferLog(
                        rs.getInt("id"),
                        rs.getString("client_ip"),
                        rs.getString("file_name"),
                        rs.getString("device_type"),
                        rs.getInt("user_id"),
                        rs.getString("timestamp")));
            }

        } catch (SQLException e) {
            System.err.println("Error reading logs: " + e.getMessage());
        }

        return logs;
    }

    public void printAllLogs() {
        List<TransferLog> logs = getAllLogs();
        if (logs.isEmpty()) {
            System.out.println("No transfer logs found.");
        } else {
            logs.forEach(System.out::println);
        }
    }

    public void clearLogs() {
        String sql = "DELETE FROM transfer_logs";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
        }
    }

    public void exportToText(String file) {
        try (FileWriter fw = new FileWriter(file)) {
            for (TransferLog log : getAllLogs())
                fw.write(log + "\n");
            System.out.println("Exported to " + file);
        } catch (IOException e) {
            System.err.println("Error exporting to text: " + e.getMessage());
        }
    }

    public void exportToJSON(String file) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("[\n");
            List<TransferLog> logs = getAllLogs();
            for (int i = 0; i < logs.size(); i++) {
                TransferLog log = logs.get(i);
                fw.write("  {\n");
                fw.write("    \"id\": " + log.getId() + ",\n");
                fw.write("    \"client_ip\": \"" + log.getClientIp() + "\",\n");
                fw.write("    \"file_name\": \"" + log.getFileName() + "\",\n");
                fw.write("    \"device_type\": \"" + log.getDeviceType() + "\",\n");
                fw.write("    \"timestamp\": \"" + log.getTimestamp() + "\"\n");
                fw.write("  }" + (i < logs.size() - 1 ? "," : "") + "\n");
            }
            fw.write("]");
            System.out.println("Exported to " + file);

        } catch (IOException e) {
            System.err.println("Error exporting to JSON: " + e.getMessage());
        }
    }
}
