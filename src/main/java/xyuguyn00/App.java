package xyuguyn00;

import java.nio.file.Path;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import xyuguyn00.bot.DummyBot;
import xyuguyn00.common.enums.GameMode;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.game.Game;
import xyuguyn00.game.GameFactory;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.service.GameLogService;
import xyuguyn00.view.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class App extends Application {
    private Stage primaryStage;
    private boolean isPaused = false; 
    private boolean isGameActive = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Strategy Game");
        showMainMenu();
    }

    private void showMainMenu() {
        isGameActive = false;
        isPaused = false;
        MainMenuView mainMenu = new MainMenuView(this::startGame);
        Scene scene = new Scene(mainMenu, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame(GameMode mode) {
        try {
            isGameActive = true;

            Game game = GameFactory.createGame(
                "data/game_stats.json", 
                "data/terrain.tsv", 
                "data/units.tsv",
                "data/units-damage.tsv"
            );

            Path logPath = Path.of("game-log.json");
            GameLogService logService = new GameLogService();
            logService.startNewLog(game);
            
            GameActionDispatcher dispatcher = GameActionDispatcher.createDefault(game, logService, logPath);

            BorderPane root = new BorderPane();
            GameView boardView = new GameView(game, dispatcher); 

            // Define the pause toggle behavior
            Runnable togglePause = () -> {
                isPaused = !isPaused;
                if (!isPaused) {
                    // Manually fire an event to kickstart the bot loop again
                    game.fireGameEvent(null, "Resume");
                }
            };

            // Pass the mode and the toggle callback to the sidebar
            PlayerSidebar sidebar = new PlayerSidebar(game, dispatcher, logService, logPath, boardView, mode, togglePause);
            HBox topBar = createTopBar();

            root.setTop(topBar);
            root.setLeft(sidebar);
            root.setCenter(boardView);

            Scene gameScene = new Scene(root, 1000, 600);
            primaryStage.setScene(gameScene);

            attachBots(mode, game, dispatcher, logService);

        } catch (Exception e) {
            showFatalError("Initialization Failed", "Could not load game data:\n" + e.getMessage());
            e.printStackTrace();
            showMainMenu(); 
        }
    }

    private HBox createTopBar() {
        Button returnBtn = new Button("⬅ Return to Main Menu");
        returnBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        returnBtn.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white;");
        
        // Destroys the current game state and brings back the menu
        returnBtn.setOnAction(e -> showMainMenu());

        HBox topBar = new HBox(returnBtn);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #3c3c3c; -fx-border-width: 0 0 2 0;");
        return topBar;
    }

    private void attachBots(GameMode mode, Game game, GameActionDispatcher dispatcher, GameLogService logService) {
        if (mode == GameMode.PLAYER_VS_PLAYER) {
            return; 
        }

        DummyBot bot2 = new DummyBot(game, dispatcher, PlayerId.PLAYER_2);
        
        if (mode == GameMode.PLAYER_VS_BOT) {
            game.addObserver(event -> {
                if (!isGameActive) return;
                if (game.getCurrentPlayer() == PlayerId.PLAYER_2 && logService.isAtLatestState()) {
                    triggerBotTurn(bot2);
                }
            });
        } 
        else if (mode == GameMode.BOT_VS_BOT) {
            DummyBot bot1 = new DummyBot(game, dispatcher, PlayerId.PLAYER_1);
            
            game.addObserver(event -> {
                if (!isGameActive || !logService.isAtLatestState() || isPaused) {
                    return; 
                }

                if (game.getCurrentPlayer() == PlayerId.PLAYER_1) {
                    triggerBotTurn(bot1);
                } else if (game.getCurrentPlayer() == PlayerId.PLAYER_2) {
                    triggerBotTurn(bot2);
                }
            });

            triggerBotTurn(bot1);
        }
    }

    private void triggerBotTurn(DummyBot bot) {
        PauseTransition delay = new PauseTransition(Duration.seconds(0.8));
        delay.setOnFinished(e -> {
            if (isGameActive) {
                bot.playTurn();
            }
        });
        delay.play();
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