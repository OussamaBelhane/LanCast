package com.lancast.lancast.service;

import com.lancast.lancast.model.PendingUpload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages file sessions - files shared by host and files received from clients.
 */
public class FileSessionService {

    private static final String UPLOADS_DIR = "uploads";
    private static final List<File> sessionFiles = new ArrayList<>();
    private static final List<File> receivedFiles = new ArrayList<>();
    private static final List<PendingUpload> pendingUploads = new ArrayList<>();

    public static void addFile(File f) {
        if (f != null && f.exists()) {
            sessionFiles.add(f);
            System.out.println("Added to session: " + f.getName());
        }
    }

    public static void removeFile(File f) {
        if (f != null) {
            sessionFiles.remove(f);
            System.out.println("Removed from session: " + f.getName());
        }
    }

    public static void resetSession() {
        sessionFiles.clear();
        System.out.println("Session cleared.");
    }

    public static List<File> getSessionFiles() {
        return sessionFiles;
    }

    public static boolean hasSessionFiles() {
        return !sessionFiles.isEmpty();
    }

    public static void loadReceivedFiles() {
        receivedFiles.clear();
        File uploadsDir = new File(UPLOADS_DIR);
        if (uploadsDir.exists() && uploadsDir.isDirectory()) {
            File[] files = uploadsDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile()) {
                        receivedFiles.add(f);
                        System.out.println("Loaded received file: " + f.getName());
                    }
                }
            }
        }
    }

    public static List<File> getReceivedFiles() {
        return new ArrayList<>(receivedFiles);
    }

    public static List<PendingUpload> getPendingUploads() {
        return new ArrayList<>(pendingUploads);
    }

    public static void addPendingUpload(PendingUpload upload) {
        pendingUploads.add(upload);
    }

    public static boolean acceptPendingUpload(String fileName) {
        for (PendingUpload pending : pendingUploads) {
            if (pending.getFileName().equals(fileName)) {
                try {
                    File outputFile = new File(UPLOADS_DIR, fileName);
                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        fos.write(pending.getFileData());
                    }
                    receivedFiles.add(outputFile);
                    pendingUploads.remove(pending);
                    System.out.println("Accepted and saved: " + fileName);
                    return true;
                } catch (IOException e) {
                    System.err.println("Error saving pending upload: " + e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    public static void ensureUploadsDirExists() {
        File uploadsDir = new File(UPLOADS_DIR);
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }
    }

    public static String getUploadsDir() {
        return UPLOADS_DIR;
    }
}
