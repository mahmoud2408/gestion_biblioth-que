package app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    public void start(Stage stage, Connection connection) {
        TableView<Book> tableView = new TableView<>();

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

        TextField codeField = new TextField();
        codeField.setPromptText("Code");

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

        Button addButton = new Button("Ajouter");
        addButton.setOnAction(e -> {
            try {
                int code = Integer.parseInt(codeField.getText());
                String titre = titreField.getText();
                String auteur = auteurField.getText();
                String categorie = categorieField.getText();
                int annee = Integer.parseInt(anneeField.getText());
                int quantite = Integer.parseInt(quantiteField.getText());

                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO Livre (CODE_LIVRE, TITRE, AUTEUR, CATEGORIE, ANNEE_PUBLICATION, QUANTITE_DISPONIBLE) VALUES (?, ?, ?, ?, ?, ?)");
                stmt.setInt(1, code);
                stmt.setString(2, titre);
                stmt.setString(3, auteur);
                stmt.setString(4, categorie);
                stmt.setInt(5, annee);
                stmt.setInt(6, quantite);
                stmt.executeUpdate();

                loadBooks(connection); // refresh table
                codeField.clear();
                titreField.clear();
                auteurField.clear();
                categorieField.clear();
                anneeField.clear();
                quantiteField.clear();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

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

        VBox root = new VBox(10, tableView, form);
        root.setPadding(new Insets(10));

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Gestion des Livres");
        stage.show();

        loadBooks(connection);
    }

    private void loadBooks(Connection connection) {
        bookList.clear();
        try {
            PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Livre");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int code = rs.getInt("CODE_LIVRE");
                String titre = rs.getString("TITRE");
                String auteur = rs.getString("AUTEUR");
                String categorie = rs.getString("CATEGORIE");
                int annee = rs.getInt("ANNEE_PUBLICATION");
                int quantite = rs.getInt("QUANTITE_DISPONIBLE");
                bookList.add(new Book(code, titre, auteur, categorie, annee, quantite));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
