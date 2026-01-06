package com.lancast.lancast.controller;

import com.lancast.lancast.LanCastApplication;
import com.lancast.lancast.service.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private VBox confirmPasswordBox;
    @FXML
    private Button submitBtn;
    @FXML
    private Button toggleBtn;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Label togglePromptLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private HBox rememberMeBox;

    private boolean isSignupMode = false;
    private AuthService authService;

    @FXML
    public void initialize() {
        authService = new AuthService();
        updateMode();

        // Clear error on input
        usernameField.textProperty().addListener((obs, old, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError());
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> clearError());
    }

    @FXML
    private void handleSubmit() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            return;
        }

        if (isSignupMode) {
            // Signup
            String confirmPassword = confirmPasswordField.getText();
            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                return;
            }

            if (authService.signup(username, password)) {
                navigateToMainView();
            } else {
                showError("Username already exists");
            }
        } else {
            // Login
            if (authService.login(username, password)) {
                // Save session if Remember Me is checked
                if (rememberMeCheckbox.isSelected()) {
                    AuthService.saveSession(username);
                }
                navigateToMainView();
            } else {
                showError("Invalid username or password");
            }
        }
    }

    @FXML
    private void handleToggleMode() {
        isSignupMode = !isSignupMode;
        updateMode();
        clearError();
        clearFields();
    }

    private void updateMode() {
        if (isSignupMode) {
            subtitleLabel.setText("Create your account");
            submitBtn.setText("Create Account");
            togglePromptLabel.setText("Already have an account?");
            toggleBtn.setText("Sign In");
            confirmPasswordBox.setVisible(true);
            confirmPasswordBox.setManaged(true);
            rememberMeBox.setVisible(false);
            rememberMeBox.setManaged(false);
        } else {
            subtitleLabel.setText("Welcome back");
            submitBtn.setText("Sign In");
            togglePromptLabel.setText("Don't have an account?");
            toggleBtn.setText("Sign Up");
            confirmPasswordBox.setVisible(false);
            confirmPasswordBox.setManaged(false);
            rememberMeBox.setVisible(true);
            rememberMeBox.setManaged(true);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-opacity: 1;");

        // Add shake animation effect
        usernameField.getParent().setStyle("-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.3), 15, 0, 0, 0);");

        // Reset after delay
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Platform.runLater(() -> {
                    usernameField.getParent().setStyle("");
                });
            } catch (InterruptedException ignored) {
            }
        }).start();
    }

    private void clearError() {
        errorLabel.setText("");
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void navigateToMainView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LanCastApplication.class.getResource("lancast-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) submitBtn.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception e) {
            showError("Failed to load application");
            e.printStackTrace();
        }
    }
}
