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

        VBox root = new VBox(10); // 10 for spacing between buttons
        root.getChildren().addAll(bookButton, studentButton);

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Menu Principal");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
