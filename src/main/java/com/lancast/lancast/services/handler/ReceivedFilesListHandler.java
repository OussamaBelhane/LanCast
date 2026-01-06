package com.lancast.lancast.services.handler;

import com.lancast.lancast.services.FileSessionService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Returns JSON list of received files.
 */
public class ReceivedFilesListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"GET".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        List<File> receivedFiles = FileSessionService.getReceivedFiles();
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

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
