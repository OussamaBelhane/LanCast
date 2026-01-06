package com.lancast.lancast.controller;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import com.lancast.lancast.service.SettingsService;

/**
 * Helper class for managing theme (dark/light mode) and accent colors.
 */
public class ThemeHelper {

    private final BorderPane rootPane;
    private final Button modeToggleBtn;
    private final Button accentPurpleBtn;
    private final Button accentBlueBtn;
    private final Button accentPinkBtn;
    private final Button accentGreenBtn;
    private final Button accentOrangeBtn;
    private final SettingsService settingsManager;

    private boolean isDarkMode = true;
    private String currentAccent = "purple";

    public ThemeHelper(BorderPane rootPane, Button modeToggleBtn,
            Button accentPurpleBtn, Button accentBlueBtn,
            Button accentPinkBtn, Button accentGreenBtn,
            Button accentOrangeBtn, SettingsService settingsManager) {
        this.rootPane = rootPane;
        this.modeToggleBtn = modeToggleBtn;
        this.accentPurpleBtn = accentPurpleBtn;
        this.accentBlueBtn = accentBlueBtn;
        this.accentPinkBtn = accentPinkBtn;
        this.accentGreenBtn = accentGreenBtn;
        this.accentOrangeBtn = accentOrangeBtn;
        this.settingsManager = settingsManager;
    }

    public void loadSavedTheme() {
        String savedMode = settingsManager.getTheme();
        String savedAccent = settingsManager.getAccentColor();

        isDarkMode = !"light".equals(savedMode);
        currentAccent = (savedAccent != null && !savedAccent.isEmpty()) ? savedAccent : "purple";

        applyTheme();
    }

    public void applyTheme() {
        rootPane.getStyleClass().removeAll("mode-dark", "mode-light",
                "accent-purple", "accent-blue", "accent-pink", "accent-green", "accent-orange");

        rootPane.getStyleClass().add(isDarkMode ? "mode-dark" : "mode-light");
        rootPane.getStyleClass().add("accent-" + currentAccent);

        if (modeToggleBtn != null) {
            modeToggleBtn.setText(isDarkMode ? "🌙" : "☀️");
        }

        updateAccentButtonSelection();

        settingsManager.setTheme(isDarkMode ? "dark" : "light");
        settingsManager.setAccentColor(currentAccent);
    }

    public void updateAccentButtonSelection() {
        if (accentPurpleBtn == null)
            return;

        accentPurpleBtn.getStyleClass().remove("selected");
        accentBlueBtn.getStyleClass().remove("selected");
        accentPinkBtn.getStyleClass().remove("selected");
        accentGreenBtn.getStyleClass().remove("selected");
        accentOrangeBtn.getStyleClass().remove("selected");

        switch (currentAccent) {
            case "purple" -> accentPurpleBtn.getStyleClass().add("selected");
            case "blue" -> accentBlueBtn.getStyleClass().add("selected");
            case "pink" -> accentPinkBtn.getStyleClass().add("selected");
            case "green" -> accentGreenBtn.getStyleClass().add("selected");
            case "orange" -> accentOrangeBtn.getStyleClass().add("selected");
        }
    }

    public void handleModeToggle() {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    public void setAccent(String accent) {
        currentAccent = accent;
        applyTheme();
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public String getCurrentAccent() {
        return currentAccent;
    }
}
