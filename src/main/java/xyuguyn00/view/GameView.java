package xyuguyn00.view;

import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import xyuguyn00.common.GameEvent;
import xyuguyn00.common.Position;
import xyuguyn00.game.Game;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.tool.GameObserver;

public class GameView extends GridPane implements GameObserver {
    private final Game game;
    private final int tileSize = 60; 
    private final TileRenderer tileRenderer;
    private final GameBoardView boardView;
    
    private final InteractionController interactionController;

    private boolean replayMode = false;

    public GameView(Game game, GameActionDispatcher dispatcher, AssetManager assetManager) {
        this.game = game;
        this.game.addObserver(this); 
        this.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: #2F4F4F;");
        
        this.tileRenderer = new TileRenderer(tileSize, assetManager);
        this.boardView = new GameBoardView(game, tileRenderer);
        
        this.interactionController = new InteractionController(game, dispatcher, this);

        this.boardView.setOnTileClicked(this::handleTileClick);
        this.boardView.setOnTileHovered(this::handleTileHover);

        this.add(boardView, 0, 0);

        requestRender();
    }

    public void setReplayMode(boolean replayMode) {
        this.replayMode = replayMode;
        interactionController.clearSelection();
    }

    private void handleTileClick(Position clickedPos, MouseEvent event) {   
        if (replayMode) return;
        interactionController.handleTileClick(clickedPos, event);
    }
    
    private void handleTileHover(Position hoveredPos) {
        if (replayMode) return;
        interactionController.handleTileHover(hoveredPos);
    }

    public void requestRender() {
        Platform.runLater(() -> boardView.render(interactionController.getViewState()));
    }

    @Override
    public void update(GameEvent event) {
        interactionController.clearSelection(); 
    }
}