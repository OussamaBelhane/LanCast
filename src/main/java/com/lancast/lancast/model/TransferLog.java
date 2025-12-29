package com.lancast.lancast.model;

/**
 * Represents a file transfer log entry in the database.
 * This class encapsulates information about a single file transfer transaction,
 * including client information, file details, and transfer metadata.
 */
public class TransferLog {
    private int id;
    private String clientIp;
    private String fileName;
    private String deviceType;
    private String timestamp;

    /**
     * Default constructor.
     */
    public TransferLog() {
    }

    /**
     * Creates a new TransferLog with all fields.
     * 
     * @param id The unique identifier for this transfer log
     * @param clientIp The IP address of the client who received/sent the file
     * @param fileName The name of the transferred file
     * @param deviceType The type of device (e.g., "Android 13", "Windows 11", "iOS 17")
     * @param timestamp The timestamp when the transfer occurred
     */
    public TransferLog(int id, String clientIp, String fileName, String deviceType, String timestamp) {
        this.id = id;
        this.clientIp = clientIp;
        this.fileName = fileName;
        this.deviceType = deviceType;
        this.timestamp = timestamp;
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Setters

    public void setId(int id) {
        this.id = id;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns a string representation of this transfer log.
     * 
     * @return A formatted string containing all log information
     */
    @Override
    public String toString() {
        return "[" + id + "] " + clientIp + " | " + fileName + " | " + deviceType + " | " + timestamp;
    }
}
