package xyuguyn00;

import java.nio.file.Path;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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
    private String currentMapFile = "game_stats.json"; 

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
        Scene scene = new Scene(mainMenu, 1000, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startGame(GameMode mode, String mapFile) {
        try {
            isGameActive = true; 

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
            HBox topBar = createTopBar();

            gameLayout.setTop(topBar);
            gameLayout.setLeft(sidebar);
            gameLayout.setCenter(boardView);

            // Wrap the game layout in a StackPane so we can overlay the win screen
            StackPane rootPane = new StackPane(gameLayout);

            Scene gameScene = new Scene(rootPane, 1200, 900);
            primaryStage.setScene(gameScene);

            // Global observer to catch the end-game trigger
            game.addObserver(event -> {
                if (event.getMessage() != null && event.getMessage().startsWith("GAME_OVER:")) {
                    String winner = event.getMessage().split(":")[1];
                    showGameOverOverlay(rootPane, winner, mode);
                }
            });

            attachBots(mode, game, dispatcher, logService);

        } catch (Exception e) {
            showFatalError("Initialization Failed", "Could not load game data:\n" + e.getMessage());
            e.printStackTrace();
            showMainMenu(); 
        }
    }

    private void showGameOverOverlay(StackPane rootPane, String winner, GameMode mode) {
        // Kill the game loop to prevent any remaining bots from acting
        isGameActive = false; 

        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Text title = new Text("Game Over");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setFill(Color.WHITE);

        Text subtitle = new Text(winner + " Wins!");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        subtitle.setFill(Color.GOLD);

        Button retryBtn = new Button("Retry");
        retryBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        retryBtn.setPrefWidth(220);
        retryBtn.setOnAction(e -> startGame(mode, currentMapFile));

        Button menuBtn = new Button("Main Menu");
        menuBtn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        menuBtn.setPrefWidth(220);
        menuBtn.setOnAction(e -> showMainMenu());

        overlay.getChildren().addAll(title, subtitle, retryBtn, menuBtn);
        rootPane.getChildren().add(overlay);
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