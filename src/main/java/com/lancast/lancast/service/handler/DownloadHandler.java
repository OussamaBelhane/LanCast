package com.lancast.lancast.service.handler;

import com.lancast.lancast.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles bulk download (all files as ZIP).
 */
public class DownloadHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"POST".equals(t.getRequestMethod()) && !"GET".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        List<File> sessionFiles = FileSessionService.getSessionFiles();
        if (sessionFiles.isEmpty()) {
            sendResponse(t, 404, "No files selected");
            return;
        }

        NetworkService.registerActivity(t.getRemoteAddress().getAddress().getHostAddress());
        try {
            String userAgent = t.getRequestHeaders().getFirst("User-Agent");
            String deviceType = NetworkService.getDeviceType(userAgent);
            String clientIp = t.getRemoteAddress().getAddress().getHostAddress();
            HistoryService historyManager = new HistoryService();

            for (File f : sessionFiles) {
                historyManager.logTransfer(clientIp, f.getName(), deviceType);
            }

            t.getResponseHeaders().set("Content-Type", "application/zip");
            t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"lanstream_files.zip\"");
            t.sendResponseHeaders(200, 0);

            try (OutputStream os = t.getResponseBody()) {
                ZipStreamService.streamZip(sessionFiles, os);
            } catch (IOException e) {
                // Silently handle connection reset (client cancelled download)
                if (e.getMessage() != null &&
                        (e.getMessage().contains("Connection reset") ||
                                e.getMessage().contains("connection was aborted"))) {
                    System.out.println("Client cancelled download");
                } else {
                    e.printStackTrace();
                }
            }
        } finally {
            // No cleanup needed
        }
    }

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
