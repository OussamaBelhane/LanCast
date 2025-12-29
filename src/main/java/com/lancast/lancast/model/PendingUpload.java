package com.lancast.lancast.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PendingUpload {
    private String fileName;
    private byte[] fileData;
    private String clientIp;
    private String deviceType;
    private String timestamp;
    private long fileSize;

    public PendingUpload(String fileName, byte[] fileData, String clientIp, String deviceType) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.clientIp = clientIp;
        this.deviceType = deviceType;
        this.fileSize = fileData.length;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public String toString() {
        return fileName + " (" + (fileSize / 1024) + " KB) from " + clientIp;
    }
}
