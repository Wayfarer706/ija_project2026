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

            AssetManager assetManager = new AssetManager();
            assetManager.loadAssets();
            
            Game game = GameFactory.createGame(
                "data/" + mapFile,
                "data/terrain.tsv",
                "data/units.tsv",
                "data/units-damage.tsv"
            );

            Path logPath = Path.of("game-log.json");
            GameLogService logService = new GameLogService();
            logService.startNewLog(game);
            
            GameActionDispatcher dispatcher = GameActionDispatcher.createDefault(game, logService, logPath);

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

            StackPane rootPane = new StackPane(gameLayout);
            Scene gameScene = new Scene(rootPane, windowWidth, windowHeight);
            primaryStage.setScene(gameScene);

            // Global observer to catch the end-game trigger
            game.addObserver(event -> {
                if (event.getMessage() != null && event.getMessage().startsWith("GAME_OVER:")) {
                    String winner = event.getMessage().split(":")[1];
                    isGameActive = false; // Kill bot loop
                    rootPane.getChildren().add(new GameOverOverlay(
                        winner, 
                        () -> startGame(mode, currentMapFile), 
                        this::showMainMenu
                    ));
                }
            });

            // Hand off AI management to the controller
            BotController botController = new BotController(game, dispatcher, logService, () -> isGameActive, () -> isPaused);
            botController.attachBots(mode);

        } catch (Exception e) {
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