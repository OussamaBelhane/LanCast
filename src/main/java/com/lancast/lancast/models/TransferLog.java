package com.lancast.lancast.models;

public class TransferLog {
    private int id;
    private String clientIp;
    private String fileName;
    private String deviceType;
    private int userId;
    private String timestamp;

    /**
     * Default constructor.
     */
    public TransferLog() {
    }

    public TransferLog(int id, String clientIp, String fileName, String deviceType, int userId, String timestamp) {
        this.id = id;
        this.clientIp = clientIp;
        this.fileName = fileName;
        this.deviceType = deviceType;
        this.userId = userId;
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

    public int getUserId() {
        return userId;
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

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + clientIp + " | " + fileName + " | " + deviceType + " | " + timestamp;
    }
}
