package com.lancast.lancast.service.handler;

import com.lancast.lancast.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Handles downloads of received (uploaded) files.
 */
public class ReceivedFileDownloadHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"GET".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        String path = t.getRequestURI().getPath();
        String filename = path.substring("/received-files/".length());
        filename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);

        File fileToSend = null;
        for (File f : FileSessionService.getReceivedFiles()) {
            if (f.getName().equals(filename)) {
                fileToSend = f;
                break;
            }
        }

        if (fileToSend != null && fileToSend.exists()) {
            String userAgent = t.getRequestHeaders().getFirst("User-Agent");
            new HistoryService().logTransfer(
                    t.getRemoteAddress().getAddress().getHostAddress(),
                    "RECEIVED: " + fileToSend.getName(),
                    NetworkService.getDeviceType(userAgent));

            t.getResponseHeaders().set("Content-Type", "application/octet-stream");
            t.getResponseHeaders().set("Content-Disposition",
                    "attachment; filename=\"" + fileToSend.getName() + "\"");
            t.sendResponseHeaders(200, fileToSend.length());

            try (BufferedOutputStream bos = new BufferedOutputStream(t.getResponseBody(), 262144);
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend), 262144)) {
                byte[] buffer = new byte[262144];
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

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
