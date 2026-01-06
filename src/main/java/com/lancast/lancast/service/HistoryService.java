package com.lancast.lancast.service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.lancast.lancast.dao.TransferLogDAO;
import com.lancast.lancast.dao.TransferLogDAOImpl;
import com.lancast.lancast.model.TransferLog;

/**
 * Service for managing file transfer history.
 * Uses TransferLogDAO for data access operations.
 */
public class HistoryService {

    private final TransferLogDAO transferLogDAO;

    public HistoryService() {
        this.transferLogDAO = new TransferLogDAOImpl();
    }

    public void logTransfer(String ip, String fileName, String deviceType, int userId) {
        transferLogDAO.create(ip, fileName, deviceType, userId);
    }

    public void logTransfer(String ip, String fileName, String deviceType) {
        logTransfer(ip, fileName, deviceType, AuthService.getCurrentUserId());
    }

    public List<TransferLog> getAllLogsForUser(int userId) {
        return transferLogDAO.findByUserId(userId);
    }

    public List<TransferLog> getAllLogs() {
        if (AuthService.isLoggedIn()) {
            return getAllLogsForUser(AuthService.getCurrentUserId());
        }
        return transferLogDAO.findAll();
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
        transferLogDAO.deleteAll();
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
