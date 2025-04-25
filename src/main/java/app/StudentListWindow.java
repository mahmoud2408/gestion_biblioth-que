package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;

public class StudentListWindow extends Stage {

    private TableView<Etudiant> tableView;
    private ObservableList<Etudiant> studentList = FXCollections.observableArrayList();
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "MAHMOUD";
    private static final String PASS = "mahmoud";

    public StudentListWindow() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("Liste des étudiants");

        tableView = new TableView<>();
        setupTableView();
        tableView.setItems(studentList);

        Button addButton = new Button("Ajouter");
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);

        root.getChildren().addAll(titleLabel, tableView, buttonBox);

        addButton.setOnAction(e -> showStudentForm(null));
        editButton.setOnAction(e -> {
            Etudiant selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) showStudentForm(selected);
        });
        deleteButton.setOnAction(e -> {
            Etudiant selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) deleteStudent(selected);
        });

        setScene(new Scene(root, 700, 400));
        setTitle("Gestion des étudiants");

        refreshTable();
    }

    private void setupTableView() {
        TableColumn<Etudiant, Integer> numCol = new TableColumn<>("Numéro");
        numCol.setCellValueFactory(new PropertyValueFactory<>("numEtudiant"));

        TableColumn<Etudiant, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Etudiant, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Etudiant, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Etudiant, String> telCol = new TableColumn<>("Téléphone");
        telCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        tableView.getColumns().addAll(numCol, nomCol, prenomCol, emailCol, telCol);
    }

    private void refreshTable() {
        Task<ObservableList<Etudiant>> task = new Task<>() {
            @Override
            protected ObservableList<Etudiant> call() throws Exception {
                ObservableList<Etudiant> list = FXCollections.observableArrayList();
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
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
        task.setOnSucceeded(e -> studentList.setAll(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    private void showStudentForm(Etudiant etudiant) {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField telField = new TextField();
        telField.setPromptText("Téléphone");

        if (etudiant != null) {
            nomField.setText(etudiant.getNom());
            prenomField.setText(etudiant.getPrenom());
            emailField.setText(etudiant.getEmail());
            telField.setText(etudiant.getTelephone());
        }

        Button saveButton = new Button("Enregistrer");
        saveButton.setOnAction(e -> {
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                        PreparedStatement stmt;
                        if (etudiant == null) {
                            stmt = conn.prepareStatement("INSERT INTO etudiants VALUES (SEQ_etudiants.NEXTVAL, ?, ?, ?, ?)");
                            stmt.setString(1, nomField.getText());
                            stmt.setString(2, prenomField.getText());
                            stmt.setString(3, emailField.getText());
                            stmt.setString(4, telField.getText());
                        } else {
                            stmt = conn.prepareStatement("UPDATE etudiants SET nom=?, prenom=?, email=?, telephone=? WHERE NumEtudiant=?");
                            stmt.setString(1, nomField.getText());
                            stmt.setString(2, prenomField.getText());
                            stmt.setString(3, emailField.getText());
                            stmt.setString(4, telField.getText());
                            stmt.setInt(5, etudiant.getNumEtudiant());
                        }
                        stmt.executeUpdate();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                formStage.close();
                refreshTable();
            });
            task.setOnFailed(ev -> task.getException().printStackTrace());
            new Thread(task).start();
        });

        form.getChildren().addAll(nomField, prenomField, emailField, telField, saveButton);
        formStage.setScene(new Scene(form, 300, 300));
        formStage.setTitle(etudiant == null ? "Ajouter un étudiant" : "Modifier l'étudiant");
        formStage.show();
    }

    private void deleteStudent(Etudiant etudiant) {
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM etudiants WHERE NumEtudiant=?")) {
                    stmt.setInt(1, etudiant.getNumEtudiant());
                    return stmt.executeUpdate();
                }
            }
        };
        task.setOnSucceeded(e -> {
            if (task.getValue() > 0) {
                studentList.remove(etudiant);
            } else {
                new Alert(Alert.AlertType.WARNING, "Aucun étudiant supprimé.").showAndWait();
            }
        });
        task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR,
                "Erreur suppression : " + task.getException().getMessage()).showAndWait());
        new Thread(task).start();
    }
}
