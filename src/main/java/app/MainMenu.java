package app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Création du conteneur principal
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f0f4f8;");

        // Titre de l'application
        Text title = new Text("Bibliothèque Manager Pro");
        title.setFont(Font.font("Roboto", 28));
        title.setStyle("-fx-fill: #2c3e50; -fx-font-weight: bold;");

        // Bouton de gestion des livres
        Button bookButton = createMenuButton("Gérer les livres", "/icons/edit-icon.png");
        bookButton.setOnAction(e -> {
            BookListWindow bookWindow = new BookListWindow();
            bookWindow.show();
        });

        // Bouton de gestion des étudiants
        Button studentButton = createMenuButton("Gérer les étudiants", "/images/student-icon.png");
        studentButton.setOnAction(e -> {
            StudentListWindow studentWindow = new StudentListWindow();
            studentWindow.show();
        });

        // Bouton de gestion des emprunts
        Button loanButton = createMenuButton("Gérer les emprunts", "/images/loan-icon.png");
        loanButton.setOnAction(e -> {
            EmpruntListWindow loanWindow = new EmpruntListWindow();
            loanWindow.show();
        });

        // Ajout des éléments au conteneur
        root.getChildren().addAll(title, bookButton, studentButton, loanButton);

        // Configuration de la scène
        Scene scene = new Scene(root, 500, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Configuration de la fenêtre principale
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gestion de Bibliothèque");
        primaryStage.getIcons().add(new Image(getClass().getResource("/images/student-icon.png").toExternalForm()));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Button createMenuButton(String text, String iconPath) {
        Button button = new Button(text);

        // Configuration du style
        button.setStyle("-fx-background-color: #3498db; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 16px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 15 30; " +
                "-fx-background-radius: 8;");

        // Configuration de l'icône
        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitWidth(30);
        icon.setFitHeight(30);
        button.setGraphic(icon);
        button.setContentDisplay(ContentDisplay.LEFT);

        // Effet au survol
        button.setOnMouseEntered(e ->
                button.setStyle("-fx-background-color: #2980b9; " +
                        "-fx-cursor: hand; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 30; " +
                        "-fx-background-radius: 8;")
        );

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 15 30; " +
                        "-fx-background-radius: 8;")
        );

        return button;
    }

    public static void main(String[] args) {
        launch(args);
    }
}