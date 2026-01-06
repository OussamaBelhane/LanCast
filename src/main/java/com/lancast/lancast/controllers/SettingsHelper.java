package com.lancast.lancast.controllers;

import com.lancast.lancast.services.SettingsService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Helper class for settings management (PIN, force zip).
 */
public class SettingsHelper {

    private final Label pinLabel;
    private final Label settingsPinLabel;
    private final Button forceZipToggleBtn;
    private final SettingsService settingsManager;

    public SettingsHelper(Label pinLabel, Label settingsPinLabel,
            Button forceZipToggleBtn, SettingsService settingsManager) {
        this.pinLabel = pinLabel;
        this.settingsPinLabel = settingsPinLabel;
        this.forceZipToggleBtn = forceZipToggleBtn;
        this.settingsManager = settingsManager;
    }

    public void updatePinDisplay() {
        String pin = settingsManager.getPin();
        if (pinLabel != null)
            pinLabel.setText(pin);
        if (settingsPinLabel != null)
            settingsPinLabel.setText(pin);
    }

    public void handleRefreshPin() {
        int randomPin = (int) (Math.random() * 9000) + 1000;
        String newPin = String.valueOf(randomPin);
        settingsManager.setPin(newPin);
        updatePinDisplay();
    }

    public void updateForceZipDisplay() {
        boolean forceZip = settingsManager.getForceZip();
        if (forceZipToggleBtn != null) {
            forceZipToggleBtn.setText(forceZip ? "ON" : "OFF");
        }
    }

    public void handleForceZipToggle() {
        boolean currentState = settingsManager.getForceZip();
        settingsManager.setForceZip(!currentState);
        updateForceZipDisplay();
    }

    public String getPin() {
        return settingsManager.getPin();
    }
}
