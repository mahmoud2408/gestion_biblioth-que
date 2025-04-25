package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class EmpruntListWindow extends Stage {

    private TableView<Emprunt> tableView = new TableView<>();
    private ObservableList<Emprunt> emprunts = FXCollections.observableArrayList();
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "MAHMOUD";
    private static final String PASS = "mahmoud";

    public EmpruntListWindow() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label title = new Label("Liste des emprunts");
        setupTableView();

        Button addBtn = new Button("Ajouter un emprunt");
        Button returnBtn = new Button("Emprunt Rendu");

        addBtn.setOnAction(e -> showEmpruntForm());
        returnBtn.setOnAction(e -> handleReturn());

        HBox buttonBox = new HBox(10, addBtn, returnBtn);

        root.getChildren().addAll(title, tableView, buttonBox);
        setScene(new Scene(root, 800, 500));
        setTitle("Gestion des emprunts");
        refreshTable();
    }

    private void setupTableView() {
        TableColumn<Emprunt, Integer> numEmpCol = new TableColumn<>("Num Emprunt");
        numEmpCol.setCellValueFactory(e -> e.getValue().numEmpruntProperty().asObject());

        TableColumn<Emprunt, String> etudiantCol = new TableColumn<>("Étudiant");
        etudiantCol.setCellValueFactory(e -> e.getValue().nomEtudiantProperty());

        TableColumn<Emprunt, String> livreCol = new TableColumn<>("Livre");
        livreCol.setCellValueFactory(e -> e.getValue().titreLivreProperty());

        TableColumn<Emprunt, Date> dateEmpCol = new TableColumn<>("Date Emprunt");
        dateEmpCol.setCellValueFactory(e -> e.getValue().dateEmpruntProperty());

        TableColumn<Emprunt, Date> retourPrevuCol = new TableColumn<>("Retour Prévu");
        retourPrevuCol.setCellValueFactory(e -> e.getValue().dateRetourPrevuProperty());

        TableColumn<Emprunt, Date> retourReelCol = new TableColumn<>("Retour Réel");
        retourReelCol.setCellValueFactory(e -> e.getValue().dateRetourReelProperty());

        TableColumn<Emprunt, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(e -> e.getValue().statutProperty());

        tableView.getColumns().addAll(numEmpCol, etudiantCol, livreCol, dateEmpCol, retourPrevuCol, retourReelCol, statutCol);
        tableView.setItems(emprunts);
    }

    private void refreshTable() {
        emprunts.clear();
        String sql = "SELECT e.NumEmprunt, s.Nom AS Etudiant, l.Titre AS Livre, "
                + "e.DateEmprunt, e.DateRetourPrevu, e.DateRetourReel, e.statut "
                + "FROM emprunts e "
                + "JOIN etudiants s ON e.NumEtudiant = s.NumEtudiant "
                + "JOIN livres l ON e.CodeLivre = l.CodeLivre";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                emprunts.add(new Emprunt(
                        rs.getInt("NumEmprunt"),
                        rs.getString("Etudiant"),
                        rs.getString("Livre"),
                        rs.getDate("DateEmprunt"),
                        rs.getDate("DateRetourPrevu"),
                        rs.getDate("DateRetourReel"),
                        rs.getString("statut")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement: " + e.getMessage()).showAndWait();
        }
    }

    private void handleReturn() {
        Emprunt selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un emprunt !").showAndWait();
            return;
        }

        Stage dateStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        DatePicker dateRetourReelPicker = new DatePicker(LocalDate.now());
        Button confirmBtn = new Button("Confirmer");

        confirmBtn.setOnAction(e -> {
            LocalDate dateRetourReel = dateRetourReelPicker.getValue();
            LocalDate dateEmprunt = selected.dateEmpruntProperty().get().toLocalDate();

            // Validation date retour réel
            if (dateRetourReel.isBefore(dateEmprunt)) {
                new Alert(Alert.AlertType.ERROR,
                        "La date de retour réel ne peut pas être avant la date d'emprunt !").showAndWait();
                return;
            }

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                        // Mise à jour emprunt
                        PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE emprunts SET statut = 'rendu', DateRetourReel = ? WHERE NumEmprunt = ?");
                        stmt.setDate(1, Date.valueOf(dateRetourReel));
                        stmt.setInt(2, selected.numEmpruntProperty().get());
                        stmt.executeUpdate();

                        // Mise à jour quantité livre
                        PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE livres SET QuantiteDisponible = QuantiteDisponible + 1 " +
                                        "WHERE CodeLivre = (SELECT CodeLivre FROM emprunts WHERE NumEmprunt = ?)");
                        updateStmt.setInt(1, selected.numEmpruntProperty().get());
                        updateStmt.executeUpdate();
                    }
                    return null;
                }
            };

            task.setOnSucceeded(ev -> {
                dateStage.close();
                refreshTable();
                new Alert(Alert.AlertType.INFORMATION, "Retour enregistré avec succès !").showAndWait();
            });

            task.setOnFailed(ev -> {
                new Alert(Alert.AlertType.ERROR, "Erreur: " + task.getException().getMessage()).showAndWait();
            });

            new Thread(task).start();
        });

        form.getChildren().addAll(
                new Label("Date de retour réel:"),
                dateRetourReelPicker,
                confirmBtn
        );

        dateStage.setScene(new Scene(form));
        dateStage.setTitle("Enregistrement du retour");
        dateStage.show();
    }

    private void showEmpruntForm() {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        ComboBox<String> etudiantBox = new ComboBox<>();
        ComboBox<String> livreBox = new ComboBox<>();
        ObservableList<String> statutOptions = FXCollections.observableArrayList("emprunté", "rendu");
        ComboBox<String> statutBox = new ComboBox<>(statutOptions);
        statutBox.setValue("emprunté");

        DatePicker dateEmpruntPicker = new DatePicker(LocalDate.now());
        DatePicker dateRetourPrevuPicker = new DatePicker(LocalDate.now().plusDays(14));

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT Nom FROM etudiants");
            while (rs1.next()) etudiantBox.getItems().add(rs1.getString("Nom"));

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT Titre FROM livres");
            while (rs2.next()) livreBox.getItems().add(rs2.getString("Titre"));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Button saveBtn = new Button("Enregistrer");
        saveBtn.setOnAction(e -> {
            // Validation des dates
            LocalDate dateEmp = dateEmpruntPicker.getValue();
            LocalDate dateRetourPrevu = dateRetourPrevuPicker.getValue();

            if (dateRetourPrevu.isBefore(dateEmp)) {
                new Alert(Alert.AlertType.ERROR,
                        "La date de retour prévu ne peut pas être avant la date d'emprunt !").showAndWait();
                return;
            }

            // ... (reste du code d'enregistrement) ...
        });

        form.getChildren().addAll(
                new Label("Étudiant"), etudiantBox,
                new Label("Livre"), livreBox,
                new Label("Date Emprunt"), dateEmpruntPicker,
                new Label("Date Retour Prévu"), dateRetourPrevuPicker,
                new Label("Statut"), statutBox,
                saveBtn
        );

        formStage.setScene(new Scene(form));
        formStage.setTitle("Nouvel Emprunt");
        formStage.show();
    }
}