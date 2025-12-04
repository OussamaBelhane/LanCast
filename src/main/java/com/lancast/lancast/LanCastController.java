package com.lancast.lancast;

import com.lancast.lancast.core.LanCast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.File;
import java.util.List;

import com.lancast.lancast.database.HistoryManager;
import com.lancast.lancast.database.TransferLog;

public class LanCastController {

    @FXML
    private StackPane dropZone;

    @FXML
    private Label statusLabel;

    @FXML
    private ListView<File> fileListView;

    @FXML
    private Button clearBtn;

    @FXML
    private Button startBtn;

    @FXML
    private Button homeBtn;

    @FXML
    private Button historyBtn;

    @FXML
    private Button settingsBtn;

    private ObservableList<File> selectedFiles;

    @FXML
    private Label ipLabel;

    @FXML
    private Label pinLabel;

    @FXML
    private javafx.scene.image.ImageView qrCodeImageView;

    @FXML
    private Label qrPlaceholderLabel;

    private com.lancast.lancast.core.SettingsManager settingsManager;

    @FXML
    private VBox homeView;

    @FXML
    private VBox historyView;

    @FXML
    private VBox settingsView;

    @FXML
    private TableView<TransferLog> historyTable;

    @FXML
    private TableColumn<TransferLog, String> timeCol;

    @FXML
    private TableColumn<TransferLog, String> deviceCol;

    @FXML
    private TableColumn<TransferLog, String> fileCol;

    @FXML
    private TableColumn<TransferLog, String> ipCol;

    private boolean isServerRunning = false;

    @FXML
    public void initialize() {
        selectedFiles = FXCollections.observableArrayList();
        fileListView.setItems(selectedFiles);

        // Custom Cell Factory
        fileListView.setCellFactory(new Callback<ListView<File>, ListCell<File>>() {
            @Override
            public ListCell<File> call(ListView<File> param) {
                return new ListCell<File>() {
                    @Override
                    protected void updateItem(File item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox();
                            Label label = new Label(item.getName());
                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);
                            Button removeBtn = new Button("X");

                            // Style the button
                            removeBtn.setStyle(
                                    "-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-cursor: hand;");
                            removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                                    "-fx-background-color: #ffebeb; -fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-cursor: hand;"));
                            removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                                    "-fx-background-color: transparent; -fx-text-fill: #ff5555; -fx-font-weight: bold; -fx-cursor: hand;"));

                            removeBtn.setOnAction(event -> {
                                selectedFiles.remove(item);
                                LanCast.removeFile(item); // Ensure sync with core
                                updateStatus();
                            });

                            hbox.getChildren().addAll(label, spacer, removeBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });

        settingsManager = new com.lancast.lancast.core.SettingsManager();
        refreshConnectionInfo();
        updatePinDisplay();

        setupDragAndDrop();
        setupHistoryTable();

        homeView.setVisible(true);
        historyView.setVisible(false);
        settingsView.setVisible(false);
        updateNavigationStyles("home");
    }

    private void setupHistoryTable() {
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        deviceCol.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        ipCol.setCellValueFactory(new PropertyValueFactory<>("clientIp"));
    }

    private void loadHistoryData() {
        HistoryManager hm = new HistoryManager();
        List<TransferLog> logs = hm.getAllLogs();
        historyTable.setItems(FXCollections.observableArrayList(logs));
    }

    private void refreshConnectionInfo() {
        String ipAddress = LanCast.getIpAddress();
        ipLabel.setText("IP: " + ipAddress);
        try {
            com.lancast.lancast.core.QRService qrService = new com.lancast.lancast.core.QRService();
            javafx.scene.image.Image qrImage = qrService.generateQRCode(ipAddress, 200, 200);
            qrCodeImageView.setImage(qrImage);
            qrPlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("Failed to generate QR Code: " + e.getMessage());
        }
    }

    private void updatePinDisplay() {
        String pin = settingsManager.getPin();
        pinLabel.setText(pin);
    }

    @FXML
    private void handleRefreshPin() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        String newPin = String.valueOf(randomPin);
        settingsManager.setPin(newPin);
        updatePinDisplay();
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            var db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                addFiles(db.getFiles());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void addFiles(List<File> files) {
        for (File file : files) {
            if (!selectedFiles.contains(file)) {
                selectedFiles.add(file);
                LanCast.addFile(file);
            }
        }
        updateStatus();
    }

    private void updateStatus() {
        int count = selectedFiles.size();
        statusLabel.setText(count + " files selected");
    }

    @FXML
    private void handleClear() {
        selectedFiles.clear();
        LanCast.resetSession();
        updateStatus();
    }

    @FXML
    private void handleStart() {
        if (!isServerRunning) {
            try {
                LanCast.startServer();
                isServerRunning = true;
                startBtn.setText("Stop Server");
                startBtn.setStyle(
                        "-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 30; -fx-cursor: hand;");
                statusLabel.setText("Server Running");
                statusLabel.setStyle("-fx-text-fill: green;");
                refreshConnectionInfo();
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
            }
        } else {
            LanCast.stopServer();
            isServerRunning = false;
            startBtn.setText("Start Server");
            startBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #2ecc71, #27ae60); -fx-text-fill: white; -fx-background-radius: 30; -fx-cursor: hand;");
            statusLabel.setText("Server Stopped");
            statusLabel.setStyle("-fx-text-fill: #666666;");
        }
    }

    @FXML
    private void handleHome() {
        homeView.setVisible(true);
        historyView.setVisible(false);
        settingsView.setVisible(false);
        updateNavigationStyles("home");
    }

    @FXML
    private void handleHistory() {
        homeView.setVisible(false);
        historyView.setVisible(true);
        settingsView.setVisible(false);
        updateNavigationStyles("history");
        loadHistoryData();
    }

    @FXML
    private void handleSettings() {
        homeView.setVisible(false);
        historyView.setVisible(false);
        settingsView.setVisible(true);
        updateNavigationStyles("settings");
    }

    private void updateNavigationStyles(String activeView) {
        String activeStyle = "-fx-background-color: #E1E5EA; -fx-background-radius: 8; -fx-text-fill: black; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20; -fx-cursor: hand; -fx-graphic-text-gap: 10;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 10 20; -fx-cursor: hand; -fx-graphic-text-gap: 10;";

        homeBtn.setStyle(inactiveStyle);
        historyBtn.setStyle(inactiveStyle);
        settingsBtn.setStyle(inactiveStyle);

        switch (activeView) {
            case "home":
                homeBtn.setStyle(activeStyle);
                break;
            case "history":
                historyBtn.setStyle(activeStyle);
                break;
            case "settings":
                settingsBtn.setStyle(activeStyle);
                break;
        }
    }

    @FXML
    private void handleBrowseFiles() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Files to Share");
        List<File> files = fileChooser.showOpenMultipleDialog(dropZone.getScene().getWindow());
        if (files != null) {
            addFiles(files);
        }
    }
}
