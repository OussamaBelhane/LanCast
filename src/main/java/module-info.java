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
    opens com.lancast.lancast.controllers to javafx.fxml;
    opens com.lancast.lancast.models to javafx.base, javafx.fxml;

    exports com.lancast.lancast;
    exports com.lancast.lancast.controllers;
    exports com.lancast.lancast.models;
    exports com.lancast.lancast.services;
    exports com.lancast.lancast.dao;
}