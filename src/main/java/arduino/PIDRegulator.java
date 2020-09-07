package arduino;

import controllers.RootController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class PIDRegulator extends Application {

    public static final Set<MouseButton> MOUSE_PRESSED_KEYS = new HashSet<>();
    public static final Set<KeyCode> KEYBOARD_PRESSED_KEYS = new HashSet<>();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("PID regulator | Krystian Borowicz");
        primaryStage.setMinWidth(1350);
        primaryStage.setMinHeight(603);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.setX(50);
        primaryStage.setY(50);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Root.fxml"));
        Scene scene = new Scene(loader.load());
        loader.<RootController>getController().setMainScene(scene);

        scene.getStylesheets().add(getClass().getResource("/base.css").toExternalForm());
        //scene.getStylesheets().add(getClass().getResource("/modena_dark.css").toExternalForm());

        scene.setOnMousePressed(e -> MOUSE_PRESSED_KEYS.add(e.getButton()));
        scene.setOnMouseReleased(e -> MOUSE_PRESSED_KEYS.remove(e.getButton()));

        scene.setOnKeyPressed(e -> KEYBOARD_PRESSED_KEYS.add(e.getCode()));
        scene.setOnKeyReleased(e -> KEYBOARD_PRESSED_KEYS.remove(e.getCode()));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
