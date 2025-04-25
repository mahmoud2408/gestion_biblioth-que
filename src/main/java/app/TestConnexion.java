package app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnexion {
    public static void main(String[] args) {
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe", "MAHMOUD", "mahmoud"
            );
            System.out.println("✅ Connexion réussie !");
            con.close();
        } catch (SQLException e) {
            System.out.println("❌ Erreur de connexion :");
            e.printStackTrace();
        }
    }
}
