module app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;            // pour JDBC

    opens app to javafx.fxml;
    exports app;
}
