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
import java.time.LocalDate;

public class EmpruntListWindow extends Stage {

    private TableView<Emprunt> tableView = new TableView<>();
    private ObservableList<Emprunt> empruntList = FXCollections.observableArrayList();
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "MAHMOUD";
    private static final String PASS = "mahmoud";

    public EmpruntListWindow() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Gestion des Emprunts");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un emprunt...");
        searchField.setStyle("-fx-pref-width: 300px; -fx-padding: 8px;");

        header.getChildren().addAll(title, searchField);

        // Table Configuration
        setupTableView();
        tableView.setStyle("-fx-border-radius: 8; -fx-background-radius: 8;");

        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = createStyledButton("Ajouter", "#27ae60");
        Button returnButton = createStyledButton("Marquer comme rendu", "#f1c40f");

        addButton.setOnAction(e -> showEmpruntForm());
        returnButton.setOnAction(e -> handleReturn());

        buttonBox.getChildren().addAll(addButton, returnButton);

        // Search Functionality
        // Dans le constructeur, remplacer la partie "Search Functionality" par :
        FilteredList<Emprunt> filteredData = new FilteredList<>(empruntList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(emprunt -> {
                // Si le champ est vide, affiche tous les éléments
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newVal.toLowerCase();

                // Vérification null-safe pour nomEtudiant
                if (emprunt.nomEtudiantProperty().get() != null &&
                        emprunt.nomEtudiantProperty().get().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                // Vérification null-safe pour titreLivre
                if (emprunt.titreLivreProperty().get() != null &&
                        emprunt.titreLivreProperty().get().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                if (emprunt.dateEmpruntProperty().get()!=null && emprunt.dateEmpruntProperty().get().toLocalDate().toString().contains(lowerCaseFilter)){return true;}

                if (emprunt.dateRetourPrevuProperty().get()!=null && emprunt.dateRetourPrevuProperty().get().toLocalDate().toString().contains(lowerCaseFilter)){return true;}

                if (emprunt.statutProperty().get()!=null && emprunt.statutProperty().get().toLowerCase().contains(lowerCaseFilter)){return true;}

                if (emprunt.numEmpruntProperty().asObject().get() != null && String.valueOf(emprunt.numEmpruntProperty().get()).contains(lowerCaseFilter)) { return true; }

                return false; // Aucune correspondance
            });
        });

// Lier la liste filtrée à la TableView
        tableView.setItems(filteredData);
        root.getChildren().addAll(header, tableView, buttonBox);

        Scene scene = new Scene(root, 1200, 800);
        this.setScene(scene);
        this.setTitle("Gestion des Emprunts");
        loadEmpruntsAsync();
    }

    private void setupTableView() {
        TableColumn<Emprunt, Integer> numCol = new TableColumn<>("Numéro");
        numCol.setCellValueFactory(new PropertyValueFactory<>("numEmprunt"));

        TableColumn<Emprunt, String> etudiantCol = new TableColumn<>("Étudiant");
        etudiantCol.setCellValueFactory(new PropertyValueFactory<>("nomEtudiant"));

        TableColumn<Emprunt, String> livreCol = new TableColumn<>("Livre");
        livreCol.setCellValueFactory(new PropertyValueFactory<>("titreLivre"));

        TableColumn<Emprunt, Date> dateEmpCol = new TableColumn<>("Date Emprunt");
        dateEmpCol.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));

        TableColumn<Emprunt, Date> retourPrevuCol = new TableColumn<>("Retour Prévu");
        retourPrevuCol.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevu"));

        TableColumn<Emprunt, Date> retourReelCol = new TableColumn<>("Retour Réel");
        retourReelCol.setCellValueFactory(new PropertyValueFactory<>("dateRetourReel"));

        TableColumn<Emprunt, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        tableView.getColumns().addAll(numCol, etudiantCol, livreCol, dateEmpCol, retourPrevuCol, retourReelCol, statutCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        return btn;
    }

    private void loadEmpruntsAsync() {
        Task<ObservableList<Emprunt>> task = new Task<>() {
            @Override
            protected ObservableList<Emprunt> call() throws Exception {
                ObservableList<Emprunt> list = FXCollections.observableArrayList();
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement(
                             "SELECT e.*, s.NOM, l.TITRE FROM emprunts e " +
                                     "JOIN etudiants s ON e.NUMETUDIANT = s.NUMETUDIANT " +
                                     "JOIN livres l ON e.CODELIVRE = l.CODELIVRE");
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        list.add(new Emprunt(
                                rs.getInt("NUMEMPRUNT"),
                                rs.getString("NOM"),
                                rs.getString("TITRE"),
                                rs.getDate("DATEEMPRUNT"),
                                rs.getDate("DATERETOURPREVU"),
                                rs.getDate("DATERETOURREEL"),
                                rs.getString("STATUT")
                        ));
                    }
                }
                return list;
            }
        };

        task.setOnSucceeded(e -> {
            empruntList.setAll(task.getValue());
            tableView.refresh();

        });
        task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Erreur: " + task.getException().getMessage()).showAndWait());
        new Thread(task).start();
    }

    private void showEmpruntForm() {
        Stage formStage = new Stage();
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 8;");

        ComboBox<String> etudiantBox = new ComboBox<>();
        ComboBox<String> livreBox = new ComboBox<>();
        DatePicker dateEmpruntPicker = new DatePicker(LocalDate.now());
        DatePicker dateRetourPicker = new DatePicker(LocalDate.now().plusDays(14));

        // Charger les étudiants et livres
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT NOM FROM etudiants");
            while (rs.next()) etudiantBox.getItems().add(rs.getString("NOM"));

            rs = conn.createStatement().executeQuery("SELECT TITRE FROM livres WHERE QUANTITEDISPONIBLE > 0");
            while (rs.next()) livreBox.getItems().add(rs.getString("TITRE"));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur de chargement: " + e.getMessage()).showAndWait();
        }

        Button saveButton = createStyledButton("Enregistrer", "#27ae60");
        saveButton.setOnAction(e -> {
            if (validateForm(etudiantBox, livreBox) && validateDates(dateEmpruntPicker, dateRetourPicker)) {
                saveEmprunt(
                        etudiantBox.getValue(),
                        livreBox.getValue(),
                        dateEmpruntPicker.getValue(),
                        dateRetourPicker.getValue()
                );
                formStage.close();
            }
        });

        form.getChildren().addAll(
                new Label("Nouvel Emprunt"),
                new Label("Étudiant"), etudiantBox,
                new Label("Livre"), livreBox,
                new Label("Date Emprunt"), dateEmpruntPicker,
                new Label("Date Retour Prévu"), dateRetourPicker,
                saveButton
        );

        formStage.setScene(new Scene(form, 400, 500));
        formStage.show();
    }

    private boolean validateForm(ComboBox<?>... fields) {
        for (ComboBox<?> field : fields) {
            if (field.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Tous les champs doivent être sélectionnés !").showAndWait();
                return false;
            }
        }
        return true;
    }

    private boolean validateDates(DatePicker... pickers) {
        for (DatePicker picker : pickers) {
            if (picker.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Toutes les dates doivent être renseignées !").showAndWait();
                return false;
            }
        }
        return true;
    }

    private void saveEmprunt(String etudiant, String livre, LocalDate dateEmp, LocalDate dateRetour) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                    // Récupération NumEtudiant
                    PreparedStatement stmt = conn.prepareStatement("SELECT NUMETUDIANT FROM etudiants WHERE NOM = ?");
                    stmt.setString(1, etudiant);
                    ResultSet rs = stmt.executeQuery();
                    int numEtudiant = rs.next() ? rs.getInt("NUMETUDIANT") : 0;

                    // Récupération CodeLivre
                    stmt = conn.prepareStatement("SELECT CODELIVRE FROM livres WHERE TITRE = ?");
                    stmt.setString(1, livre);
                    rs = stmt.executeQuery();
                    int codeLivre = rs.next() ? rs.getInt("CODELIVRE") : 0;

                    // Insertion emprunt
                    stmt = conn.prepareStatement(
                            "INSERT INTO emprunts (NUMEMPRUNT, NUMETUDIANT, CODELIVRE, DATEEMPRUNT, DATERETOURPREVU, STATUT) " +
                                    "VALUES (seq_emprunts.NEXTVAL, ?, ?, ?, ?, 'emprunté')");
                    stmt.setInt(1, numEtudiant);
                    stmt.setInt(2, codeLivre);
                    stmt.setDate(3, Date.valueOf(dateEmp));
                    stmt.setDate(4, Date.valueOf(dateRetour));
                    stmt.executeUpdate();

                    // Mise à jour quantité livre
                    stmt = conn.prepareStatement("UPDATE livres SET QUANTITEDISPONIBLE = QUANTITEDISPONIBLE - 1 WHERE CODELIVRE = ?");
                    stmt.setInt(1, codeLivre);
                    stmt.executeUpdate();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> loadEmpruntsAsync());
        task.setOnFailed(e -> new Alert(Alert.AlertType.ERROR, "Erreur: " + task.getException().getMessage()).showAndWait());
        new Thread(task).start();
    }

    private void handleReturn() {
        Emprunt selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Sélectionnez un emprunt !").showAndWait();
            return;
        }

        Stage dateStage = new Stage();
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 8;");

        DatePicker dateRetourReelPicker = new DatePicker(LocalDate.now());
        Button confirmButton = createStyledButton("Confirmer", "#27ae60");

        confirmButton.setOnAction(e -> {
            LocalDate dateRetour = dateRetourReelPicker.getValue();
            if (dateRetour.isBefore(selected.dateEmpruntProperty().get().toLocalDate())) {
                new Alert(Alert.AlertType.ERROR, "Date de retour invalide !").showAndWait();
                return;
            }

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                        // Mise à jour emprunt
                        PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE emprunts SET STATUT = 'rendu', DATERETOURREEL = ? WHERE NUMEMPRUNT = ?");
                        stmt.setDate(1, Date.valueOf(dateRetour));
                        stmt.setInt(2, selected.numEmpruntProperty().get());
                        stmt.executeUpdate();

                        // Mise à jour stock livre
                        stmt = conn.prepareStatement(
                                "UPDATE livres SET QUANTITEDISPONIBLE = QUANTITEDISPONIBLE + 1 " +
                                        "WHERE CODELIVRE = (SELECT CODELIVRE FROM emprunts WHERE NUMEMPRUNT = ?)");
                        stmt.setInt(1, selected.numEmpruntProperty().get());
                        stmt.executeUpdate();
                    }
                    return null;
                }
            };

            task.setOnSucceeded(ev -> {
                dateStage.close();
                loadEmpruntsAsync();
                new Alert(Alert.AlertType.INFORMATION, "Retour enregistré !").showAndWait();
            });
            task.setOnFailed(ev -> new Alert(Alert.AlertType.ERROR, "Erreur: " + task.getException().getMessage()).showAndWait());

            new Thread(task).start();
        });

        form.getChildren().addAll(
                new Label("Date de retour réel:"),
                dateRetourReelPicker,
                confirmButton
        );

        dateStage.setScene(new Scene(form, 300, 200));
        dateStage.show();
    }
}