package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class BookListWindow extends Stage {

    private TableView<Book> tableView;
    private ObservableList<Book> bookList;
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String USER = "MAHMOUD";
    private static final String PASS = "mahmoud";

    public BookListWindow() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label titleLabel = new Label("Liste des livres");

        tableView = new TableView<>();
        setupTableView();

        Button addButton = new Button("Ajouter");
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);

        root.getChildren().addAll(titleLabel, tableView, buttonBox);

        addButton.setOnAction(e -> showBookForm(null));
        editButton.setOnAction(e -> {
            Book selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) showBookForm(selected);
        });
        deleteButton.setOnAction(e -> {
            Book selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) deleteBook(selected);
        });

        setScene(new Scene(root, 700, 400));
        setTitle("Gestion des livres");

        refreshTable();
    }

    private void setupTableView() {
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
    }

    private void refreshTable() {
        Task<ObservableList<Book>> task = new Task<>() {
            @Override protected ObservableList<Book> call() throws Exception {
                ObservableList<Book> list = FXCollections.observableArrayList();
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM livres")) {
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

    private void showBookForm(Book book) {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        TextField titreField = new TextField(); titreField.setPromptText("Titre");
        TextField auteurField = new TextField(); auteurField.setPromptText("Auteur");
        TextField categorieField = new TextField(); categorieField.setPromptText("Catégorie");
        TextField anneeField = new TextField(); anneeField.setPromptText("Année de publication");
        TextField quantiteField = new TextField(); quantiteField.setPromptText("Quantité disponible");

        if (book != null) {
            titreField.setText(book.titreProperty().get());
            auteurField.setText(book.auteurProperty().get());
            categorieField.setText(book.categorieProperty().get());
            anneeField.setText(String.valueOf(book.anneeProperty().get()));
            quantiteField.setText(String.valueOf(book.quantiteProperty().get()));
        }

        Button saveButton = new Button("Enregistrer");
        saveButton.setOnAction(e -> {
            // validation simple
            if (titreField.getText().isBlank() || anneeField.getText().isBlank()) {
                new Alert(Alert.AlertType.WARNING, "Titre et année obligatoires").showAndWait();
                return;
            }
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
                        PreparedStatement stmt;
                        if (book == null) {
                            stmt = conn.prepareStatement(
                                    "INSERT INTO livres (Titre,Auteur,Categorie,AnneePublication,QuantiteDisponible) VALUES (?,?,?,?,?)");
                        } else {
                            stmt = conn.prepareStatement(
                                    "UPDATE livres SET Titre=?,Auteur=?,Categorie=?,AnneePublication=?,QuantiteDisponible=? WHERE CodeLivre=?");
                            stmt.setInt(6, book.codeProperty().get());
                        }
                        stmt.setString(1, titreField.getText());
                        stmt.setString(2, auteurField.getText());
                        stmt.setString(3, categorieField.getText());
                        stmt.setInt(4, Integer.parseInt(anneeField.getText()));
                        stmt.setInt(5, Integer.parseInt(quantiteField.getText()));
                        stmt.executeUpdate();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(ev -> {
                formStage.close();
                refreshTable();
            });
            task.setOnFailed(ev -> {
                Throwable ex = task.getException();
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Erreur d'enregistrement : " + ex.getMessage()).showAndWait();
            });
            new Thread(task).start();
        });


        form.getChildren().addAll(titreField, auteurField, categorieField, anneeField, quantiteField, saveButton);
        formStage.setScene(new Scene(form, 300, 300));
        formStage.setTitle(book == null ? "Ajouter un livre" : "Modifier le livre");
        formStage.show();
    }

    private void deleteBook(Book book) {
        Task<Integer> task = new Task<>() {
            @Override protected Integer call() throws Exception {
                try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
                     PreparedStatement stmt = conn.prepareStatement(
                             "DELETE FROM livres WHERE CodeLivre = ?")) {
                    stmt.setInt(1, book.codeProperty().get());
                    int n = stmt.executeUpdate();
                    return n;
                }

            }
        };

        task.setOnSucceeded(e -> {
            int deleted = task.getValue();
            if (deleted>0) {
                tableView.getItems().remove(book);
            } else {
                new Alert(Alert.AlertType.WARNING, "Aucun livre supprimé.").showAndWait();
            }
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur suppression : " + ex.getMessage()).showAndWait();
        });

        new Thread(task).start();
    }


}
