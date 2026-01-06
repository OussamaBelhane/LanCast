package com.lancast.lancast;

import com.lancast.lancast.service.AuthService;
import com.lancast.lancast.service.LanCastService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LanCastApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Stop server when window is closed
        stage.setOnCloseRequest(event -> {
            LanCastService.stopServer();
            System.out.println("Application closing, server stopped.");
        });

        // Check for saved session (Remember Me)
        AuthService authService = new AuthService();
        if (authService.restoreSession()) {
            // Auto-login - go directly to main view
            FXMLLoader fxmlLoader = new FXMLLoader(LanCastApplication.class.getResource("lancast-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("LanCast");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } else {
            // No saved session - show login screen
            FXMLLoader fxmlLoader = new FXMLLoader(LanCastApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1080, 768);
            stage.setTitle("LanCast - Login");
            stage.setScene(scene);
            stage.show();
        }
    }
}
