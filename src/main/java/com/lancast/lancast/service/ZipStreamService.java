package com.lancast.lancast.service;

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

                // Create a new ZipEntry for the file
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);

                // Read the file content and write it to the ZipOutputStream
                // Using BufferedInputStream for efficient reading
                try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                    byte[] buffer = new byte[8192]; // 8KB buffer
                    int bytesRead;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                }

                // Close the current entry
                zos.closeEntry();
            }
            
            // Finish the ZIP process (this writes the central directory)
            // Note: We do NOT close the underlying 'out' stream here, as the server might need to do more with it,
            // or the server framework handles closing the exchange stream. 
            // However, ZipOutputStream.close() DOES close the underlying stream.
            // In the context of HttpExchange, closing the response body stream is usually fine/required to end the response.
            zos.finish();
            zos.flush();
        }
    }
}
