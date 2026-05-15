/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: The main HUD for the players. Manages active turn 
 * indication, economy tracking, and coordinates the time-travel replay system controls.
 */
package xyuguyn00.view.scene;

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
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.common.enums.GameMode;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.game.Game;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.logger.GameLogService;
import xyuguyn00.model.dto.GameActionDto;
import xyuguyn00.tool.GameObserver;
import xyuguyn00.view.render.ViewConstants;

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
    
    private final Text p1GoldText;
    private final Text p2GoldText;

    public PlayerSidebar(Game game, GameActionDispatcher dispatcher, GameLogService logService, Path logPath, GameView gameView, GameMode mode, Runnable onTogglePause) {
        this.game = game;
        this.game.addObserver(this);
        this.dispatcher = dispatcher;
        this.logService = logService;
        this.logPath = logPath;
        this.gameView = gameView;

        this.setPadding(new Insets(ViewConstants.PADDING_MEDIUM)); 
        this.setSpacing(ViewConstants.PADDING_LARGE); 
        this.setAlignment(Pos.TOP_CENTER);
        this.setStyle("-fx-background-color: #2b2b2b;"); 
        this.setPrefWidth(ViewConstants.SIDEBAR_WIDTH); 

        p1GoldText = new Text("Gold: 0");
        p1GoldText.setFill(Color.GOLD);
        p1GoldText.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_SMALL));
        
        p2GoldText = new Text("Gold: 0");
        p2GoldText.setFill(Color.GOLD);
        p2GoldText.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_SMALL));

        player1Card = createPlayerCard(PlayerId.PLAYER_1.label(), Color.DARKBLUE, p1GoldText);
        player2Card = createPlayerCard(PlayerId.PLAYER_2.label(), Color.DARKRED, p2GoldText);

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
                // Allows players to overwrite history and start a new timeline from a past state
                this.logService.continueGameFromCurrentReplayState(this.game);
                this.logService.save(this.logPath);
                this.gameView.setReplayMode(false);
                updateReplayControls();
                this.game.fireGameEvent(null, "Timeline Resumed");
            } catch (Exception ex) {
                showError("Could not continue game", ex.getMessage());
            }
        });

        endTurnBtn.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_SMALL));
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

        // The UI structurally adapts based on the mode. Bots don't need manual turn endings.
        if (mode == GameMode.BOT_VS_BOT) {
            Button pauseResumeBtn = new Button("Pause");
            pauseResumeBtn.setMaxWidth(Double.MAX_VALUE);
            pauseResumeBtn.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_SMALL));
            
            pauseResumeBtn.setOnAction(e -> {
                if (pauseResumeBtn.getText().equals("Pause")) {
                    pauseResumeBtn.setText("Resume");
                } else {
                    pauseResumeBtn.setText("Pause");
                }
                
                if (onTogglePause != null) {
                    onTogglePause.run();
                }
            });
            
            this.getChildren().addAll(player1Card, player2Card, pauseResumeBtn);
        } else {
            this.getChildren().addAll(
                player1Card,
                player2Card,
                endTurnBtn,
                backBtn,
                nextBtn,
                playFromHereBtn
            );
        }
        
        updateSidebarState();
        updateReplayControls();
    }

    private VBox createPlayerCard(String playerName, Color themeColor, Text goldText) {
        VBox card = new VBox(ViewConstants.PADDING_SMALL); 
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER);
        
        Text nameText = new Text(playerName);
        nameText.setFill(themeColor);
        nameText.setFont(Font.font(ViewConstants.FONT_MAIN, FontWeight.BOLD, ViewConstants.FONT_SIZE_MEDIUM));

        card.getChildren().addAll(nameText, goldText);
        return card;
    }

    private void updateSidebarState() {
        String activeBorder = "-fx-border-color: gold; -fx-border-width: 3; -fx-border-radius: 5; -fx-background-color: #3c3c3c; -fx-background-radius: 5;";
        String inactiveBorder = "-fx-border-color: gray; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #3c3c3c; -fx-background-radius: 5;";

        if (game.getCurrentPlayer() == PlayerId.PLAYER_1) {
            player1Card.setStyle(activeBorder);
            player2Card.setStyle(inactiveBorder);
        } else {
            player1Card.setStyle(inactiveBorder);
            player2Card.setStyle(activeBorder);
        }
        
        p1GoldText.setText("Gold: " + game.getPlayerFunds(PlayerId.PLAYER_1));
        p2GoldText.setText("Gold: " + game.getPlayerFunds(PlayerId.PLAYER_2));
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateReplayControls() {
        // Enforce strict state bounds so users cannot break the logger indexing
        boolean canGoBack = this.logService.canStepBackward();
        boolean canGoNext = this.logService.canStepForward();
        boolean isInHistory = !this.logService.isAtLatestState();

        backBtn.setDisable(!canGoBack);
        nextBtn.setDisable(!canGoNext);
        
        // Mutually exclusive UI states: You can either end your turn (present) or branch the timeline (past)
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