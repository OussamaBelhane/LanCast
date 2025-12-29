package com.lancast.lancast.service;

import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.lancast.lancast.model.TransferLog;

public class HistoryService {

    private static final String DB_URL = "jdbc:sqlite:lancast.db";

    public HistoryService() {
        // Table is created automatically
    }

    // save transfer to database
    public void logTransfer(String ip, String fileName, String deviceType) {
        String sql = "INSERT INTO transfer_logs(client_ip, file_name, device_type) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ip);
            pstmt.setString(2, fileName);
            pstmt.setString(3, deviceType);
            pstmt.executeUpdate();



        } catch (SQLException e) {
            System.err.println("Error logging transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
            System.err.println("Error reading logs: " + e.getMessage());
            e.printStackTrace();
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

    public void deleteLog(int id) {
        String sql = "DELETE FROM transfer_logs WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

            // Log deleted successfully

        } catch (SQLException e) {
            System.err.println("Error deleting log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void clearLogs() {
        String sql = "DELETE FROM transfer_logs";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            // All logs cleared successfully

        } catch (SQLException e) {
            System.err.println("Error clearing logs: " + e.getMessage());
            e.printStackTrace();
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
            System.err.println("Search error: " + e.getMessage());
            e.printStackTrace();
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

    public void exportToText(String file) {
        try (FileWriter fw = new FileWriter(file)) {
            for (TransferLog log : getAllLogs())
                fw.write(log + "\n");
            System.out.println("Exported to " + file);
        } catch (IOException e) {
            System.err.println("Error exporting to text: " + e.getMessage());
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}
