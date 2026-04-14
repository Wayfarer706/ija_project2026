package xyuguyn00;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import xyuguyn00.game.Game;
import xyuguyn00.game.GameFactory;

/**
 * The main entry point for the JavaFX GUI application.
 * Bootstraps the game engine and initializes the primary window.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Define a temporary test map (this uses the characters mapped in Game.java)
            String[] mapDefinition = {
                "P P F M W",
                "P C P F P",
                "P P T P P",
                "M F P C W",
                "H P P P P"
            };

            // 2. Boot up the backend engine using our GameFactory
            Game game = GameFactory.createGame(
                mapDefinition, 
                "data/terrain.tsv", 
                "data/units.tsv"
            );

            // 3. Initialize the root layout structure
            BorderPane root = new BorderPane();
            
            // TODO: We will attach the Grid Canvas (View) and Controls here in the next step.
            // e.g., GameView view = new GameView(game);
            // root.setCenter(view);

            // 4. Configure the primary window (Stage)
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Strategy Game - xyuguyn00");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Locking size keeps initial grid math simple
            primaryStage.show();

        } catch (Exception e) {
            // If the TSV files fail to load, we cannot launch. Show a native GUI error.
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