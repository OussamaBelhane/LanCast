package com.lancast.lancast.dao;

import com.lancast.lancast.model.TransferLog;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of TransferLogDAO.
 * Handles all database operations for transfer logs.
 */
public class TransferLogDAOImpl implements TransferLogDAO {

    private static final String DB_URL = "jdbc:sqlite:lancast.db";
    private static boolean initialized = false;

    public TransferLogDAOImpl() {
        if (!initialized) {
            initializeTable();
            initialized = true;
        }
    }

    private void initializeTable() {
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
            System.err.println("Error initializing transfer_logs table: " + e.getMessage());
        }
    }

    @Override
    public void create(String clientIp, String fileName, String deviceType, int userId) {
        String sql = "INSERT INTO transfer_logs(client_ip, file_name, device_type, user_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, clientIp);
            pstmt.setString(2, fileName);
            pstmt.setString(3, deviceType);
            pstmt.setInt(4, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error creating transfer log: " + e.getMessage());
        }
    }

    @Override
    public List<TransferLog> findByUserId(int userId) {
        List<TransferLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM transfer_logs WHERE user_id = ? ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToTransferLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding logs by user ID: " + e.getMessage());
        }

        return logs;
    }

    @Override
    public List<TransferLog> findAll() {
        List<TransferLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM transfer_logs ORDER BY id DESC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToTransferLog(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error finding all logs: " + e.getMessage());
        }

        return logs;
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM transfer_logs";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error deleting all logs: " + e.getMessage());
        }
    }

    @Override
    public void deleteByUserId(int userId) {
        String sql = "DELETE FROM transfer_logs WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting logs by user ID: " + e.getMessage());
        }
    }

    private TransferLog mapResultSetToTransferLog(ResultSet rs) throws SQLException {
        return new TransferLog(
                rs.getInt("id"),
                rs.getString("client_ip"),
                rs.getString("file_name"),
                rs.getString("device_type"),
                rs.getInt("user_id"),
                rs.getString("timestamp"));
    }
}
