package com.lancast.lancast.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class SettingsManager {
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String KEY_PIN = "security_pin";
    private static final String DEFAULT_PIN = "1234";

    private Properties properties;

    public SettingsManager() {
        properties = new Properties();
        loadSettings();
    }

    private void loadSettings() {
        try (FileInputStream in = new FileInputStream(SETTINGS_FILE)) {
            properties.load(in);
        } catch (IOException e) {
            // File might not exist yet, ignore
        }
    }

    public void saveSettings() {
        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(out, "LanCast Settings");
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    public String getPin() {
        return properties.getProperty(KEY_PIN, DEFAULT_PIN);
    }

    public void setPin(String pin) {
        properties.setProperty(KEY_PIN, pin);
        saveSettings();
    }
}
