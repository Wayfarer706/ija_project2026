package xyuguyn00;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import xyuguyn00.game.Game;
import xyuguyn00.game.GameFactory;
import xyuguyn00.view.*;

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
                "data/units.tsv",
                "data/units-damage.tsv"
            );

            // --- Spawn some test units ---
            game.createUnit("Tank", "Player 1", 0, 0); 
            game.createUnit("Pěchota", "Player 2", 4, 4);
            game.createUnit("Tank", "Player 1", 1, 2); 
            game.createUnit("Pěchota", "Player 2", 4, 3);

            // Initialize the root layout
            BorderPane root = new BorderPane();
            
            // Attach the View components
            GameView boardView = new GameView(game); 
            PlayerSidebar sidebar = new PlayerSidebar(game);

            // Set layout positions
            root.setLeft(sidebar);
            root.setCenter(boardView);

            // Slightly widened the window to accommodate the 200px sidebar
            Scene scene = new Scene(root, 1000, 600); 
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