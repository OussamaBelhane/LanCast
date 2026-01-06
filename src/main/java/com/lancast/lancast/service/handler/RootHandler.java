package com.lancast.lancast.service.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.lancast.lancast.service.NetworkService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Serves the index.html file for the web UI.
 */
public class RootHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"GET".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        try {
            NetworkService.registerActivity(t.getRemoteAddress().getAddress().getHostAddress());
            byte[] bytes = Files.readAllBytes(Paths.get("index.html"));
            t.getResponseHeaders().set("Content-Type", "text/html");
            t.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Exception e) {
            sendResponse(t, 200, "Error: index.html not found");
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
