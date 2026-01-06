package com.lancast.lancast.controllers;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Helper class for managing navigation between views.
 */
public class NavigationHelper {

    private final VBox homeView;
    private final VBox historyView;
    private final VBox settingsView;
    private final VBox receiveView;
    private final Button homeBtn;
    private final Button historyBtn;
    private final Button settingsBtn;
    private final Button receiveBtn;

    private Runnable onHomeSelected;
    private Runnable onHistorySelected;
    private Runnable onSettingsSelected;
    private Runnable onReceiveSelected;

    public NavigationHelper(VBox homeView, VBox historyView, VBox settingsView, VBox receiveView,
            Button homeBtn, Button historyBtn, Button settingsBtn, Button receiveBtn) {
        this.homeView = homeView;
        this.historyView = historyView;
        this.settingsView = settingsView;
        this.receiveView = receiveView;
        this.homeBtn = homeBtn;
        this.historyBtn = historyBtn;
        this.settingsBtn = settingsBtn;
        this.receiveBtn = receiveBtn;
    }

    public void setupNavIcons() {
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

    public void initializeViews() {
        homeView.setVisible(true);
        historyView.setVisible(false);
        settingsView.setVisible(false);
        receiveView.setVisible(false);
        updateNavigationStyles("home");
    }

    public void showView(String view) {
        homeView.setVisible("home".equals(view));
        historyView.setVisible("history".equals(view));
        settingsView.setVisible("settings".equals(view));
        receiveView.setVisible("receive".equals(view));
        updateNavigationStyles(view);

        switch (view) {
            case "home" -> {
                if (onHomeSelected != null)
                    onHomeSelected.run();
            }
            case "history" -> {
                if (onHistorySelected != null)
                    onHistorySelected.run();
            }
            case "settings" -> {
                if (onSettingsSelected != null)
                    onSettingsSelected.run();
            }
            case "receive" -> {
                if (onReceiveSelected != null)
                    onReceiveSelected.run();
            }
        }
    }

    public void updateNavigationStyles(String activeView) {
        homeBtn.getStyleClass().remove("active");
        historyBtn.getStyleClass().remove("active");
        settingsBtn.getStyleClass().remove("active");
        receiveBtn.getStyleClass().remove("active");

        switch (activeView) {
            case "home" -> homeBtn.getStyleClass().add("active");
            case "history" -> historyBtn.getStyleClass().add("active");
            case "settings" -> settingsBtn.getStyleClass().add("active");
            case "receive" -> receiveBtn.getStyleClass().add("active");
        }
    }

    public void setOnHomeSelected(Runnable callback) {
        this.onHomeSelected = callback;
    }

    public void setOnHistorySelected(Runnable callback) {
        this.onHistorySelected = callback;
    }

    public void setOnSettingsSelected(Runnable callback) {
        this.onSettingsSelected = callback;
    }

    public void setOnReceiveSelected(Runnable callback) {
        this.onReceiveSelected = callback;
    }
}
