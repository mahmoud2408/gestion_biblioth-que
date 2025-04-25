package app;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BorrowManager {
    public static void showBorrowWindow() {
        Stage stage = new Stage();
        VBox layout = new VBox();
        layout.getChildren().add(new Label("🔄 Borrow & Return Window"));

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Borrow & Return");
        stage.setScene(scene);
        stage.show();
    }
}
