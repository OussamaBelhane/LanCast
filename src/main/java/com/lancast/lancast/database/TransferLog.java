package com.lancast.lancast.database;

public class TransferLog {
    public int id;
    public String clientIp;
    public String fileName;
    public String deviceType;
    public String timestamp;

    public TransferLog(int id, String clientIp, String fileName, String deviceType, String timestamp) {
        this.id = id;
        this.clientIp = clientIp;
        this.fileName = fileName;
        this.deviceType = deviceType;
        this.timestamp = timestamp;
    }

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

    @Override
    public String toString() {
        return "[" + id + "] " + clientIp + " | " + fileName + " | " + deviceType + " | " + timestamp;
    }
}
