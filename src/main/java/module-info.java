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
    opens com.lancast.lancast.database to javafx.base;

    exports com.lancast.lancast;
}