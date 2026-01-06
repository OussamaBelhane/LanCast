package com.lancast.lancast.controller;

import com.lancast.lancast.model.TransferLog;
import com.lancast.lancast.service.HistoryService;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Helper class for history table management.
 */
public class HistoryHelper {

    private final TableView<TransferLog> historyTable;
    private final TableColumn<TransferLog, String> timeCol;
    private final TableColumn<TransferLog, String> deviceCol;
    private final TableColumn<TransferLog, String> fileCol;
    private final TableColumn<TransferLog, String> ipCol;

    public HistoryHelper(TableView<TransferLog> historyTable,
            TableColumn<TransferLog, String> timeCol,
            TableColumn<TransferLog, String> deviceCol,
            TableColumn<TransferLog, String> fileCol,
            TableColumn<TransferLog, String> ipCol) {
        this.historyTable = historyTable;
        this.timeCol = timeCol;
        this.deviceCol = deviceCol;
        this.fileCol = fileCol;
        this.ipCol = ipCol;
    }

    public void setupHistoryTable() {
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        deviceCol.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        fileCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        ipCol.setCellValueFactory(new PropertyValueFactory<>("clientIp"));
    }

    public void loadHistoryData() {
        HistoryService hm = new HistoryService();
        List<TransferLog> logs = hm.getAllLogs();
        historyTable.setItems(FXCollections.observableArrayList(logs));
    }
}
