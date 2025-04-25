package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class StudentManager {

    public StudentManager() {
        System.out.println("StudentManager constructor called");

        // Create the TableView for Students
        TableView<Etudiant> table = new TableView<>();

        // Create the columns for the TableView
        TableColumn<Etudiant, Integer> numEtudiantCol = new TableColumn<>("Num Etudiant");
        numEtudiantCol.setCellValueFactory(new PropertyValueFactory<>("numEtudiant"));

        TableColumn<Etudiant, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Etudiant, String> prenomCol = new TableColumn<>("Prenom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Etudiant, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Etudiant, String> telephoneCol = new TableColumn<>("Telephone");
        telephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        // Add columns to the TableView
        table.getColumns().addAll(numEtudiantCol, nomCol, prenomCol, emailCol, telephoneCol);

        // Fetch students from the database
        ObservableList<Etudiant> students = getStudentList();
        table.setItems(students);

        System.out.println("Students fetched: " + students.size()); // Check the number of students

        // Layout for the scene
        VBox layout = new VBox(table);
        layout.setStyle("-fx-padding: 10;");

        // Create and show the scene
        Scene scene = new Scene(layout, 600, 400);
        Stage stage = new Stage();
        stage.setTitle("Liste des Etudiants");
        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<Etudiant> getStudentList() {
        ObservableList<Etudiant> list = FXCollections.observableArrayList();

        try {
            // Database connection
            System.out.println("Connecting to the database...");
            Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "MAHMOUD", "mahmoud");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Etudiant");

            // Add students to the list
            while (rs.next()) {
                System.out.println("Found student: " + rs.getString("NOM") + " " + rs.getString("PRENOM"));
                list.add(new Etudiant(
                        rs.getInt("NUM_ETUDIANT"),
                        rs.getString("NOM"),
                        rs.getString("PRENOM"),
                        rs.getString("EMAIL"),
                        rs.getString("TELEPHONE")
                ));
            }

            rs.close();
            stmt.close();
            conn.close();
            System.out.println("Database connection closed.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
