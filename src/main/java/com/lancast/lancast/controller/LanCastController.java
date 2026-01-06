package com.lancast.lancast.controller;

import com.lancast.lancast.LanCastApplication;
import com.lancast.lancast.service.AuthService;
import com.lancast.lancast.service.SettingsService;
import com.lancast.lancast.model.TransferLog;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main controller for the LanCast application.
 * Delegates specific functionality to helper classes for better organization.
 */
public class LanCastController {

    // =============================================
    // FXML COMPONENTS
    // =============================================

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
    private ImageView qrCodeImageView;
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
    private ListView<String> receivedFilesListView;
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
    @FXML
    private Button forceZipToggleBtn;
    @FXML
    private Label currentUserLabel;

    // =============================================
    // HELPER CLASSES
    // =============================================

    private ThemeHelper themeHelper;
    private NavigationHelper navigationHelper;
    private FileHelper fileHelper;
    private ServerHelper serverHelper;
    private SettingsHelper settingsHelper;
    private HistoryHelper historyHelper;
    private ReceiveHelper receiveHelper;
    private SettingsService settingsManager;

    // =============================================
    // INITIALIZATION
    // =============================================

    @FXML
    public void initialize() {
        settingsManager = new SettingsService();

        // Initialize helpers
        initializeHelpers();

        // Setup components
        fileHelper.initialize();
        navigationHelper.setupNavIcons();
        navigationHelper.initializeViews();
        historyHelper.setupHistoryTable();
        serverHelper.initialize();
        serverHelper.refreshConnectionInfo();
        settingsHelper.updatePinDisplay();
        settingsHelper.updateForceZipDisplay();
        themeHelper.loadSavedTheme();
        serverHelper.updateDashboardStats(fileHelper.getFileCount());

        // Display current username
        updateCurrentUserDisplay();

        // Setup navigation callbacks
        setupNavigationCallbacks();
    }

    private void initializeHelpers() {
        themeHelper = new ThemeHelper(
                rootPane, modeToggleBtn,
                accentPurpleBtn, accentBlueBtn, accentPinkBtn, accentGreenBtn, accentOrangeBtn,
                settingsManager);

        navigationHelper = new NavigationHelper(
                homeView, historyView, settingsView, receiveView,
                homeBtn, historyBtn, settingsBtn, receiveBtn);

        fileHelper = new FileHelper(dropZone, fileListView, statusLabel, fileCountLabel);

        serverHelper = new ServerHelper(
                startBtn, ipLabel, urlLabel, pinLabel, peerCountLabel,
                serverStatusLabel, versionLabel, fileCountLabel, statusCircle,
                qrCodeImageView, qrPlaceholderLabel);

        settingsHelper = new SettingsHelper(pinLabel, settingsPinLabel, forceZipToggleBtn, settingsManager);

        historyHelper = new HistoryHelper(historyTable, timeCol, deviceCol, fileCol, ipCol);

        receiveHelper = new ReceiveHelper(receivedFilesListView, receivedCountLabel);
    }

    private void setupNavigationCallbacks() {
        navigationHelper.setOnHomeSelected(() -> serverHelper.updateDashboardStats(fileHelper.getFileCount()));
        navigationHelper.setOnHistorySelected(() -> historyHelper.loadHistoryData());
        navigationHelper.setOnSettingsSelected(() -> {
            settingsHelper.updatePinDisplay();
            themeHelper.updateAccentButtonSelection();
        });
        navigationHelper.setOnReceiveSelected(() -> receiveHelper.loadReceivedFiles());
    }

    private void updateCurrentUserDisplay() {
        if (currentUserLabel != null && AuthService.isLoggedIn()) {
            currentUserLabel.setText(AuthService.getCurrentUser().getUsername());
        } else if (currentUserLabel != null) {
            currentUserLabel.setText("Guest");
        }
    }

    // =============================================
    // NAVIGATION HANDLERS
    // =============================================

    @FXML
    private void handleHome() {
        navigationHelper.showView("home");
    }

    @FXML
    private void handleHistory() {
        navigationHelper.showView("history");
    }

    @FXML
    private void handleSettings() {
        navigationHelper.showView("settings");
    }

    @FXML
    private void handleReceive() {
        navigationHelper.showView("receive");
    }

    // =============================================
    // FILE HANDLERS
    // =============================================

    @FXML
    private void handleClear() {
        fileHelper.handleClear();
    }

    @FXML
    private void handleBrowseFiles() {
        fileHelper.handleBrowseFiles();
    }

    // =============================================
    // SERVER HANDLERS
    // =============================================

    @FXML
    private void handleStart() {
        serverHelper.handleStart();
    }

    @FXML
    private void handleCopyUrl() {
        serverHelper.handleCopyUrl();
    }

    // =============================================
    // SETTINGS HANDLERS
    // =============================================

    @FXML
    private void handleRefreshPin() {
        settingsHelper.handleRefreshPin();
    }

    @FXML
    private void handleForceZipToggle() {
        settingsHelper.handleForceZipToggle();
    }

    // =============================================
    // THEME HANDLERS
    // =============================================

    @FXML
    private void handleModeToggle() {
        themeHelper.handleModeToggle();
    }

    @FXML
    private void handleAccentPurple() {
        themeHelper.setAccent("purple");
    }

    @FXML
    private void handleAccentBlue() {
        themeHelper.setAccent("blue");
    }

    @FXML
    private void handleAccentPink() {
        themeHelper.setAccent("pink");
    }

    @FXML
    private void handleAccentGreen() {
        themeHelper.setAccent("green");
    }

    @FXML
    private void handleAccentOrange() {
        themeHelper.setAccent("orange");
    }

    // =============================================
    // RECEIVE HANDLERS
    // =============================================

    @FXML
    private void handleRefreshReceived() {
        receiveHelper.handleRefreshReceived();
    }

    @FXML
    private void handleOpenUploadsFolder() {
        receiveHelper.handleOpenUploadsFolder();
    }

    // =============================================
    // AUTH HANDLERS
    // =============================================

    @FXML
    private void handleLogout() {
        new AuthService().logout();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LanCastApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 800, 650);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setTitle("LanCast - Login");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to load login view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
