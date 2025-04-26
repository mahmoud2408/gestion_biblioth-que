package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class StudentListWindow extends Stage {

    private TableView<Etudiant> tableView = new TableView<>();
    private ObservableList<Etudiant> studentList = FXCollections.observableArrayList();
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "MAHMOUD";
    private static final String PASS = "mahmoud";

    public StudentListWindow() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Gestion des Étudiants");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un étudiant...");
        searchField.setStyle("-fx-pref-width: 300px; -fx-padding: 8px;");

        header.getChildren().addAll(title, searchField);

        // Table Configuration
        setupTableView();
        tableView.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = createStyledButton("Ajouter", "#27ae60");
        Button editButton = createStyledButton("Modifier", "#f1c40f");
        Button deleteButton = createStyledButton("Supprimer", "#e74c3c");

        addButton.setOnAction(e -> showStudentForm(null));
        editButton.setOnAction(e -> {
            Etudiant selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) showStudentForm(selected);
        });
        deleteButton.setOnAction(e -> deleteStudent());

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        // Search Functionality
        FilteredList<Etudiant> filteredData = new FilteredList<>(studentList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) ->
                filteredData.setPredicate(etudiant ->
                        etudiant.nomProperty().get().toLowerCase().contains(newVal.toLowerCase())
                )
        );
        tableView.setItems(filteredData);

        root.getChildren().addAll(header, tableView, buttonBox);

        Scene scene = new Scene(root, 1000, 700);
        this.setScene(scene);
        this.setTitle("Gestion des Étudiants");
        loadStudentsAsync();
    }

    private void setupTableView() {
        TableColumn<Etudiant, Integer> numCol = new TableColumn<>("Numéro");
        numCol.setCellValueFactory(new PropertyValueFactory<>("numEtudiant")); // camelCase

        TableColumn<Etudiant, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Etudiant, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Etudiant, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Etudiant, String> telCol = new TableColumn<>("Téléphone");
        telCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        tableView.getColumns().addAll(numCol, nomCol, prenomCol, emailCol, telCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        return btn;
    }

    private void loadStudentsAsync() {
        Task<ObservableList<Etudiant>> task = new Task<>() {
            @Override
            protected ObservableList<Etudiant> call() throws Exception {
                ObservableList<Etudiant> list = FXCollections.observableArrayList();
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement("SELECT NUMETUDIANT, NOM, PRENOM, EMAIL, TELEPHONE FROM etudiants");
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        list.add(new Etudiant(
                                rs.getInt("NUMETUDIANT"),
                                rs.getString("NOM"),
                                rs.getString("PRENOM"),
                                rs.getString("EMAIL"),
                                rs.getString("TELEPHONE")
                        ));
                    }
                }
                return list;
            }
        };

        task.setOnSucceeded(e -> {
            studentList.setAll(task.getValue());
            tableView.setItems(studentList);
        });
        task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Erreur: " + task.getException().getMessage()).showAndWait());
        new Thread(task).start();
    }

    private void showStudentForm(Etudiant etudiant) {
        Stage formStage = new Stage();
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 8;");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField telField = new TextField();
        telField.setPromptText("Téléphone");

        if (etudiant != null) {
            nomField.setText(etudiant.nomProperty().get());
            prenomField.setText(etudiant.prenomProperty().get());
            emailField.setText(etudiant.emailProperty().get());
            telField.setText(etudiant.telephoneProperty().get());
        }

        Button saveButton = createStyledButton("Enregistrer", "#27ae60");
        saveButton.setOnAction(e -> {
            if (validateFields(nomField, prenomField)) {
                saveStudent(
                        etudiant == null ? 0 : etudiant.numEtudiantProperty().get(),
                        nomField.getText(),
                        prenomField.getText(),
                        emailField.getText(),
                        telField.getText()
                );
                formStage.close();
            }
        });

        form.getChildren().addAll(
                new Label("Formulaire Étudiant"),
                nomField, prenomField,
                emailField, telField,
                saveButton
        );

        formStage.setScene(new Scene(form, 400, 400));
        formStage.show();
    }

    private boolean validateFields(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Les champs Nom et Prénom sont obligatoires !").showAndWait();
                return false;
            }
        }
        return true;
    }

    private void saveStudent(int num, String nom, String prenom, String email, String tel) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String query = num == 0 ?
                        "INSERT INTO etudiants VALUES (SEQ_ETUDIANTS.NEXTVAL, ?, ?, ?, ?)" :
                        "UPDATE etudiants SET NOM=?, PRENOM=?, EMAIL=?, TELEPHONE=? WHERE NUMETUDIANT=?";

                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    if (num == 0) {
                        stmt.setString(1, nom);
                        stmt.setString(2, prenom);
                        stmt.setString(3, email);
                        stmt.setString(4, tel);
                    } else {
                        stmt.setString(1, nom);
                        stmt.setString(2, prenom);
                        stmt.setString(3, email);
                        stmt.setString(4, tel);
                        stmt.setInt(5, num);
                    }
                    stmt.executeUpdate();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> loadStudentsAsync());
        task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Erreur: " + task.getException().getMessage()).showAndWait());
        new Thread(task).start();
    }

    private void deleteStudent() {
        Etudiant selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cet étudiant ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                             PreparedStatement stmt = conn.prepareStatement(
                                     "DELETE FROM etudiants WHERE NUMETUDIANT = ?")) {
                            stmt.setInt(1, selected.numEtudiantProperty().get());
                            stmt.executeUpdate();
                        }
                        return null;
                    }
                };
                task.setOnSucceeded(e -> loadStudentsAsync());
                new Thread(task).start();
            }
        });
    }
}