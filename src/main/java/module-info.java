module app {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;            // pour JDBC

    opens app to javafx.fxml;
    exports app;
}
