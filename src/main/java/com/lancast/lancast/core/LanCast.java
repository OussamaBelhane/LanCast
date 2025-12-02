package com.lancast.lancast.core;

import com.lancast.lancast.database.HistoryManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * The Core Engine of LAN-Stream.
 * Handles HTTP requests and manages file transfers.
 */
public class LanCast {

    private static final int PORT = 8000;
    private static HttpServer server;
    // Session state to hold selected files
    private static List<File> sessionFiles = new ArrayList<>();

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
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Context for the root path
        server.createContext("/", new RootHandler());

        // Context for the download action
        server.createContext("/download", new DownloadHandler());

        server.setExecutor(null); // creates a default executor
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
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp())
                    continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address) {
                        return "http://" + addr.getHostAddress() + ":" + PORT;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unavailable";
    }

    private static void printIpAddresses() {
        try {
            System.out.println("Available LAN IP Addresses:");
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Filter out loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Only print IPv4 addresses for simplicity
                    if (addr instanceof Inet4Address) {
                        System.out.println("  - http://" + addr.getHostAddress() + ":" + PORT);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error listing IP addresses: " + e.getMessage());
        }
    }

    /**
     * Handler for the root path (GET /).
     * Serves a simple HTML page with a "Enter PIN" form.
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"GET".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            String response = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head><title>LAN-Stream</title></head>" +
                    "<body>" +
                    "<h1>LAN-Stream Download</h1>" +
                    "<form action='/download' method='post'>" +
                    "  <label for='pin'>Enter PIN:</label><br>" +
                    "  <input type='text' id='pin' name='pin'><br><br>" +
                    "  <input type='submit' value='Download Files'>" +
                    "</form>" +
                    "</body>" +
                    "</html>";

            sendResponse(t, 200, response);
        }
    }

    /**
     * Handler for the download path (POST /download).
     * Validates PIN and streams files.
     */
    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            if (!"POST".equals(t.getRequestMethod())) {
                sendResponse(t, 405, "Method Not Allowed");
                return;
            }

            // Read the request body to get the PIN
            // For simplicity in this core engine, we'll assume the body contains "pin=..."
            // In a real app, we'd parse the form data properly.
            // Here we just proceed to validation.

            // TODO: Call G3_PinSecurity.checkPin() in the verification step.
            // boolean isValid = G3_PinSecurity.checkPin(extractedPin);
            boolean isValid = true; // Mock validation for now

            if (isValid) {
                String userAgent = t.getRequestHeaders().getFirst("User-Agent");
                new HistoryManager().logTransfer(
                        t.getRemoteAddress().getAddress().getHostAddress(),
                        sessionFiles.size() > 1 ? "LanCast_Bundle.zip" : sessionFiles.get(0).getName(),
                        getDeviceType(userAgent));

                if (sessionFiles.isEmpty()) {
                    // Case A: List is Empty
                    sendResponse(t, 404, "No files selected");
                    return;
                } else if (sessionFiles.size() == 1) {
                    // Case B: 1 File - Send directly
                    File file = sessionFiles.get(0);
                    // Simple content type guessing
                    String contentType = "application/octet-stream"; // Default
                    if (file.getName().endsWith(".html"))
                        contentType = "text/html";
                    else if (file.getName().endsWith(".txt"))
                        contentType = "text/plain";
                    // Add more types as needed or use Files.probeContentType(path)

                    t.getResponseHeaders().set("Content-Type", contentType);
                    t.getResponseHeaders().set("Content-Disposition",
                            "attachment; filename=\"" + file.getName() + "\"");
                    t.sendResponseHeaders(200, file.length());

                    try (OutputStream os = t.getResponseBody();
                            java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                        byte[] buffer = new byte[8192];
                        int count;
                        while ((count = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, count);
                        }
                    }
                } else {
                    // Case C: 2+ Files - Zip and stream
                    t.getResponseHeaders().set("Content-Type", "application/zip");
                    t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"lanstream_files.zip\"");
                    t.sendResponseHeaders(200, 0); // Chunked encoding

                    try (OutputStream os = t.getResponseBody()) {
                        ZipStreamManager.streamZip(sessionFiles, os);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                sendResponse(t, 403, "Invalid PIN");
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
}
