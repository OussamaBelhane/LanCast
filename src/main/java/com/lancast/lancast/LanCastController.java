package com.lancast.lancast;

import com.lancast.lancast.core.LanCast;
import com.lancast.lancast.core.QRService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LanCastController {
    @FXML
    private Label statusLabel;
    @FXML
    private Label ipLabel;
    @FXML
    private Button serverButton;
    @FXML
    private ImageView qrCodeImageView;
    @FXML
    private Label qrPlaceholderLabel;
    @FXML
    private Label pinDisplayLabel;

    // Navigation Buttons
    @FXML
    private Button navHomeButton;
    @FXML
    private Button navHistoryButton;
    @FXML
    private Button navSettingsButton;

    // Panes
    @FXML
    private javafx.scene.layout.HBox homePane;
    @FXML
    private javafx.scene.layout.VBox historyPane;
    @FXML
    private javafx.scene.layout.VBox settingsPane;

    // History Table
    @FXML
    private javafx.scene.control.TableView<com.lancast.lancast.database.TransferLog> historyTable;
    @FXML
    private javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> historyIpCol;
    @FXML
    private javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> historyFileCol;
    @FXML
    private javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> historyDeviceCol;
    @FXML
    private javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> historyTimeCol;

    // Settings
    @FXML
    private javafx.scene.control.TextField pinTextField;
    @FXML
    private Label settingsStatusLabel;

    private boolean isServerRunning = false;
    private com.lancast.lancast.core.SettingsManager settingsManager;

    @FXML
    public void initialize() {
        settingsManager = new com.lancast.lancast.core.SettingsManager();
        refreshConnectionInfo();
        setupHistoryTable();
        updatePinDisplay();
    }

    private void refreshConnectionInfo() {
        String ipAddress = LanCast.getIpAddress();
        ipLabel.setText("IP: " + ipAddress);
        try {
            QRService qrService = new QRService();
            javafx.scene.image.Image qrImage = qrService.generateQRCode(ipAddress, 200, 200);
            qrCodeImageView.setImage(qrImage);
            qrPlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("Failed to generate QR Code: " + e.getMessage());
            statusLabel.setText("QR Gen Error: " + e.getMessage());
        }
    }

    private void updatePinDisplay() {
        String pin = settingsManager.getPin();
        pinDisplayLabel.setText(pin);
        pinTextField.setText(pin);
    }

    private void setupHistoryTable() {
        historyIpCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("clientIp"));
        historyFileCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fileName"));
        historyDeviceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deviceType"));
        historyTimeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
    }

    // --- Navigation ---

    @FXML
    protected void onHomeClick() {
        showPane(homePane, navHomeButton);
    }

    @FXML
    protected void onHistoryClick() {
        showPane(historyPane, navHistoryButton);
        onRefreshHistoryClick();
    }

    @FXML
    protected void onSettingsClick() {
        showPane(settingsPane, navSettingsButton);
        settingsStatusLabel.setText("");
    }

    private void showPane(javafx.scene.layout.Pane paneToShow, Button activeButton) {
        homePane.setVisible(false);
        historyPane.setVisible(false);
        settingsPane.setVisible(false);
        paneToShow.setVisible(true);

        navHomeButton.getStyleClass().remove("active");
        navHistoryButton.getStyleClass().remove("active");
        navSettingsButton.getStyleClass().remove("active");
        activeButton.getStyleClass().add("active");
    }

    // --- Home Actions ---

    @FXML
    protected void onSelectFilesClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Share");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(new Stage());

        if (selectedFiles != null) {
            LanCast.resetSession();
            for (File file : selectedFiles) {
                LanCast.addFile(file);
            }
            statusLabel.setText(selectedFiles.size() + " files selected.");
            statusLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    protected void onServerToggleClick() {
        if (!isServerRunning) {
            try {
                LanCast.startServer();
                isServerRunning = true;
                serverButton.setText("Stop Server");
                statusLabel.setText("Server Running");
                statusLabel.setStyle("-fx-text-fill: green;");

                refreshConnectionInfo();

            } catch (IOException e) {
                statusLabel.setText("Error starting server: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } else {
            LanCast.stopServer();
            isServerRunning = false;
            serverButton.setText("Start Server");
            statusLabel.setText("Server Stopped");
            statusLabel.setStyle("-fx-text-fill: red;");
            // Keep QR code and IP visible
        }
    }

    // --- History Actions ---

    @FXML
    protected void onRefreshHistoryClick() {
        historyTable.getItems().clear();
        historyTable.getItems().addAll(new com.lancast.lancast.database.HistoryManager().getAllLogs());
    }

    @FXML
    protected void onClearHistoryClick() {
        new com.lancast.lancast.database.HistoryManager().clearLogs();
        onRefreshHistoryClick();
    }

    // --- Settings Actions ---

    @FXML
    protected void onSaveSettingsClick() {
        String newPin = pinTextField.getText();
        if (newPin != null && !newPin.isEmpty()) {
            settingsManager.setPin(newPin);
            updatePinDisplay();
            settingsStatusLabel.setText("Settings Saved!");
            settingsStatusLabel.setStyle("-fx-text-fill: green;");
        } else {
            settingsStatusLabel.setText("PIN cannot be empty.");
            settingsStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // Legacy method removed: onViewLogsClick (replaced by onHistoryClick)
}
