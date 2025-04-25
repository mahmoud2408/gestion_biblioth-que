package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BookManager {

    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private Connection connection;
    private Stage stage;

    public void start(Stage stage, Connection connection) {
        this.stage = stage;
        this.connection = connection;

        TableView<Book> tableView = new TableView<>();
        setupTable(tableView);

        GridPane form = setupForm(tableView);

        VBox root = new VBox(10, tableView, form);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Gestion des Livres");
        stage.show();

        loadBooksAsync(tableView);
    }

    private void setupTable(TableView<Book> tableView) {
        TableColumn<Book, Number> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> data.getValue().codeProperty());

        TableColumn<Book, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(data -> data.getValue().titreProperty());

        TableColumn<Book, String> auteurCol = new TableColumn<>("Auteur");
        auteurCol.setCellValueFactory(data -> data.getValue().auteurProperty());

        TableColumn<Book, String> categorieCol = new TableColumn<>("Catégorie");
        categorieCol.setCellValueFactory(data -> data.getValue().categorieProperty());

        TableColumn<Book, Number> anneeCol = new TableColumn<>("Année");
        anneeCol.setCellValueFactory(data -> data.getValue().anneeProperty());

        TableColumn<Book, Number> quantiteCol = new TableColumn<>("Quantité");
        quantiteCol.setCellValueFactory(data -> data.getValue().quantiteProperty());

        tableView.getColumns().addAll(codeCol, titreCol, auteurCol, categorieCol, anneeCol, quantiteCol);
        tableView.setItems(bookList);
    }

    private GridPane setupForm(TableView<Book> tableView) {
        TextField codeField = new TextField(); codeField.setPromptText("Code");
        TextField titreField = new TextField(); titreField.setPromptText("Titre");
        TextField auteurField = new TextField(); auteurField.setPromptText("Auteur");
        TextField categorieField = new TextField(); categorieField.setPromptText("Catégorie");
        TextField anneeField = new TextField(); anneeField.setPromptText("Année");
        TextField quantiteField = new TextField(); quantiteField.setPromptText("Quantité");

        Button addButton = new Button("Ajouter");
        addButton.setOnAction(e -> addBookAsync(codeField, titreField, auteurField,
                categorieField, anneeField, quantiteField, tableView));

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Code:"), 0, 0);
        form.add(codeField, 1, 0);
        form.add(new Label("Titre:"), 0, 1);
        form.add(titreField, 1, 1);
        form.add(new Label("Auteur:"), 0, 2);
        form.add(auteurField, 1, 2);
        form.add(new Label("Catégorie:"), 0, 3);
        form.add(categorieField, 1, 3);
        form.add(new Label("Année:"), 0, 4);
        form.add(anneeField, 1, 4);
        form.add(new Label("Quantité:"), 0, 5);
        form.add(quantiteField, 1, 5);
        form.add(addButton, 1, 6);
        return form;
    }

    private void loadBooksAsync(TableView<Book> tableView) {
        Task<ObservableList<Book>> task = new Task<>() {
            @Override protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> list = FXCollections.observableArrayList();
                try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM livres");
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
        task.setOnSucceeded(e -> tableView.setItems(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    private void addBookAsync(TextField codeField, TextField titreField, TextField auteurField,
                              TextField categorieField, TextField anneeField, TextField quantiteField,
                              TableView<Book> tableView) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                int code = Integer.parseInt(codeField.getText());
                String titre = titreField.getText();
                String auteur = auteurField.getText();
                String categorie = categorieField.getText();
                int annee = Integer.parseInt(anneeField.getText());
                int quantite = Integer.parseInt(quantiteField.getText());
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO livres (CodeLivre, Titre, Auteur, Categorie, AnneePublication, QuantiteDisponible) VALUES (?,?,?,?,?,?)")) {
                    stmt.setInt(1, code);
                    stmt.setString(2, titre);
                    stmt.setString(3, auteur);
                    stmt.setString(4, categorie);
                    stmt.setInt(5, annee);
                    stmt.setInt(6, quantite);
                    stmt.executeUpdate();
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> loadBooksAsync(tableView));
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }
}
