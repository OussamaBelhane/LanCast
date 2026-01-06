package com.lancast.lancast.service.handler;

import com.lancast.lancast.model.PendingUpload;
import com.lancast.lancast.service.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handles file uploads from web clients.
 */
public class FileUploadHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (!"POST".equals(t.getRequestMethod())) {
            sendResponse(t, 405, "Method Not Allowed");
            return;
        }

        String contentType = t.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            sendResponse(t, 400, "Content-Type must be multipart/form-data");
            return;
        }

        String boundary = null;
        for (String part : contentType.split(";")) {
            part = part.trim();
            if (part.startsWith("boundary=")) {
                boundary = part.substring("boundary=".length());
                if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                break;
            }
        }

        if (boundary == null) {
            sendResponse(t, 400, "Missing boundary in Content-Type");
            return;
        }

        try {
            InputStream is = t.getRequestBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[262144];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            byte[] body = baos.toByteArray();

            String bodyStr = new String(body, StandardCharsets.ISO_8859_1);
            String[] parts = bodyStr.split("--" + boundary);

            int uploadedCount = 0;
            for (String part : parts) {
                if (part.trim().isEmpty() || part.equals("--"))
                    continue;

                int filenameStart = part.indexOf("filename=\"");
                if (filenameStart == -1)
                    continue;
                filenameStart += "filename=\"".length();
                int filenameEnd = part.indexOf("\"", filenameStart);
                if (filenameEnd == -1)
                    continue;
                String filename = part.substring(filenameStart, filenameEnd);

                if (filename.isEmpty())
                    continue;

                int dataStart = part.indexOf("\r\n\r\n");
                if (dataStart == -1)
                    continue;
                dataStart += 4;

                String partHeader = bodyStr.substring(0, bodyStr.indexOf(part) + dataStart);
                int byteDataStart = partHeader.getBytes(StandardCharsets.ISO_8859_1).length;

                String afterPart = part.substring(dataStart);
                int byteDataEnd = byteDataStart + afterPart.getBytes(StandardCharsets.ISO_8859_1).length;
                if (byteDataEnd >= 2)
                    byteDataEnd -= 2;

                byte[] fileData = new byte[byteDataEnd - byteDataStart];
                System.arraycopy(body, byteDataStart, fileData, 0, fileData.length);

                String userAgent = t.getRequestHeaders().getFirst("User-Agent");
                String clientIp = t.getRemoteAddress().getAddress().getHostAddress();
                String deviceType = NetworkService.getDeviceType(userAgent);

                PendingUpload pendingUpload = new PendingUpload(filename, fileData, clientIp, deviceType);
                FileSessionService.addPendingUpload(pendingUpload);

                new HistoryService().logTransfer(clientIp, "UPLOADED: " + filename, deviceType);

                uploadedCount++;
                System.out.println("Received file: " + filename + " (" + fileData.length + " bytes)");
            }

            if (uploadedCount > 0) {
                t.getResponseHeaders().set("Content-Type", "application/json");
                sendResponse(t, 200, "{\"success\": true, \"count\": " + uploadedCount + "}");
            } else {
                sendResponse(t, 400, "{\"success\": false, \"error\": \"No files uploaded\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(t, 500, "{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
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
