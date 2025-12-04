package com.lancast.lancast.database;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    private static final String DB_URL = "jdbc:sqlite:lancast.db";

    public HistoryManager() {
        createTable();
    }

    // Create table if not exists
    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS transfer_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "client_ip TEXT, " +
                "file_name TEXT, " +
                "device_type TEXT, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            stmt.execute(sql);

        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    // Insert a transfer record
    public void logTransfer(String ip, String fileName, String deviceType) {
        String sql = "INSERT INTO transfer_logs(client_ip, file_name, device_type) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setString(2, fileName);
            pstmt.setString(3, deviceType);
            pstmt.executeUpdate();

            System.out.println("Logged transfer: " + ip);

        } catch (SQLException e) {
            System.out.println("Error inserting log: " + e.getMessage());
        }
    }

    // Read all logs
    public List<TransferLog> getAllLogs() {
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
                        rs.getString("timestamp")));
            }

        } catch (SQLException e) {
            System.out.println("Error reading logs: " + e.getMessage());
        }

        return logs;
    }

    // Print all logs to console
    public void printAllLogs() {
        getAllLogs().forEach(System.out::println);
    }

    // Delete by ID
    public void deleteLog(int id) {
        String sql = "DELETE FROM transfer_logs WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            System.out.println("Deleted log id: " + id);

        } catch (SQLException e) {
            System.out.println("Error deleting log: " + e.getMessage());
        }
    }

    // Clear all logs
    public void clearLogs() {
        String sql = "DELETE FROM transfer_logs";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("All logs cleared.");

        } catch (SQLException e) {
            System.out.println("Error clearing logs: " + e.getMessage());
        }
    }

    // Search logs by IP, filename, device
    private List<TransferLog> search(String column, String value) {
        List<TransferLog> results = new ArrayList<>();
        String sql = "SELECT * FROM transfer_logs WHERE " + column + " LIKE ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + value + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new TransferLog(
                        rs.getInt("id"),
                        rs.getString("client_ip"),
                        rs.getString("file_name"),
                        rs.getString("device_type"),
                        rs.getString("timestamp")));
            }

        } catch (SQLException e) {
            System.out.println("Search error: " + e.getMessage());
        }
        return results;
    }

    public List<TransferLog> searchByIP(String ip) {
        return search("client_ip", ip);
    }

    public List<TransferLog> searchByFile(String file) {
        return search("file_name", file);
    }

    public List<TransferLog> searchByDevice(String device) {
        return search("device_type", device);
    }

    // Export to text
    public void exportToText(String file) {
        try (FileWriter fw = new FileWriter(file)) {
            for (TransferLog log : getAllLogs())
                fw.write(log + "\n");
            System.out.println("Exported to " + file);
        } catch (IOException e) {
            System.out.println("Error exporting text: " + e.getMessage());
        }
    }

    // Export to JSON
    public void exportToJSON(String file) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("[\n");
            List<TransferLog> logs = getAllLogs();
            for (int i = 0; i < logs.size(); i++) {
                TransferLog log = logs.get(i);
                fw.write("  {\n");
                fw.write("    \"id\": " + log.id + ",\n");
                fw.write("    \"client_ip\": \"" + log.clientIp + "\",\n");
                fw.write("    \"file_name\": \"" + log.fileName + "\",\n");
                fw.write("    \"device_type\": \"" + log.deviceType + "\",\n");
                fw.write("    \"timestamp\": \"" + log.timestamp + "\"\n");
                fw.write("  }" + (i < logs.size() - 1 ? "," : "") + "\n");
            }
            fw.write("]");
            System.out.println("Exported to " + file);

        } catch (IOException e) {
            System.out.println("Error exporting JSON: " + e.getMessage());
        }
    }

    // Test
    public static void main(String[] args) {
        HistoryManager db = new HistoryManager();

        db.logTransfer("192.168.1.10", "vacation.zip", "Android 13");
        db.logTransfer("192.168.1.55", "notes.pdf", "Windows 11");

        db.printAllLogs();
        System.out.println("Database test complete.");
    }
}
