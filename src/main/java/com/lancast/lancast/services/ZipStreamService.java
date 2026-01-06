package com.lancast.lancast.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipStreamService {

    public static void streamZip(List<File> files, OutputStream out) throws IOException {
        // Use BufferedOutputStream for efficiency
        try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out))) {

            for (File file : files) {
                if (!file.exists() || !file.isFile()) {
                    System.err.println("Skipping invalid file: " + file.getAbsolutePath());
                    continue;
                }

                try {
                    // Create a new ZipEntry for the file
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);

                    // Read the file content and write it to the ZipOutputStream
                    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                        byte[] buffer = new byte[8192]; // 8KB buffer
                        int bytesRead;
                        while ((bytesRead = bis.read(buffer)) != -1) {
                            zos.write(buffer, 0, bytesRead);
                        }
                    }

                    // Close the current entry
                    zos.closeEntry();
                } catch (IOException e) {
                    // Connection reset is normal when client cancels download
                    if (e.getMessage() != null &&
                            (e.getMessage().contains("Connection reset") ||
                                    e.getMessage().contains("connection was aborted"))) {
                        System.out.println("Client disconnected during download: " + file.getName());
                        return; // Exit gracefully
                    }
                    throw e;
                }
            }

            zos.finish();
            zos.flush();
        } catch (IOException e) {
            // Silently handle connection reset errors
            if (e.getMessage() != null &&
                    (e.getMessage().contains("Connection reset") ||
                            e.getMessage().contains("connection was aborted"))) {
                System.out.println("Client disconnected during ZIP streaming");
                return;
            }
            throw e;
        }
    }
}
