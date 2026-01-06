package com.lancast.lancast.services;

import com.lancast.lancast.models.PendingUpload;
import com.lancast.lancast.services.handler.*;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main service for LanCast file sharing.
 * Coordinates between FileSessionService, NetworkService, and HTTP handlers.
 */
public class LanCastService {

    private static HttpServer server;

    // =============================================
    // FILE SESSION DELEGATION
    // =============================================

    public static void addFile(File f) {
        FileSessionService.addFile(f);
    }

    public static void removeFile(File f) {
        FileSessionService.removeFile(f);
    }

    public static void resetSession() {
        FileSessionService.resetSession();
    }

    public static List<File> getReceivedFiles() {
        return FileSessionService.getReceivedFiles();
    }

    public static List<PendingUpload> getPendingUploads() {
        return FileSessionService.getPendingUploads();
    }

    public static boolean acceptPendingUpload(String fileName) {
        return FileSessionService.acceptPendingUpload(fileName);
    }

    // =============================================
    // NETWORK DELEGATION
    // =============================================

    public static void setPeerCountListener(Consumer<Integer> listener) {
        NetworkService.setPeerCountListener(listener);
    }

    public static int getActivePeers() {
        return NetworkService.getActivePeers();
    }

    public static String getIpAddress() {
        return NetworkService.getIpAddress();
    }

    // =============================================
    // SERVER LIFECYCLE
    // =============================================

    public static void main(String[] args) throws IOException {
        startServer();
    }

    public static void startServer() throws IOException {
        if (server != null) {
            System.out.println("Server already running.");
            return;
        }

        // Initialize file session
        FileSessionService.ensureUploadsDirExists();
        FileSessionService.loadReceivedFiles();

        // Create and configure HTTP server
        int port = NetworkService.getPort();
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Register handlers
        server.createContext("/", new RootHandler());
        server.createContext("/download", new DownloadHandler());
        server.createContext("/api/files", new FileListHandler());
        server.createContext("/files/", new FileDownloadHandler());
        server.createContext("/api/verify-pin", new PinVerifyHandler());
        server.createContext("/api/upload", new FileUploadHandler());
        server.createContext("/api/received-files", new ReceivedFilesListHandler());
        server.createContext("/received-files/", new ReceivedFileDownloadHandler());

        // Start server
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();

        System.out.println("LAN-Stream Core Server started on port " + port);
        NetworkService.printIpAddresses();
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            System.out.println("Server stopped.");
        }
    }

    public static boolean isServerRunning() {
        return server != null;
    }
}
