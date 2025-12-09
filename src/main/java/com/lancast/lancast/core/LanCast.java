package com.lancast.lancast.core;

import com.lancast.lancast.database.HistoryManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * The Core Engine of LAN-Stream.
 * Handles HTTP requests and manages file transfers.
 */
public class LanCast {

    private static final int PORT = 8000;
    private static final String UPLOADS_DIR = "uploads";
    private static HttpServer server;
    // Session state to hold selected files (shared by host)
    private static List<File> sessionFiles = new ArrayList<>();
    // Received files (uploaded by web clients)
    private static List<File> receivedFiles = new ArrayList<>();

    // --- Control Methods for GUI ---

    /**
     * Adds a file to the current session.
     * 
     * @param f The file to add.
     */
    public static void addFile(File f) {
        if (f != null && f.exists()) {
            sessionFiles.add(f);
            System.out.println("Added to session: " + f.getName());
        }
    }

    /**
     * Removes a file from the current session.
     * 
     * @param f The file to remove.
     */
    public static void removeFile(File f) {
        if (f != null) {
            sessionFiles.remove(f);
            System.out.println("Removed from session: " + f.getName());
        }
    }

    /**
     * Clears the current session.
     */
    public static void resetSession() {
        sessionFiles.clear();
        System.out.println("Session cleared.");
    }

    // --- Server Logic ---

    public static void main(String[] args) throws IOException {
        startServer();
    }

    public static void startServer() throws IOException {
        if (server != null) {
            System.out.println("Server already running.");
            return;
        }

        // Create uploads directory if it doesn't exist
        File uploadsDir = new File(UPLOADS_DIR);
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }
        // Load existing uploaded files
        loadReceivedFiles();

        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Context for the root path (UI)
        server.createContext("/", new RootHandler());

        // Context for the download action (ZIP / Bulk)
        server.createContext("/download", new DownloadHandler());

        // API to get list of files
        server.createContext("/api/files", new FileListHandler());

        // Context to download individual files
        server.createContext("/files/", new FileDownloadHandler());

        // API to verify PIN
        server.createContext("/api/verify-pin", new PinVerifyHandler());

        // API to upload files
        server.createContext("/api/upload", new FileUploadHandler());

        // API to get list of received files
        server.createContext("/api/received-files", new ReceivedFilesListHandler());

        // Context to download received files
        server.createContext("/received-files/", new ReceivedFileDownloadHandler());

