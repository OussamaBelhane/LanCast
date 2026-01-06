package com.lancast.lancast.controller;

import com.lancast.lancast.service.LanCastService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

/**
 * Helper class for file selection and drag-and-drop functionality.
 */
public class FileHelper {

    private final StackPane dropZone;
    private final ListView<File> fileListView;
    private final Label statusLabel;
    private final Label fileCountLabel;
    private final ObservableList<File> selectedFiles;

    public FileHelper(StackPane dropZone, ListView<File> fileListView,
            Label statusLabel, Label fileCountLabel) {
        this.dropZone = dropZone;
        this.fileListView = fileListView;
        this.statusLabel = statusLabel;
        this.fileCountLabel = fileCountLabel;
        this.selectedFiles = FXCollections.observableArrayList();
    }

    public void initialize() {
        fileListView.setItems(selectedFiles);
        setupFileListCellFactory();
        setupDragAndDrop();
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

    private void setupFileListCellFactory() {
        fileListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox();
                    hbox.setSpacing(8);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    Label iconLabel = new Label("📄");
                    iconLabel.setStyle("-fx-font-size: 14px;");

                    Label nameLabel = new Label(item.getName());
                    nameLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: -text-primary; -fx-font-size: 11px;");

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
                        LanCastService.removeFile(item);
                        updateStatus();
                    });

                    hbox.getChildren().addAll(iconLabel, nameLabel, sizeLabel, spacer, removeBtn);
                    setGraphic(hbox);
                }
            }
        });
    }

    public void addFiles(List<File> files) {
        for (File file : files) {
            if (!selectedFiles.contains(file)) {
                selectedFiles.add(file);
                LanCastService.addFile(file);
            }
        }
        updateStatus();
    }

    public void handleBrowseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files");
        List<File> files = fileChooser.showOpenMultipleDialog(dropZone.getScene().getWindow());
        if (files != null) {
            addFiles(files);
        }
    }

    public void handleClear() {
        selectedFiles.clear();
        LanCastService.resetSession();
        updateStatus();
    }

    public void updateStatus() {
        int count = selectedFiles.size();
        if (statusLabel != null)
            statusLabel.setText(String.valueOf(count));
        if (fileCountLabel != null)
            fileCountLabel.setText(String.valueOf(count));
    }

    public int getFileCount() {
        return selectedFiles.size();
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
