package com.lancast.lancast.controllers;

import com.lancast.lancast.services.LanCastService;
import com.lancast.lancast.services.QRService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Helper class for server control and connection info management.
 */
public class ServerHelper {

    private final Button startBtn;
    private final Label ipLabel;
    private final Label urlLabel;
    private final Label pinLabel;
    private final Label peerCountLabel;
    private final Label serverStatusLabel;
    private final Label versionLabel;
    private final Label fileCountLabel;
    private final Circle statusCircle;
    private final ImageView qrCodeImageView;
    private final Label qrPlaceholderLabel;

    private boolean isServerRunning = false;

    public ServerHelper(Button startBtn, Label ipLabel, Label urlLabel, Label pinLabel,
            Label peerCountLabel, Label serverStatusLabel, Label versionLabel,
            Label fileCountLabel, Circle statusCircle, ImageView qrCodeImageView,
            Label qrPlaceholderLabel) {
        this.startBtn = startBtn;
        this.ipLabel = ipLabel;
        this.urlLabel = urlLabel;
        this.pinLabel = pinLabel;
        this.peerCountLabel = peerCountLabel;
        this.serverStatusLabel = serverStatusLabel;
        this.versionLabel = versionLabel;
        this.fileCountLabel = fileCountLabel;
        this.statusCircle = statusCircle;
        this.qrCodeImageView = qrCodeImageView;
        this.qrPlaceholderLabel = qrPlaceholderLabel;
    }

    public void initialize() {
        LanCastService.setPeerCountListener(count -> {
            Platform.runLater(() -> {
                if (peerCountLabel != null) {
                    peerCountLabel.setText(String.valueOf(count));
                }
            });
        });
    }

    public void refreshConnectionInfo() {
        String fullUrl = LanCastService.getIpAddress();
        String ipOnly = fullUrl.replace("http://", "").replace("/", "");

        if (ipLabel != null)
            ipLabel.setText(ipOnly);
        if (urlLabel != null)
            urlLabel.setText(fullUrl);

        try {
            QRService qrService = new QRService();
            Image qrImage = qrService.generateQRCode(fullUrl, 200, 200);
            qrCodeImageView.setImage(qrImage);
            if (qrPlaceholderLabel != null)
                qrPlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("Failed to generate QR Code: " + e.getMessage());
        }
    }

    public void handleStart() {
        if (!isServerRunning) {
            try {
                LanCastService.startServer();
                isServerRunning = true;
                startBtn.setText("⏹ Stop");
                startBtn.getStyleClass().remove("server-button-start");
                startBtn.getStyleClass().add("server-button-stop");
                updateServerStatus(true);
                refreshConnectionInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LanCastService.stopServer();
            isServerRunning = false;
            startBtn.setText("▶ Start");
            startBtn.getStyleClass().remove("server-button-stop");
            startBtn.getStyleClass().add("server-button-start");
            updateServerStatus(false);
        }
    }

    public void updateServerStatus(boolean running) {
        if (statusCircle != null)
            statusCircle.setFill(running ? Color.web("#22c55e") : Color.web("#ef4444"));
        if (serverStatusLabel != null)
            serverStatusLabel.setText(running ? "Online" : "Offline");
        if (versionLabel != null)
            versionLabel.setText("v1.0.0 • " + (running ? "Online" : "Offline"));
    }

    public void updateDashboardStats(int fileCount) {
        if (fileCountLabel != null)
            fileCountLabel.setText(String.valueOf(fileCount));
        if (peerCountLabel != null && peerCountLabel.getText().isEmpty())
            peerCountLabel.setText("0");
    }

    public void handleCopyUrl() {
        if (urlLabel != null && urlLabel.getText() != null) {
            String urlToCopy = urlLabel.getText();

            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(urlToCopy);
            clipboard.setContent(content);

            urlLabel.setText("✓ Copied!");
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> urlLabel.setText(urlToCopy));
                } catch (InterruptedException ignored) {
                }
            }).start();
        }
    }

    public boolean isServerRunning() {
        return isServerRunning;
    }
}
