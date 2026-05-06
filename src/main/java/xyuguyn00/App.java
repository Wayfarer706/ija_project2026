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
            Game game = GameFactory.createGame(
                "data/game_stats.json", 
                "data/terrain.tsv", 
                "data/units.tsv",
                "data/units-damage.tsv"
            );

            BorderPane root = new BorderPane();
            GameView boardView = new GameView(game); 
            PlayerSidebar sidebar = new PlayerSidebar(game);

            root.setLeft(sidebar);
            root.setCenter(boardView);

            Scene scene = new Scene(root, 1000, 600); 
            primaryStage.setTitle("Strategy Game");
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