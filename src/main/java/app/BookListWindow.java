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

public class BookListWindow extends Stage {

    private TableView<Book> tableView;
    private ObservableList<Book> bookList;

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

        addButton.setOnAction(e -> showBookForm(null)); // null means "Add"
        editButton.setOnAction(e -> {
            Book selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showBookForm(selected); // edit mode
            }
        });
        deleteButton.setOnAction(e -> {
            Book selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteBook(selected);
            }
        });

        Scene scene = new Scene(root, 700, 400);
        this.setTitle("Gestion des livres");
        this.setScene(scene);

        refreshTable();
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
    }

    private void refreshTable() {
        bookList = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "", "mahmoud");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM LIVRE")) {

            while (rs.next()) {
                bookList.add(new Book(
                        rs.getInt("CODE_LIVRE"),
                        rs.getString("TITRE"),
                        rs.getString("AUTEUR"),
                        rs.getString("CATEGORIE"),
                        rs.getInt("ANNEE_PUBLICATION"),
                        rs.getInt("QUANTITE_DISPONIBLE")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableView.setItems(bookList);
    }

    private void showBookForm(Book book) {
        Stage formStage = new Stage();
        VBox form = new VBox(10);
        form.setPadding(new Insets(10));

        TextField titreField = new TextField();
        titreField.setPromptText("Titre");

        TextField auteurField = new TextField();
        auteurField.setPromptText("Auteur");

        TextField categorieField = new TextField();
        categorieField.setPromptText("Catégorie");

        TextField anneeField = new TextField();
        anneeField.setPromptText("Année de publication");

        TextField quantiteField = new TextField();
        quantiteField.setPromptText("Quantité disponible");

        if (book != null) {
            // Edit mode: fill fields
            titreField.setText(book.titreProperty().get());
            auteurField.setText(book.auteurProperty().get());
            categorieField.setText(book.categorieProperty().get());
            anneeField.setText(String.valueOf(book.anneeProperty().get()));
            quantiteField.setText(String.valueOf(book.quantiteProperty().get()));
        }

        Button saveButton = new Button("Enregistrer");

        saveButton.setOnAction(e -> {
            String titre = titreField.getText();
            String auteur = auteurField.getText();
            String categorie = categorieField.getText();
            int annee = Integer.parseInt(anneeField.getText());
            int quantite = Integer.parseInt(quantiteField.getText());

            try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "MAHMOUD", "mahmoud")) {
                PreparedStatement stmt;
                if (book == null) {
                    // INSERT
                    stmt = conn.prepareStatement("INSERT INTO LIVRE VALUES (SEQ_LIVRE.NEXTVAL, ?, ?, ?, ?, ?)");
                } else {
                    // UPDATE
                    stmt = conn.prepareStatement("UPDATE LIVRE SET TITRE=?, AUTEUR=?, CATEGORIE=?, ANNEE_PUBLICATION=?, QUANTITE_DISPONIBLE=? WHERE CODE_LIVRE=?");
                    stmt.setInt(6, book.codeProperty().get());
                }

                stmt.setString(1, titre);
                stmt.setString(2, auteur);
                stmt.setString(3, categorie);
                stmt.setInt(4, annee);
                stmt.setInt(5, quantite);
                stmt.executeUpdate();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            formStage.close();
            refreshTable();
        });

        form.getChildren().addAll(titreField, auteurField, categorieField, anneeField, quantiteField, saveButton);
        formStage.setScene(new Scene(form, 300, 300));
        formStage.setTitle(book == null ? "Ajouter un livre" : "Modifier le livre");
        formStage.show();
    }

    private void deleteBook(Book book) {
        try (Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "MAHMOUD", "mahmoud")) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM LIVRE WHERE CODE_LIVRE=?");
            stmt.setInt(1, book.codeProperty().get());
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        refreshTable();
    }
}
