package xyuguyn00.view;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import xyuguyn00.common.GameEvent;
import xyuguyn00.common.Position;
import xyuguyn00.common.Result;
import xyuguyn00.dto.AvailableActionsDto;
import xyuguyn00.dto.GameActionDto;
import xyuguyn00.dto.GameActionType;
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

    public GameView(Game game) {
        this.game = game;
        this.game.addObserver(this); 
        this.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: #2F4F4F;");
        this.dispatcher = GameActionDispatcher.createDefault(game);
        this.validationService = new ActionValidationService(game);
        this.tileRenderer = new TileRenderer(tileSize);
        this.actionMenuFactory = new ActionMenuFactory();
        this.factoryMenuFactory = new FactoryMenuFactory();
        render();
    }

    private void handleTileClick(Position clickedPos, MouseEvent event) {   
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
        Platform.runLater(() -> {
            this.getChildren().clear();
            String[] map = game.getMapDefinition();

            for (int row = 0; row < game.getHeight(); row++) {
                String rowStr = map[row].replace(" ", "");

                for (int col = 0; col < game.getWidth(); col++) {
                    Position pos = new Position(row, col);
                    char terrainChar = rowStr.charAt(col);

                    StackPane tile = createTile(pos, terrainChar);
                    this.add(tile, col, row);
                }
            }
        });
    }

    private StackPane createTile(Position pos, char terrainChar) {
        StackPane tile = new StackPane();

        setupTileMouseHandlers(tile, pos);

        tile.getChildren().add(tileRenderer.createTerrainBackground(terrainChar));
        addBuildingOverlay(tile, pos);
        addHighlights(tile, pos);
        addPathDot(tile, pos);
        addUnit(tile, pos);

        return tile;
    }

    private void setupTileMouseHandlers(StackPane tile, Position pos) {
        tile.setOnMouseClicked(e -> {
            if (shouldOpenFactoryMenu(pos)) {
                showFactoryMenu(pos, tile, e.getScreenX(), e.getScreenY());
                return;
            }

            handleTileClick(pos, e);
        });

        tile.setOnMouseEntered(e -> updateHoveredPath(pos));
    }

    private boolean shouldOpenFactoryMenu(Position pos) {
        if (selectedPosition != null) {
            return false;
        }

        Unit clickedUnit = game.getUnitAt(pos);
        if (clickedUnit != null) {
            return false;
        }

        Building building = game.getBuildingAt(pos);

        return building != null
                && building.getType().equals("Továrna")
                && building.getOwner().equals(game.getCurrentPlayer());
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

    private void addBuildingOverlay(StackPane tile, Position pos) {
        Building building = game.getBuildingAt(pos);

        if (building != null) {
            tile.getChildren().add(tileRenderer.createBuildingOverlay(building));
        }
    }

    private void addHighlights(StackPane tile, Position pos) {
        if (isTargeting && validTargets.contains(pos)) {
            tile.getChildren().add(tileRenderer.createTargetingHighlight());
        } else if (pos.equals(selectedPosition) && previewPosition == null) {
            tile.getChildren().add(tileRenderer.createSelectedHighlight());
        } else if (reachablePositions.contains(pos) && previewPosition == null) {
            tile.getChildren().add(tileRenderer.createReachableHighlight());
        }
    }

    private void addPathDot(StackPane tile, Position pos) {
        if (currentPath.contains(pos)) {
            tile.getChildren().add(tileRenderer.createPathDot());
        }
    }

    private void addUnit(StackPane tile, Position pos) {
        Unit unit = getPreviewAwareUnit(pos);

        if (unit != null) {
            Building buildingOnTile = game.getBuildingAt(pos);
            tile.getChildren().addAll(tileRenderer.createUnitNodes(unit, buildingOnTile));
        }
    }

    private Unit getPreviewAwareUnit(Position pos) {
        Unit unit = game.getUnitAt(pos);

        if (previewPosition == null) {
            return unit;
        }

        if (pos.equals(previewPosition)) {
            return game.getUnitAt(selectedPosition);
        }

        if (pos.equals(selectedPosition)) {
            return null;
        }

        return unit;
    }

    private void showFactoryMenu(Position pos, javafx.scene.Node tile, double screenX, double screenY) {
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