/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy, Mariia Zhdaniuk
 * Description: Main entry point and application bootstrapper. Responsible for initializing
 * JavaFX, routing scenes (Main Menu vs Game), injecting dependencies, and
 * managing the global game lifecycle.
 */
package xyuguyn00;

import java.nio.file.Path;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import xyuguyn00.bot.BotController;
import xyuguyn00.common.enums.GameMode;
import xyuguyn00.game.Game;
import xyuguyn00.game.GameFactory;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.logger.GameLogService;
import xyuguyn00.view.render.AssetManager;
import xyuguyn00.view.scene.*;

public class App extends Application {
    private Stage primaryStage;
    
    // Global state flags used to control background processes like the Bot loop
    private boolean isPaused = false; 
    private boolean isGameActive = false;
    
    private String currentMapFile = "game_stats.json"; 
    private int windowWidth = 1200;
    private int windowHeight = 900;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Advance Wars Clone");
        showMainMenu();
    }

    private void showMainMenu() {
        // Reset global states when returning to the menu to safely kill any active game loops
        isGameActive = false;
        isPaused = false;
        
        MainMenuView mainMenu = new MainMenuView(this::startGame);
        Scene scene = new Scene(mainMenu, windowWidth, windowHeight);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame(GameMode mode, String mapFile) {
        try {
            isGameActive = true; 
            currentMapFile = mapFile;

            // 1. Load flyweight graphics cache
            AssetManager assetManager = new AssetManager();
            assetManager.loadAssets();
            
            // 2. Bootstrap the core engine via data files
            Game game = GameFactory.createGame(
                "data/" + mapFile,
                "data/terrain.tsv",
                "data/units.tsv",
                "data/units-damage.tsv"
            );

            // 3. Initialize Replay & Action routing architecture
            Path logPath = Path.of("game-log.json");
            GameLogService logService = new GameLogService();
            logService.startNewLog(game);
            
            GameActionDispatcher dispatcher = GameActionDispatcher.createDefault(game, logService, logPath);

            // 4. Assemble the UI layout
            BorderPane gameLayout = new BorderPane();
            GameView boardView = new GameView(game, dispatcher, assetManager); 

            Runnable togglePause = () -> {
                isPaused = !isPaused;
                if (!isPaused) {
                    game.fireGameEvent(null, "Resume");
                }
            };

            PlayerSidebar sidebar = new PlayerSidebar(game, dispatcher, logService, logPath, boardView, mode, togglePause);
            TopNavigationBar topBar = new TopNavigationBar(this::showMainMenu);

            gameLayout.setTop(topBar);
            gameLayout.setLeft(sidebar);
            gameLayout.setCenter(boardView);

            // Using a StackPane as the root allows us to easily drop modal overlays on top of the game
            StackPane rootPane = new StackPane(gameLayout);
            Scene gameScene = new Scene(rootPane, windowWidth, windowHeight);
            primaryStage.setScene(gameScene);

            // 5. Global Observer Trap for Win Condition
            // Placed here so the UI can instantly respond by freezing the game and showing the overlay
            game.addObserver(event -> {
                if (event.getMessage() != null && event.getMessage().startsWith("GAME_OVER:")) {
                    String winner = event.getMessage().split(":")[1];
                    isGameActive = false; // Prevents bots from continuing to play in the background
                    
                    rootPane.getChildren().add(new GameOverOverlay(
                        winner, 
                        () -> startGame(mode, currentMapFile), 
                        this::showMainMenu
                    ));
                }
            });

            // 6. Attach AI
            // We pass state flags as lambda suppliers so the BotController always reads the latest values
            BotController botController = new BotController(game, dispatcher, logService, () -> isGameActive, () -> isPaused);
            botController.attachBots(mode);

        } catch (Exception e) {
            // Failsafe: Catch map parsing errors and fallback to the main menu
            showFatalError("Initialization Failed", "Could not load game data:\n" + e.getMessage());
            e.printStackTrace();
            showMainMenu(); 
        }
    }

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