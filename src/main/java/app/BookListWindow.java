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
import java.util.Locale;
import java.util.Optional;

public class BookListWindow extends Stage {

    private TableView<Book> tableView = new TableView<>();
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "MAHMOUD";
    private static final String PASS = "mahmoud";

    public BookListWindow() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Gestion des Livres");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un livre...");
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

        addButton.setOnAction(e -> showBookForm(null));
        editButton.setOnAction(e -> {
            Book selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) showBookForm(selected);
        });
        deleteButton.setOnAction(e -> deleteBook());

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton);

        // Search Functionality
        FilteredList<Book> filteredData = new FilteredList<>(bookList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(book -> {
                if (newVal == null || newVal.isEmpty()) return true;

                String lowerCaseFilter = newVal.toLowerCase(Locale.FRENCH);

                // Conversion des numériques en String pour la recherche
                String annee = String.valueOf(book.anneeProperty().get());
                String code = String.valueOf(book.codeProperty().get());

                return Optional.ofNullable(book.titreProperty().get()).orElse("").toLowerCase().contains(lowerCaseFilter)
                        || Optional.ofNullable(book.auteurProperty().get()).orElse("").toLowerCase().contains(lowerCaseFilter)
                        || Optional.ofNullable(book.categorieProperty().get()).orElse("").toLowerCase().contains(lowerCaseFilter)
                        || annee.contains(lowerCaseFilter) // Recherche par année
                        || code.contains(lowerCaseFilter); // Recherche par code
            });

            tableView.refresh();
        });

        tableView.setItems(filteredData);

        root.getChildren().addAll(header, tableView, buttonBox);

        Scene scene = new Scene(root, 1000, 700);
        this.setScene(scene);
        this.setTitle("Gestion des Livres");
        loadBooksAsync();
    }

    private void setupTableView() {
        TableColumn<Book, Integer> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<Book, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));

        TableColumn<Book, String> auteurCol = new TableColumn<>("Auteur");
        auteurCol.setCellValueFactory(new PropertyValueFactory<>("auteur"));

        TableColumn<Book, String> categorieCol = new TableColumn<>("Catégorie");
        categorieCol.setCellValueFactory(new PropertyValueFactory<>("categorie"));

        TableColumn<Book, Integer> anneeCol = new TableColumn<>("Année");
        anneeCol.setCellValueFactory(new PropertyValueFactory<>("annee"));

        TableColumn<Book, Integer> quantiteCol = new TableColumn<>("Quantité");
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        tableView.getColumns().addAll(codeCol, titreCol, auteurCol, categorieCol, anneeCol, quantiteCol);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private Button createStyledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        return btn;
    }

    private void loadBooksAsync() {
        Task<ObservableList<Book>> task = new Task<>() {
            @Override
            protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> list = FXCollections.observableArrayList();
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement("SELECT * FROM livres");
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        list.add(new Book(
                                rs.getInt("CodeLivre"),
                                rs.getString("Titre"),
                                rs.getString("Auteur"),
                                rs.getString("Categorie"),
                                rs.getInt("AnneePublication"),
                                rs.getInt("QuantiteDisponible")
                        ));
                    }
                }
                return list;
            }
        };

        task.setOnSucceeded(e -> bookList.setAll(task.getValue()));
        new Thread(task).start();
    }

    private void showBookForm(Book book) {
        Stage formStage = new Stage();
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-radius: 8;");

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");
        TextField auteurField = new TextField();
        auteurField.setPromptText("Auteur");
        TextField categorieField = new TextField();
        categorieField.setPromptText("Catégorie");
        TextField anneeField = new TextField();
        anneeField.setPromptText("Année");
        TextField quantiteField = new TextField();
        quantiteField.setPromptText("Quantité");

        if (book != null) {
            titreField.setText(book.titreProperty().get());
            auteurField.setText(book.auteurProperty().get());
            categorieField.setText(book.categorieProperty().get());
            anneeField.setText(String.valueOf(book.anneeProperty().get()));
            quantiteField.setText(String.valueOf(book.quantiteProperty().get()));
        }

        Button saveButton = createStyledButton("Enregistrer", "#27ae60");
        saveButton.setOnAction(e -> {
            if (validateFields(titreField, anneeField, quantiteField)) {
                saveBook(
                        book == null ? 0 : book.codeProperty().get(),
                        titreField.getText(),
                        auteurField.getText(),
                        categorieField.getText(),
                        Integer.parseInt(anneeField.getText()),
                        Integer.parseInt(quantiteField.getText())
                );
                formStage.close();
            }
        });

        form.getChildren().addAll(
                new Label("Formulaire Livre"), titreField, auteurField,
                categorieField, anneeField, quantiteField, saveButton
        );

        formStage.setScene(new Scene(form, 400, 400));
        formStage.show();
    }

    private boolean validateFields(TextField... fields) {
        for (TextField field : fields) {
            if (field.getText().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Tous les champs obligatoires doivent être remplis !").showAndWait();
                return false;
            }
        }
        return true;
    }

    private void saveBook(int code, String titre, String auteur, String categorie, int annee, int quantite) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String query = code == 0 ?
                        "INSERT INTO livres VALUES (?, ?, ?, ?, ?, ?)" :
                        "UPDATE livres SET Titre=?, Auteur=?, Categorie=?, AnneePublication=?, QuantiteDisponible=? WHERE CodeLivre=?";

                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement(query)) {

                    if (code == 0) {
                        stmt.setInt(1, code);
                        stmt.setString(2, titre);
                        stmt.setString(3, auteur);
                        stmt.setString(4, categorie);
                        stmt.setInt(5, annee);
                        stmt.setInt(6, quantite);
                    } else {
                        stmt.setString(1, titre);
                        stmt.setString(2, auteur);
                        stmt.setString(3, categorie);
                        stmt.setInt(4, annee);
                        stmt.setInt(5, quantite);
                        stmt.setInt(6, code);
                    }
                    stmt.executeUpdate();
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> loadBooksAsync());
        new Thread(task).start();
    }

    private void deleteBook() {
        Book selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce livre ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Void> task = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                             PreparedStatement stmt = conn.prepareStatement(
                                     "DELETE FROM livres WHERE CodeLivre = ?")) {
                            stmt.setInt(1, selected.codeProperty().get());
                            stmt.executeUpdate();
                        }
                        return null;
                    }
                };
                task.setOnSucceeded(e -> loadBooksAsync());
                new Thread(task).start();
            }
        });
    }
}