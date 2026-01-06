package com.lancast.lancast.service.handler;

import com.lancast.lancast.service.SettingsService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Verifies PIN code for access control.
 */
public class PinVerifyHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"POST".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        String submittedPin;
        try (Scanner scanner = new Scanner(t.getRequestBody(), StandardCharsets.UTF_8.name())) {
            submittedPin = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        }

        String actualPin = new SettingsService().getPin();

        if (actualPin.equals(submittedPin.trim())) {
            sendResponse(t, 200, "OK");
        } else {
            sendResponse(t, 401, "Invalid PIN");
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
