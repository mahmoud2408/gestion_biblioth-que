package app;

import javafx.application.Application;
import javafx.stage.Stage;

public class LibraryApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainMenu menu = new MainMenu();
        menu.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
