package com.lancast.lancast.service.handler;

import com.lancast.lancast.service.FileSessionService;
import com.lancast.lancast.service.NetworkService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Returns a JSON list of files available for download.
 */
public class FileListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"GET".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        NetworkService.registerActivity(t.getRemoteAddress().getAddress().getHostAddress());
        List<File> sessionFiles = FileSessionService.getSessionFiles();
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

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }
}
