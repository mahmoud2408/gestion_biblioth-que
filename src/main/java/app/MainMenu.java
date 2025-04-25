package app;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button bookButton = new Button("Gérer les livres");
        bookButton.setOnAction(e -> {
            BookListWindow bookWindow = new BookListWindow();
            bookWindow.show();
        });

        Button studentButton = new Button("Gérer les étudiants");
        studentButton.setOnAction(e -> {
            StudentListWindow studentWindow = new StudentListWindow();
            studentWindow.show();
        });

        // Nouveau bouton pour les emprunts
        Button loanButton = new Button("Gérer les emprunts");
        loanButton.setOnAction(e -> {
            EmpruntListWindow loanWindow = new EmpruntListWindow();
            loanWindow.show();
        });

        VBox root = new VBox(10); // 10 pour l'espacement entre les boutons
        root.getChildren().addAll(bookButton, studentButton, loanButton); // Ajout du 3ème bouton

        Scene scene = new Scene(root, 300, 250); // Augmentation de la hauteur pour accommoder le nouveau bouton
        primaryStage.setScene(scene);
        primaryStage.setTitle("Menu Principal");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}