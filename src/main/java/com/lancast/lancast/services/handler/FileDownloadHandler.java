package com.lancast.lancast.services.handler;

import com.lancast.lancast.services.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles individual file downloads with optional ZIP wrapping.
 */
public class FileDownloadHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"GET".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        String path = t.getRequestURI().getPath();
        String filename = path.substring("/files/".length());
        filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);

        File fileToSend = null;
        for (File f : FileSessionService.getSessionFiles()) {
            if (f.getName().equals(filename)) {
                fileToSend = f;
                break;
            }
        }

        if (fileToSend != null && fileToSend.exists()) {
            NetworkService.registerActivity(t.getRemoteAddress().getAddress().getHostAddress());
            try {
                SettingsService settings = new SettingsService();
                boolean forceZip = settings.getForceZip();
                String userAgent = t.getRequestHeaders().getFirst("User-Agent");

                if (forceZip) {
                    new HistoryService().logTransfer(
                            t.getRemoteAddress().getAddress().getHostAddress(),
                            fileToSend.getName() + " (zipped)",
                            NetworkService.getDeviceType(userAgent));

                    t.getResponseHeaders().set("Content-Type", "application/zip");
                    t.getResponseHeaders().set("Content-Disposition",
                            "attachment; filename=\"" + fileToSend.getName() + ".zip\"");
                    t.sendResponseHeaders(200, 0);

                    try (OutputStream os = t.getResponseBody()) {
                        List<File> singleFile = new ArrayList<>();
                        singleFile.add(fileToSend);
                        ZipStreamService.streamZip(singleFile, os);
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                } else {
                    new HistoryService().logTransfer(
                            t.getRemoteAddress().getAddress().getHostAddress(),
                            fileToSend.getName(),
                            NetworkService.getDeviceType(userAgent));

                    t.getResponseHeaders().set("Content-Type", "application/octet-stream");
                    t.getResponseHeaders().set("Content-Disposition",
                            "attachment; filename=\"" + fileToSend.getName() + "\"");
                    t.sendResponseHeaders(200, fileToSend.length());

                    try (BufferedOutputStream bos = new BufferedOutputStream(t.getResponseBody(), 262144);
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend),
                                    262144)) {
                        byte[] buffer = new byte[262144];
                        int count;
                        while ((count = bis.read(buffer)) != -1) {
                            bos.write(buffer, 0, count);
                        }
                        bos.flush();
                    } catch (IOException e) {
                        handleIOException(e);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during file download: " + e.getMessage());
            } finally {
                // No cleanup needed
            }
        } else {
            sendResponse(t, 404, "File Not Found");
        }
    }

    private void handleIOException(IOException e) {
        // Silently handle connection reset (client cancelled download)
        if (e.getMessage() != null &&
                (e.getMessage().contains("Connection reset") ||
                        e.getMessage().contains("connection was aborted") ||
                        e.getMessage().contains("Broken pipe"))) {
            System.out.println("Client cancelled download");
        } else {
            e.printStackTrace();
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
