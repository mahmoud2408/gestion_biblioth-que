package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class StudentListWindow extends Stage {

    private TableView<Etudiant> tableView;
    private ObservableList<Etudiant> studentList;

    public StudentListWindow() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("Liste des étudiants");

        tableView = new TableView<>();
        setupTableView();

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

        Scene scene = new Scene(root, 700, 400);
        setTitle("Gestion des étudiants");
        setScene(scene);

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
        studentList = FXCollections.observableArrayList();
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "MAHMOUD";
        String password = "mahmoud";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM ETUDIANT")) {

            while (rs.next()) {
                studentList.add(new Etudiant(
                        rs.getInt("NUM_ETUDIANT"),
                        rs.getString("NOM"),
                        rs.getString("PRENOM"),
                        rs.getString("EMAIL"),
                        rs.getString("TELEPHONE")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableView.setItems(studentList);
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
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String email = emailField.getText();
            String tel = telField.getText();

            String url = "jdbc:oracle:thin:@localhost:1521:xe";
            String user = "MAHMOUD";
            String password = "mahmoud";
            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                PreparedStatement stmt;
                if (etudiant == null) {
                    stmt = conn.prepareStatement("INSERT INTO ETUDIANT VALUES (SEQ_ETUDIANT.NEXTVAL, ?, ?, ?, ?)");
                    stmt.setString(1, nom);
                    stmt.setString(2, prenom);
                    stmt.setString(3, email);
                    stmt.setString(4, tel);
                } else {
                    stmt = conn.prepareStatement("UPDATE ETUDIANT SET NOM=?, PRENOM=?, EMAIL=?, TELEPHONE=? WHERE NUM_ETUDIANT=?");
                    stmt.setString(1, nom);
                    stmt.setString(2, prenom);
                    stmt.setString(3, email);
                    stmt.setString(4, tel);
                    stmt.setInt(5, etudiant.getNumEtudiant());
                }
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            formStage.close();
            refreshTable();
        });

        form.getChildren().addAll(nomField, prenomField, emailField, telField, saveButton);
        formStage.setScene(new Scene(form, 300, 300));
        formStage.setTitle(etudiant == null ? "Ajouter un étudiant" : "Modifier l'étudiant");
        formStage.show();
    }

    private void deleteStudent(Etudiant etudiant) {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String user = "MAHMOUD";
        String password = "mahmoud";
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM ETUDIANT WHERE NUM_ETUDIANT=?");
            stmt.setInt(1, etudiant.getNumEtudiant());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        refreshTable();
    }
}
