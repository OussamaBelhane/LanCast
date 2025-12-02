module com.lancast.lancast {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;
    requires java.sql;

    opens com.lancast.lancast to javafx.fxml;
    opens com.lancast.lancast.database to javafx.base;

    exports com.lancast.lancast;
}