package xyuguyn00;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import xyuguyn00.game.Game;
import xyuguyn00.game.GameFactory;
import xyuguyn00.view.GameView;

/**
 * The main entry point for the JavaFX GUI application.
 * Bootstraps the game engine and initializes the primary window.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            String[] mapDefinition = {
                "P P F M W",
                "P C P F P",
                "P P T P P",
                "M F P C W",
                "H P P P P"
            };

            Game game = GameFactory.createGame(
                mapDefinition, 
                "data/terrain.tsv", 
                "data/units.tsv"
            );

            // --- Spawn some test units ---
            // Creating a Tank for Player 1 at [0, 0]
            game.createUnit("Tank", "Player 1", 0, 0); 
            // Creating Infantry for Player 2 at [4, 4]
            game.createUnit("Pěchota", "Player 2", 4, 4);

            // Initialize the root layout and attach our custom View
            BorderPane root = new BorderPane();
            
            GameView view = new GameView(game); 
            root.setCenter(view);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Strategy Game - xyuguyn00");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            showFatalError("Initialization Failed", "Could not load game data:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays a blocking error dialog to the user.
     */
    private void showFatalError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Critical Application Error");
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}