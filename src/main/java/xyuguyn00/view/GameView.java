package xyuguyn00.view;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import xyuguyn00.common.GameEvent;
import xyuguyn00.common.Position;
import xyuguyn00.common.Result;
import xyuguyn00.common.enums.GameActionType;
import xyuguyn00.dto.AvailableActionsDto;
import xyuguyn00.dto.GameActionDto;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.handler.GameActionDispatcher;
import xyuguyn00.service.ActionValidationService;
import xyuguyn00.tool.GameObserver;

import java.util.ArrayList;
import java.util.List;

public class GameView extends GridPane implements GameObserver {
    private final Game game;
    private final int tileSize = 60; 

    private final GameActionDispatcher dispatcher;
    private final ActionValidationService validationService;
    private final TileRenderer tileRenderer;
    private final GameBoardView boardView;
    private final ActionMenuFactory actionMenuFactory;
    private final FactoryMenuFactory factoryMenuFactory;

    // --- Interactivity State ---
    private Position selectedPosition = null;
    private Position previewPosition = null; 
    private List<Position> reachablePositions = new ArrayList<>();

    // Pathfinding UI State
    private Position hoveredPosition = null;
    private List<Position> currentPath = new ArrayList<>();
    
    // --- Combat State ---
    private ContextMenu activeMenu = null;
    private boolean isTargeting = false; 
    private List<Position> validTargets = new ArrayList<>();

    private boolean replayMode = false;

    public GameView(Game game, GameActionDispatcher dispatcher) {
        this.game = game;
        this.game.addObserver(this); 
        this.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: #2F4F4F;");
        this.dispatcher = dispatcher;
        this.validationService = new ActionValidationService(game);
        this.tileRenderer = new TileRenderer(tileSize);
        this.boardView = new GameBoardView(game, tileRenderer);
        this.actionMenuFactory = new ActionMenuFactory();
        this.factoryMenuFactory = new FactoryMenuFactory();

        this.boardView.setOnTileClicked(this::handleTileClick);
        this.boardView.setOnTileHovered(this::updateHoveredPath);
        this.boardView.setOnFactoryMenuRequested(this::showFactoryMenu);

        this.add(boardView, 0, 0);

        render();
    }

    public void setReplayMode(boolean replayMode) {
        this.replayMode = replayMode;
        clearSelection();
    }

    private void handleTileClick(Position clickedPos, MouseEvent event) {   
        if (replayMode) {
            return;
        }

        if (isTargeting) {
            if (validTargets.contains(clickedPos)) {
                Position moveFrom = selectedPosition;
                Position moveTo = previewPosition;
                Position target = clickedPos;

                dispatchAction(
                    GameActionDto.builder(GameActionType.ATTACK)
                        .from(moveFrom)
                        .to(moveTo)
                        .target(target)
                        .build()
                );

            } else {
                // User clicked somewhere else. Cancel targeting and reopen the menu.
                isTargeting = false;
                showActionMenu(previewPosition, event.getScreenX(), event.getScreenY());
                render();
            }
            return;
        }

        // 2. Menu Safety Catch
        if (activeMenu != null && activeMenu.isShowing()) {
            clearSelection();
            return; 
        }

        // 3. Select a Unit
        if (selectedPosition == null) {
            Unit unit = game.getUnitAt(clickedPos);
            if (unit != null && unit.getPlayer().equals(game.getCurrentPlayer()) && !unit.hasMoved()) {
                selectedPosition = clickedPos;
                reachablePositions = game.getReachableTiles(clickedPos);
                render(); 
            }
        } 
        // 4. Preview Move
        else if (previewPosition == null) {
            if (reachablePositions.contains(clickedPos)) {
                previewPosition = clickedPos; 
                showActionMenu(previewPosition, event.getScreenX(), event.getScreenY());
                render(); 
            } else {
                clearSelection(); 
            }
        }
    }