        // Use thread pool for better concurrent performance
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("LAN-Stream Core Server started on port " + PORT);
        printIpAddresses();
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Server stopped.");
        }
    }

    public static String getIpAddress() {
        String bestIp = "Unavailable";
        int bestPriority = -1; // Higher is better

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Skip loopback and down interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        String ip = addr.getHostAddress();
                        String name = iface.getDisplayName().toLowerCase();
                        int priority = 0;

                        // Check for virtual adapters FIRST to deprioritize them
                        if (name.contains("vmware") || name.contains("virtual") || name.contains("vbox")
                                || name.contains("wsl") || name.contains("hyper-v")) {
                            priority = 0;
                        } else if (name.contains("wi-fi") || name.contains("wlan") || name.contains("wireless")) {
                            priority = 4;
                        } else if (name.contains("eth") || name.contains("ethernet")) {
                            priority = 3;
                        } else {
                            priority = 1;
                        }

                        // Boost priority for site-local addresses (192.168.x.x, 10.x.x.x, etc.)
                        if (addr.isSiteLocalAddress()) {
                            priority += 1;
                        }

                        if (priority > bestPriority) {
                            bestPriority = priority;
                            bestIp = "http://" + ip + ":" + PORT + "/";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bestIp;
    }

    private static void printIpAddresses() {
        try {
            System.out.println("Available LAN IP Addresses:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        System.out.println("  - http://" + addr.getHostAddress() + ":" + PORT + " ("
                                + iface.getDisplayName() + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing IP addresses: " + e.getMessage());
        }
    }

    /**
     * Handler for the root path (GET /).
     * Serves the HTML page.
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            try {
                byte[] bytes = Files.readAllBytes(Paths.get("index.html"));
                t.getResponseHeaders().set("Content-Type", "text/html");
                t.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = t.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                String response = "Error: index.html not found";
                sendResponse(t, 200, response);
            }
        }
    }

    /**
     * API Handler to return list of files (GET /api/files).
     */
    static class FileListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < sessionFiles.size(); i++) {
                File f = sessionFiles.get(i);
                json.append(String.format("{\"name\": \"%s\", \"size\": %d}", f.getName(), f.length()));
                if (i < sessionFiles.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            t.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(t, 200, json.toString());
        }
    }

    /**
     * Handler for individual file downloads (GET /files/{filename}).
     */
    static class FileDownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            String path = t.getRequestURI().getPath();
            String filename = path.substring("/files/".length()); // Extract filename

            // URL decode the filename
            filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);

            File fileToSend = null;
            for (File f : sessionFiles) {
                if (f.getName().equals(filename)) {
                    fileToSend = f;
                    break;
                }
            }

            if (fileToSend != null && fileToSend.exists()) {
                // Log transfer
                String userAgent = t.getRequestHeaders().getFirst("User-Agent");
                new HistoryManager().logTransfer(
                        t.getRemoteAddress().getAddress().getHostAddress(),
                        fileToSend.getName(),
                        getDeviceType(userAgent));

                t.getResponseHeaders().set("Content-Type", "application/octet-stream");
                t.getResponseHeaders().set("Content-Disposition",
                        "attachment; filename=\"" + fileToSend.getName() + "\"");
                t.sendResponseHeaders(200, fileToSend.length());

                try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(t.getResponseBody(), 262144);
                        java.io.BufferedInputStream bis = new java.io.BufferedInputStream(
                                new java.io.FileInputStream(fileToSend), 262144)) {
                    byte[] buffer = new byte[262144]; // 256KB buffer for fast transfers
                    int count;
                    while ((count = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, count);
                    }
                    bos.flush();
                }
            } else {
                sendResponse(t, 404, "File Not Found");
            }
        }
    }

    /**
     * Handler for the bulk download path (POST /download).
     * Zips all files.
     */
    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod()) && !"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            if (sessionFiles.isEmpty()) {
                sendResponse(t, 404, "No files selected");
                return;
            }

            String userAgent = t.getRequestHeaders().getFirst("User-Agent");
            String deviceType = getDeviceType(userAgent);
            String clientIp = t.getRemoteAddress().getAddress().getHostAddress();
            HistoryManager historyManager = new HistoryManager();

            // Log each file individually
            for (File f : sessionFiles) {
                historyManager.logTransfer(clientIp, f.getName(), deviceType);
            }

            t.getResponseHeaders().set("Content-Type", "application/zip");
            t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"lanstream_files.zip\"");
            t.sendResponseHeaders(200, 0); // Chunked encoding

            try (OutputStream os = t.getResponseBody()) {
                ZipStreamManager.streamZip(sessionFiles, os);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * API Handler to verify PIN (POST /api/verify-pin).
     */
    static class PinVerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            // Read the PIN from the request body
            String submittedPin;
            try (java.util.Scanner scanner = new java.util.Scanner(t.getRequestBody(), StandardCharsets.UTF_8.name())) {
                submittedPin = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
            }

            // Get the actual PIN from settings
            // We create a new instance to ensure we get the latest value from the file
            String actualPin = new com.lancast.lancast.core.SettingsManager().getPin();

            if (actualPin.equals(submittedPin.trim())) {
                sendResponse(t, 200, "OK");
            } else {
                sendResponse(t, 401, "Invalid PIN");
            }
        }
    }

    private static String getDeviceType(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        if (userAgent.contains("Android"))
            return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
            return "iOS";
        if (userAgent.contains("Windows"))
            return "Windows";
        if (userAgent.contains("Macintosh"))
            return "macOS";
        if (userAgent.contains("Linux"))
            return "Linux";
        return "Unknown";
    }

    /**
     * Load existing files from the uploads directory.
     */
    private static void loadReceivedFiles() {
        receivedFiles.clear();
        File uploadsDir = new File(UPLOADS_DIR);
        if (uploadsDir.exists() && uploadsDir.isDirectory()) {
            File[] files = uploadsDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        receivedFiles.add(f);
                        System.out.println("Loaded received file: " + f.getName());
                    }
                }
            }
        }
    }

    /**
     * Get the list of received files (for GUI access).
     */
    public static List<File> getReceivedFiles() {
        return new ArrayList<>(receivedFiles);
    }

    /**
     * Handler for file uploads (POST /api/upload).
     * Handles multipart/form-data file uploads.
     */
    static class FileUploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            String contentType = t.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("multipart/form-data")) {
                sendResponse(t, 400, "Content-Type must be multipart/form-data");
                return;
            }

            // Extract boundary from Content-Type
            String boundary = null;
            for (String part : contentType.split(";")) {
                part = part.trim();
                if (part.startsWith("boundary=")) {
                    boundary = part.substring("boundary=".length());
                    // Remove quotes if present
                    if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                        boundary = boundary.substring(1, boundary.length() - 1);
                    }
                    break;
                }
            }

            if (boundary == null) {
                sendResponse(t, 400, "Missing boundary in Content-Type");
                return;
            }

            try {
                // Read the entire request body
                InputStream is = t.getRequestBody();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[262144]; // 256KB buffer for fast uploads
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                byte[] body = baos.toByteArray();

                // Parse multipart data
                String bodyStr = new String(body, StandardCharsets.ISO_8859_1);
                String[] parts = bodyStr.split("--" + boundary);

                int uploadedCount = 0;
                for (String part : parts) {
                    if (part.trim().isEmpty() || part.equals("--"))
                        continue;

                    // Find the filename
                    int filenameStart = part.indexOf("filename=\"");
                    if (filenameStart == -1)
                        continue;
                    filenameStart += "filename=\"".length();
                    int filenameEnd = part.indexOf("\"", filenameStart);
                    if (filenameEnd == -1)
                        continue;
                    String filename = part.substring(filenameStart, filenameEnd);

                    if (filename.isEmpty())
                        continue;

                    // Find the start of file data (after double CRLF)
                    int dataStart = part.indexOf("\r\n\r\n");
                    if (dataStart == -1)
                        continue;
                    dataStart += 4;

                    // Find where in the original byte array this part starts
                    String partHeader = bodyStr.substring(0, bodyStr.indexOf(part) + dataStart);
                    int byteDataStart = partHeader.getBytes(StandardCharsets.ISO_8859_1).length;

                    // Calculate the end (before the trailing CRLF)
                    String afterPart = part.substring(dataStart);
                    int byteDataEnd = byteDataStart + afterPart.getBytes(StandardCharsets.ISO_8859_1).length;
                    // Remove trailing \r\n
                    if (byteDataEnd >= 2) {
                        byteDataEnd -= 2;
                    }

                    // Extract file bytes
                    byte[] fileData = new byte[byteDataEnd - byteDataStart];
                    System.arraycopy(body, byteDataStart, fileData, 0, fileData.length);

                    // Save the file
                    File uploadedFile = new File(UPLOADS_DIR, filename);
                    try (FileOutputStream fos = new FileOutputStream(uploadedFile)) {
                        fos.write(fileData);
                    }

                    // Add to received files list
                    if (!receivedFiles.contains(uploadedFile)) {
                        receivedFiles.add(uploadedFile);
                    }

                    // Log the upload
                    String userAgent = t.getRequestHeaders().getFirst("User-Agent");
                    new HistoryManager().logTransfer(
                            t.getRemoteAddress().getAddress().getHostAddress(),
                            "UPLOADED: " + filename,
                            getDeviceType(userAgent));

                    uploadedCount++;
                    System.out.println("Received file: " + filename + " (" + fileData.length + " bytes)");
                }

                if (uploadedCount > 0) {
                    t.getResponseHeaders().set("Content-Type", "application/json");
                    sendResponse(t, 200, "{\"success\": true, \"count\": " + uploadedCount + "}");
                } else {
                    sendResponse(t, 400, "{\"success\": false, \"error\": \"No files uploaded\"}");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(t, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * API Handler to return list of received files (GET /api/received-files).
     */
    static class ReceivedFilesListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < receivedFiles.size(); i++) {
                File f = receivedFiles.get(i);
                if (f.exists()) {
                    json.append(String.format("{\"name\": \"%s\", \"size\": %d}", f.getName(), f.length()));
                    if (i < receivedFiles.size() - 1) {
                        json.append(",");
                    }
                }
            }
            json.append("]");

            t.getResponseHeaders().set("Content-Type", "application/json");
            sendResponse(t, 200, json.toString());
        }
    }

    /**
     * Handler for downloading received files (GET /received-files/{filename}).
     */
    static class ReceivedFileDownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            String path = t.getRequestURI().getPath();
            String filename = path.substring("/received-files/".length());

            // URL decode the filename
            filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);

            File fileToSend = null;
            for (File f : receivedFiles) {
                if (f.getName().equals(filename)) {
                    fileToSend = f;
                    break;
                }
            }

            if (fileToSend != null && fileToSend.exists()) {
                // Log transfer
                String userAgent = t.getRequestHeaders().getFirst("User-Agent");
                new HistoryManager().logTransfer(
                        t.getRemoteAddress().getAddress().getHostAddress(),
                        "RECEIVED: " + fileToSend.getName(),
                        getDeviceType(userAgent));

                t.getResponseHeaders().set("Content-Type", "application/octet-stream");
                t.getResponseHeaders().set("Content-Disposition",
                        "attachment; filename=\"" + fileToSend.getName() + "\"");
                t.sendResponseHeaders(200, fileToSend.length());

                try (java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(t.getResponseBody(), 262144);
                        java.io.BufferedInputStream bis = new java.io.BufferedInputStream(
                                new java.io.FileInputStream(fileToSend), 262144)) {
                    byte[] buffer = new byte[262144]; // 256KB buffer for fast transfers
                    int count;
                    while ((count = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, count);
                    }
                    bos.flush();
                }
            } else {
                sendResponse(t, 404, "File Not Found");
            }
        }
    }
}
