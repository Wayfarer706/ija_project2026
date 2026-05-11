package xyuguyn00.view;

import java.nio.file.Path;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.GameEvent;
import xyuguyn00.dto.GameActionDto;
import xyuguyn00.dto.GameActionType;
import xyuguyn00.game.Game;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.service.GameLogService;
import xyuguyn00.tool.GameObserver;

/**
 * The sidebar UI displaying player stats and turn controls.
 */
public class PlayerSidebar extends VBox implements GameObserver {
    private final Game game;
    private final VBox player1Card;
    private final VBox player2Card;

    private final GameActionDispatcher dispatcher;
    private final GameLogService logService;
    private final Path logPath;
    private final GameView gameView;

    private final Button endTurnBtn;
    private final Button backBtn;
    private final Button nextBtn;
    private final Button playFromHereBtn;
    
    // Hold references to the text fields so we can update them
    private final Text p1GoldText;
    private final Text p2GoldText;

    public PlayerSidebar(Game game, GameActionDispatcher dispatcher, GameLogService logService, Path logPath, GameView gameView) {
        this.game = game;
        this.game.addObserver(this);
        this.dispatcher = dispatcher;
        this.logService = logService;
        this.logPath = logPath;
        this.gameView = gameView;

        this.setPadding(new Insets(20));
        this.setSpacing(30);
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #2b2b2b;"); 
        this.setPrefWidth(200);

        // Initialize UI Elements
        p1GoldText = new Text("Gold: 0");
        p1GoldText.setFill(Color.GOLD);
        p1GoldText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        p2GoldText = new Text("Gold: 0");
        p2GoldText.setFill(Color.GOLD);
        p2GoldText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        player1Card = createPlayerCard("Player 1", Color.DARKBLUE, p1GoldText);
        player2Card = createPlayerCard("Player 2", Color.DARKRED, p2GoldText);

        endTurnBtn = new Button("End Turn");
        backBtn = new Button("Back");
        nextBtn = new Button("Next");
        playFromHereBtn = new Button("Play From Here");

        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> {
            this.logService.stepBackward(this.game);
            updateReplayControls();
        });

        nextBtn.setMaxWidth(Double.MAX_VALUE);
        nextBtn.setOnAction(e -> {
            this.logService.stepForward(this.game);
            updateReplayControls();
        });

        playFromHereBtn.setMaxWidth(Double.MAX_VALUE);
        playFromHereBtn.setOnAction(e -> {
            try {
                this.logService.continueGameFromCurrentReplayState(this.game);
                this.logService.save(this.logPath);
                this.gameView.setReplayMode(false);
                updateReplayControls();
            } catch (Exception ex) {
                showError("Could not continue game", ex.getMessage());
            }
        });

        endTurnBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        endTurnBtn.setOnAction(e -> {
            var result = this.dispatcher.dispatch(
                GameActionDto.builder(GameActionType.END_TURN).build()
            );

            if (result.isFailure()) {
                showError("End turn failed", result.getMessage());
            }

            updateReplayControls();
        });

        this.getChildren().addAll(
            player1Card,
            player2Card,
            endTurnBtn,
            backBtn,
            nextBtn,
            playFromHereBtn
        );
        
        // Set initial visual state
        updateSidebarState();
        updateReplayControls();
    }

    private VBox createPlayerCard(String playerName, Color themeColor, Text goldText) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        
        Text nameText = new Text(playerName);
        nameText.setFill(themeColor);
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        card.getChildren().addAll(nameText, goldText);
        return card;
    }

    private void updateSidebarState() {
        String activeBorder = "-fx-border-color: gold; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #3c3c3c; -fx-background-radius: 5;";
        String inactiveBorder = "-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #3c3c3c; -fx-background-radius: 5;";

        // Update active player highlight
        if (game.getCurrentPlayer().equals("Player 1")) {
            player1Card.setStyle(activeBorder);
            player2Card.setStyle(inactiveBorder);
        } else {
            player1Card.setStyle(inactiveBorder);
            player2Card.setStyle(activeBorder);
        }
        
        // Update Gold Counters dynamically from the engine
        p1GoldText.setText("Gold: " + game.getPlayerFunds("Player 1"));
        p2GoldText.setText("Gold: " + game.getPlayerFunds("Player 2"));
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateReplayControls() {
        boolean canGoBack = this.logService.canStepBackward();
        boolean canGoNext = this.logService.canStepForward();
        boolean isInHistory = !this.logService.isAtLatestState();

        backBtn.setDisable(!canGoBack);
        nextBtn.setDisable(!canGoNext);
        playFromHereBtn.setDisable(!isInHistory);

        endTurnBtn.setDisable(isInHistory);

        this.gameView.setReplayMode(isInHistory);
    }

    @Override
    public void update(GameEvent event) {
        updateSidebarState();
        updateReplayControls();
    }
}