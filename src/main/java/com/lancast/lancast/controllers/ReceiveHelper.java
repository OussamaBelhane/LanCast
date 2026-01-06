package com.lancast.lancast.controllers;

import com.lancast.lancast.models.PendingUpload;
import com.lancast.lancast.services.LanCastService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

import java.io.File;
import java.util.List;

/**
 * Helper class for managing received files and pending uploads.
 */
public class ReceiveHelper {

    private final ListView<String> receivedFilesListView;
    private final Label receivedCountLabel;

    public ReceiveHelper(ListView<String> receivedFilesListView, Label receivedCountLabel) {
        this.receivedFilesListView = receivedFilesListView;
        this.receivedCountLabel = receivedCountLabel;
    }

    public void loadReceivedFiles() {
        List<File> received = LanCastService.getReceivedFiles();
        List<PendingUpload> pending = LanCastService.getPendingUploads();

        ObservableList<String> displayList = FXCollections.observableArrayList();
        for (PendingUpload p : pending) {
            displayList.add("PENDING|" + p.getFileName());
        }
        for (File f : received) {
            displayList.add("DOWNLOADED|" + f.getName());
        }

        receivedFilesListView.setItems(displayList);
        receivedCountLabel.setText((pending.size() + received.size()) + " files");

        receivedFilesListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String[] parts = item.split("\\|", 2);
                    String status = parts[0];
                    String fileName = parts[1];

                    HBox hbox = new HBox(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    Label icon;
                    Label name;

                    if ("PENDING".equals(status)) {
                        icon = new Label("⏳");
                        icon.setStyle("-fx-font-size: 14px;");
                        name = new Label(fileName);
                        name.setStyle("-fx-font-size: 11px; -fx-text-fill: -accent-color; -fx-font-weight: bold;");

                        Button downloadBtn = new Button("📥");
                        downloadBtn.setStyle("-fx-background-color: -accent-color; -fx-text-fill: white; " +
                                "-fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 4 8;");
                        downloadBtn.setOnAction(e -> {
                            if (LanCastService.acceptPendingUpload(fileName)) {
                                loadReceivedFiles();
                            }
                        });

                        hbox.getChildren().addAll(icon, name, downloadBtn);
                    } else {
                        icon = new Label("✅");
                        icon.setStyle("-fx-font-size: 14px;");
                        name = new Label(fileName);
                        name.setStyle("-fx-font-size: 11px; -fx-text-fill: -text-primary;");
                        hbox.getChildren().addAll(icon, name);
                    }

                    setGraphic(hbox);
                }
            }
        });
    }

    public void handleRefreshReceived() {
        loadReceivedFiles();
    }

    public void handleOpenUploadsFolder() {
        try {
            java.awt.Desktop.getDesktop().open(new File("uploads"));
        } catch (Exception e) {
            System.err.println("Could not open folder: " + e.getMessage());
        }
    }
}
