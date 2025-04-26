package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class StudentManager {

    private TableView<Etudiant> table = new TableView<>();
    private ObservableList<Etudiant> studentList = FXCollections.observableArrayList();

    public StudentManager() {
        System.out.println("StudentManager constructor called");

        setupTable();
        fetchStudentsAsync();

        VBox layout = new VBox(table);
        layout.setStyle("-fx-padding: 10;");

        Scene scene = new Scene(layout, 600, 400);
        Stage stage = new Stage();
        stage.setTitle("Liste des Etudiants");
        stage.setScene(scene);
        stage.show();
    }

    private void setupTable() {
        TableColumn<Etudiant, Integer> numEtudiantCol = new TableColumn<>("Num Etudiant");
        numEtudiantCol.setCellValueFactory(new PropertyValueFactory<>("NumEtudiant"));

        TableColumn<Etudiant, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Etudiant, String> prenomCol = new TableColumn<>("Prenom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Etudiant, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Etudiant, String> telephoneCol = new TableColumn<>("Telephone");
        telephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        table.getColumns().addAll(numEtudiantCol, nomCol, prenomCol, emailCol, telephoneCol);
        table.setItems(studentList);
    }

    private void fetchStudentsAsync() {
        Task<ObservableList<Etudiant>> task = new Task<>() {
            @Override
            protected ObservableList<Etudiant> call() throws Exception {
                ObservableList<Etudiant> list = FXCollections.observableArrayList();
                try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "MAHMOUD", "mahmoud");
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM etudiants")) {

                    while (rs.next()) {
                        list.add(new Etudiant(
                                rs.getInt("NumEtudiant"),
                                rs.getString("nom"),
                                rs.getString("prenom"),
                                rs.getString("email"),
                                rs.getString("telephone")
                        ));
                    }
                }
                return list;
            }
        };

        task.setOnSucceeded(e -> {
            studentList.setAll(task.getValue());
            System.out.println("Students fetched: " + studentList.size());
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }
}
