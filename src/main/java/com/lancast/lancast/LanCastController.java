package com.lancast.lancast;

import com.lancast.lancast.core.LanCast;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

import java.io.File;
import java.util.List;

import com.lancast.lancast.database.HistoryManager;
import com.lancast.lancast.database.TransferLog;

public class LanCastController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private StackPane contentPane;
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
    @FXML
    private Button receiveBtn;
    @FXML
    private Label ipLabel;
    @FXML
    private Label pinLabel;
    @FXML
    private javafx.scene.image.ImageView qrCodeImageView;
    @FXML
    private Label qrPlaceholderLabel;
    @FXML
    private VBox homeView;
    @FXML
    private VBox historyView;
    @FXML
    private VBox settingsView;
    @FXML
    private VBox receiveView;
    @FXML
    private ListView<File> receivedFilesListView;
    @FXML
    private Label receivedCountLabel;
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
    @FXML
    private Circle statusCircle;
    @FXML
    private Label serverStatusLabel;
    @FXML
    private Label fileCountLabel;
    @FXML
    private Label peerCountLabel;
    @FXML
    private Label urlLabel;
    @FXML
    private Label versionLabel;
    @FXML
    private Label settingsPinLabel;

    // Theme controls
    @FXML
    private Button modeToggleBtn;
    @FXML
    private Button accentPurpleBtn;
    @FXML
    private Button accentBlueBtn;
    @FXML
    private Button accentPinkBtn;
    @FXML
    private Button accentGreenBtn;
    @FXML
    private Button accentOrangeBtn;

    private ObservableList<File> selectedFiles;
    private com.lancast.lancast.core.SettingsManager settingsManager;
    private boolean isServerRunning = false;
    private boolean isDarkMode = true;
    private String currentAccent = "purple";

    @FXML
    public void initialize() {
        selectedFiles = FXCollections.observableArrayList();
        fileListView.setItems(selectedFiles);
        setupFileListCellFactory();

        settingsManager = new com.lancast.lancast.core.SettingsManager();
        refreshConnectionInfo();
        updatePinDisplay();
        setupDragAndDrop();
        setupHistoryTable();
        setupNavIcons();

        homeView.setVisible(true);
        historyView.setVisible(false);
        settingsView.setVisible(false);
        receiveView.setVisible(false);
        updateNavigationStyles("home");

        loadSavedTheme();
        updateDashboardStats();
    }

    private void setupFileListCellFactory() {
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
                            hbox.setSpacing(8);
                            hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                            Label iconLabel = new Label("📄");
                            iconLabel.setStyle("-fx-font-size: 14px;");

                            Label nameLabel = new Label(item.getName());
                            nameLabel.setStyle(
                                    "-fx-font-weight: 600; -fx-text-fill: -text-primary; -fx-font-size: 11px;");

                            Label sizeLabel = new Label("(" + formatFileSize(item.length()) + ")");
                            sizeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: -text-muted;");

                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);

                            Button removeBtn = new Button("✕");
                            removeBtn.setStyle(
                                    "-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-cursor: hand;");
                            removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                                    "-fx-background-color: rgba(239,68,68,0.1); -fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-cursor: hand; -fx-background-radius: 4px;"));
                            removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                                    "-fx-background-color: transparent; -fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-cursor: hand;"));
                            removeBtn.setOnAction(event -> {
                                selectedFiles.remove(item);
                                LanCast.removeFile(item);
                                updateStatus();
                            });

                            hbox.getChildren().addAll(iconLabel, nameLabel, sizeLabel, spacer, removeBtn);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    private void setupNavIcons() {
        setNavIcon(homeBtn, "🏠");
        setNavIcon(historyBtn, "📊");
        setNavIcon(receiveBtn, "📥");
        setNavIcon(settingsBtn, "⚙");
    }

    private void setNavIcon(Button btn, String icon) {
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px;");
        btn.setGraphic(iconLabel);
    }

    // ============================================
    // THEME HANDLING
    // ============================================

    private void loadSavedTheme() {
        String savedMode = settingsManager.getTheme();
        String savedAccent = settingsManager.getAccentColor();

        isDarkMode = !"light".equals(savedMode);
        currentAccent = (savedAccent != null && !savedAccent.isEmpty()) ? savedAccent : "purple";

        applyTheme();
    }

    private void applyTheme() {
        // Remove old classes
        rootPane.getStyleClass().removeAll("mode-dark", "mode-light",
                "accent-purple", "accent-blue", "accent-pink", "accent-green", "accent-orange");

        // Apply mode
        rootPane.getStyleClass().add(isDarkMode ? "mode-dark" : "mode-light");

        // Apply accent
        rootPane.getStyleClass().add("accent-" + currentAccent);

        // Update toggle button icon
        if (modeToggleBtn != null) {
            modeToggleBtn.setText(isDarkMode ? "🌙" : "☀️");
        }

        // Update accent button selection
        updateAccentButtonSelection();

        // Save preferences
        settingsManager.setTheme(isDarkMode ? "dark" : "light");
        settingsManager.setAccentColor(currentAccent);
    }

    private void updateAccentButtonSelection() {
        if (accentPurpleBtn == null)
            return;

        accentPurpleBtn.getStyleClass().remove("selected");
        accentBlueBtn.getStyleClass().remove("selected");
        accentPinkBtn.getStyleClass().remove("selected");
        accentGreenBtn.getStyleClass().remove("selected");
        accentOrangeBtn.getStyleClass().remove("selected");

        switch (currentAccent) {
            case "purple":
                accentPurpleBtn.getStyleClass().add("selected");
                break;
            case "blue":
                accentBlueBtn.getStyleClass().add("selected");
                break;
            case "pink":
                accentPinkBtn.getStyleClass().add("selected");
                break;
            case "green":
                accentGreenBtn.getStyleClass().add("selected");
                break;
            case "orange":
                accentOrangeBtn.getStyleClass().add("selected");
                break;
        }
    }

    @FXML
    private void handleModeToggle() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    @FXML
    private void handleAccentPurple() {
        currentAccent = "purple";
        applyTheme();
    }

    @FXML
    private void handleAccentBlue() {
        currentAccent = "blue";
        applyTheme();
    }

    @FXML
    private void handleAccentPink() {
        currentAccent = "pink";
        applyTheme();
    }

    @FXML
    private void handleAccentGreen() {
        currentAccent = "green";
        applyTheme();
    }

    @FXML
    private void handleAccentOrange() {
        currentAccent = "orange";
        applyTheme();
    }

    // ============================================
    // HISTORY & CONNECTION
    // ============================================

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
        String fullUrl = LanCast.getIpAddress(); // Already returns "http://IP:PORT/"

        // Extract just the IP for the short display
        String ipOnly = fullUrl.replace("http://", "").replace("/", "");

        if (ipLabel != null)
            ipLabel.setText(ipOnly);
        if (urlLabel != null)
            urlLabel.setText(fullUrl);

        try {
            com.lancast.lancast.core.QRService qrService = new com.lancast.lancast.core.QRService();
            javafx.scene.image.Image qrImage = qrService.generateQRCode(fullUrl, 200, 200);
            qrCodeImageView.setImage(qrImage);
            if (qrPlaceholderLabel != null)
                qrPlaceholderLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("Failed to generate QR Code: " + e.getMessage());
        }
    }

    private void updatePinDisplay() {
        String pin = settingsManager.getPin();
        if (pinLabel != null)
            pinLabel.setText(pin);
        if (settingsPinLabel != null)
            settingsPinLabel.setText(pin);
    }

    @FXML
    private void handleRefreshPin() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        String newPin = String.valueOf(randomPin);
        settingsManager.setPin(newPin);
        updatePinDisplay();
    }

    @FXML
    private void handleCopyUrl() {
        if (urlLabel != null && urlLabel.getText() != null) {
            // Save the original URL first before any changes
            String urlToCopy = urlLabel.getText();

            // Copy to clipboard
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(urlToCopy);
            clipboard.setContent(content);

            // Show visual feedback
            urlLabel.setText("✓ Copied!");
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> urlLabel.setText(urlToCopy));
                } catch (InterruptedException e) {
                }
            }).start();
        }
    }

    // ============================================
    // DRAG AND DROP
    // ============================================

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

    // ============================================
    // STATUS UPDATES
    // ============================================

    private void updateStatus() {
        int count = selectedFiles.size();
        if (statusLabel != null)
            statusLabel.setText(String.valueOf(count));
        if (fileCountLabel != null)
            fileCountLabel.setText(String.valueOf(count));
    }

    private void updateDashboardStats() {
        if (fileCountLabel != null)
            fileCountLabel.setText(String.valueOf(selectedFiles.size()));
        if (peerCountLabel != null)
            peerCountLabel.setText("0");
    }

    private void updateServerStatus(boolean running) {
        if (statusCircle != null)
            statusCircle.setFill(running ? Color.web("#22c55e") : Color.web("#ef4444"));
        if (serverStatusLabel != null)
            serverStatusLabel.setText(running ? "Online" : "Offline");
        if (versionLabel != null)
            versionLabel.setText("v1.0.0 • " + (running ? "Online" : "Offline"));
    }

    // ============================================
    // BUTTON HANDLERS
    // ============================================

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
                startBtn.setText("⏹ Stop");
                startBtn.getStyleClass().remove("server-button-start");
                startBtn.getStyleClass().add("server-button-stop");
                updateServerStatus(true);
                refreshConnectionInfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            LanCast.stopServer();
            isServerRunning = false;
            startBtn.setText("▶ Start");
            startBtn.getStyleClass().remove("server-button-stop");
            startBtn.getStyleClass().add("server-button-start");
            updateServerStatus(false);
        }
    }

    // ============================================
    // NAVIGATION
    // ============================================

    @FXML
    private void handleHome() {
        showView("home");
    }

    @FXML
    private void handleHistory() {
        showView("history");
        loadHistoryData();
    }

    @FXML
    private void handleSettings() {
        showView("settings");
        updatePinDisplay();
        updateAccentButtonSelection();
    }

    @FXML
    private void handleReceive() {
        showView("receive");
        loadReceivedFiles();
    }

    private void showView(String view) {
        homeView.setVisible("home".equals(view));
        historyView.setVisible("history".equals(view));
        settingsView.setVisible("settings".equals(view));
        receiveView.setVisible("receive".equals(view));
        updateNavigationStyles(view);
        if ("home".equals(view))
            updateDashboardStats();
    }

    @FXML
    private void handleRefreshReceived() {
        loadReceivedFiles();
    }

    @FXML
    private void handleOpenUploadsFolder() {
        try {
            java.awt.Desktop.getDesktop().open(new File("uploads"));
        } catch (Exception e) {
            System.err.println("Could not open folder: " + e.getMessage());
        }
    }

    private void loadReceivedFiles() {
        List<File> received = LanCast.getReceivedFiles();
        receivedFilesListView.setItems(FXCollections.observableArrayList(received));
        receivedCountLabel.setText(received.size() + " files");

        receivedFilesListView.setCellFactory(lv -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(8);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label icon = new Label("📥");
                    icon.setStyle("-fx-font-size: 14px;");
                    Label name = new Label(item.getName() + " (" + formatFileSize(item.length()) + ")");
                    name.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-primary;");
                    hbox.getChildren().addAll(icon, name);
                    setGraphic(hbox);
                }
            }
        });
    }

    // ============================================
    // UTILITY
    // ============================================

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private void updateNavigationStyles(String activeView) {
        homeBtn.getStyleClass().remove("active");
        historyBtn.getStyleClass().remove("active");
        settingsBtn.getStyleClass().remove("active");
        receiveBtn.getStyleClass().remove("active");

        switch (activeView) {
            case "home":
                homeBtn.getStyleClass().add("active");
                break;
            case "history":
                historyBtn.getStyleClass().add("active");
                break;
            case "settings":
                settingsBtn.getStyleClass().add("active");
                break;
            case "receive":
                receiveBtn.getStyleClass().add("active");
                break;
        }
    }

    @FXML
    private void handleBrowseFiles() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Files");
        List<File> files = fileChooser.showOpenMultipleDialog(dropZone.getScene().getWindow());
        if (files != null)
            addFiles(files);
    }
}
