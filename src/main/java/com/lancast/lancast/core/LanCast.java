package com.lancast.lancast.core;

import com.lancast.lancast.database.HistoryManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
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

        // Context for the root path (UI)
        server.createContext("/", new RootHandler());

        // Context for the download action (ZIP / Bulk)
        server.createContext("/download", new DownloadHandler());

        // API to get list of files
        server.createContext("/api/files", new FileListHandler());

        // Context to download individual files
        server.createContext("/files/", new FileDownloadHandler());

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

                try (OutputStream os = t.getResponseBody();
                        java.io.FileInputStream fis = new java.io.FileInputStream(fileToSend)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, count);
                    }
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
