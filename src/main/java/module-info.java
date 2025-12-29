module com.lancast.lancast {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires jdk.httpserver;
    requires java.sql;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires java.desktop;

    opens com.lancast.lancast to javafx.fxml;
    opens com.lancast.lancast.controller to javafx.fxml;
    opens com.lancast.lancast.model to javafx.base, javafx.fxml;

    exports com.lancast.lancast;
    exports com.lancast.lancast.controller;
    exports com.lancast.lancast.model;
    exports com.lancast.lancast.service;
}