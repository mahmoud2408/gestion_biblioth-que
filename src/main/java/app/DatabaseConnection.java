package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:oracle:thin:@localhost:1521:xe"; // change if needed
        String user = "MAHMOUD"; // replace with your Oracle username
        String password = "mahmoud"; // replace with your Oracle password

        return DriverManager.getConnection(url, user, password);
    }
}