    private void showActionMenu(Position targetPos, double screenX, double screenY) {
        validTargets.clear();

        AvailableActionsDto actions = validationService.getAvailableActions(selectedPosition, targetPos);
        validTargets.addAll(actions.getAttackTargets());

        Building targetBuilding = game.getBuildingAt(targetPos);

        activeMenu = actionMenuFactory.createActionMenu(
                actions,
                targetBuilding,
                this::enterTargetingMode,
                () -> performCapture(targetPos),
                () -> performWait(targetPos),
                this::clearSelection
        );

        activeMenu.setOnHidden(e -> clearSelection());
        activeMenu.show(this, screenX, screenY);
    }

    private void enterTargetingMode() {
        if (activeMenu != null) {
            activeMenu.setOnHidden(null);
            activeMenu.hide();
        }

        isTargeting = true;
        render();
    }

    private void performCapture(Position targetPos) {
        Position moveFrom = selectedPosition;
        Position moveTo = targetPos;

        hideActiveMenuWithoutClearing();

        dispatchAction(
                GameActionDto.builder(GameActionType.CAPTURE)
                        .from(moveFrom)
                        .to(moveTo)
                        .build()
        );
    }

    private void performWait(Position targetPos) {
        Position from = selectedPosition;
        Position to = targetPos;

        hideActiveMenuWithoutClearing();

        if (from != null && to != null) {
            dispatchAction(
                    GameActionDto.builder(GameActionType.WAIT)
                            .from(from)
                            .to(to)
                            .build()
            );
        } else {
            clearSelection();
        }
    }

    private void hideActiveMenuWithoutClearing() {
        if (activeMenu != null) {
            activeMenu.setOnHidden(null);
            activeMenu.hide();
        }
    }

    private boolean dispatchAction(GameActionDto action) {
        Result result = dispatcher.dispatch(action);

        if (result.isFailure()) {
            System.out.println("Action failed: " + result.getMessage());
            clearSelection();
            return false;
        }

        clearSelection();
        return true;
    }

    private void clearSelection() {
        selectedPosition = null;
        previewPosition = null;
        reachablePositions.clear();
        isTargeting = false;
        validTargets.clear();
        
        // Clear path UI
        hoveredPosition = null;
        currentPath.clear();
        
        ContextMenu menuToHide = activeMenu;
        activeMenu = null; 
        if (menuToHide != null && menuToHide.isShowing()) {
            menuToHide.hide();
        }
        
        render();
    }

    private void render() {
        Platform.runLater(() -> boardView.render(createBoardViewState()));
    }

    private BoardViewState createBoardViewState() {
        return new BoardViewState(
                selectedPosition,
                previewPosition,
                new ArrayList<>(reachablePositions),
                new ArrayList<>(currentPath),
                isTargeting,
                new ArrayList<>(validTargets)
        );
    }

    private void updateHoveredPath(Position pos) {
        if (selectedPosition == null || previewPosition != null) {
            return;
        }

        if (reachablePositions.contains(pos)) {
            if (!pos.equals(hoveredPosition)) {
                hoveredPosition = pos;
                currentPath = game.getPath(selectedPosition, pos);
                render();
            }
            return;
        }

        if (hoveredPosition != null) {
            hoveredPosition = null;
            currentPath.clear();
            render();
        }
    }

    private void showFactoryMenu(Position pos, javafx.scene.Node tile, double screenX, double screenY) {
        if (replayMode) {
            return;
        }

        ContextMenu shopMenu = factoryMenuFactory.createFactoryMenu(
                game::getUnitCost,
                type -> game.getPlayerFunds(game.getCurrentPlayer()) < game.getUnitCost(type)
                        || game.getUnitAt(pos) != null,
                type -> dispatchAction(
                        GameActionDto.builder(GameActionType.PURCHASE)
                                .to(pos)
                                .unitType(type)
                                .build()
                )
        );

        shopMenu.show(tile, screenX, screenY);
    }

    @Override
    public void update(GameEvent event) {
        clearSelection(); 
    }
}