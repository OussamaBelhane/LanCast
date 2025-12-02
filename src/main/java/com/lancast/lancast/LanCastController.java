package com.lancast.lancast;

import com.lancast.lancast.core.LanCast;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    private boolean isServerRunning = false;

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
                ipLabel.setText("IP: " + LanCast.getIpAddress());
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
            ipLabel.setText("IP: Unavailable");
        }
    }

    @FXML
    protected void onViewLogsClick() {
        Stage logStage = new Stage();
        logStage.setTitle("Transfer Logs");

        javafx.scene.control.TableView<com.lancast.lancast.database.TransferLog> table = new javafx.scene.control.TableView<>();

        javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> ipCol = new javafx.scene.control.TableColumn<>(
                "Client IP");
        ipCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("clientIp"));

        javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> fileCol = new javafx.scene.control.TableColumn<>(
                "File Name");
        fileCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fileName"));

        javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> deviceCol = new javafx.scene.control.TableColumn<>(
                "Device");
        deviceCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("deviceType"));

        javafx.scene.control.TableColumn<com.lancast.lancast.database.TransferLog, String> timeCol = new javafx.scene.control.TableColumn<>(
                "Timestamp");
        timeCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));

        table.getColumns().addAll(ipCol, fileCol, deviceCol, timeCol);

        // Fetch logs
        table.getItems().addAll(new com.lancast.lancast.database.HistoryManager().getAllLogs());

        javafx.scene.Scene scene = new javafx.scene.Scene(table, 600, 400);
        logStage.setScene(scene);
        logStage.show();
    }
}
