/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: The core visual grid of the game. Implements a layered rendering 
 * architecture by separating static terrain from rapidly changing dynamic elements (units, highlights).
 */
package xyuguyn00.view.scene;

import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import xyuguyn00.common.Position;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.view.render.TileRenderer;
import xyuguyn00.view.render.ViewConstants;
import xyuguyn00.view.state.BoardViewState;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameBoardView extends StackPane {
    private final Game game;
    private final TileRenderer tileRenderer;

    private final GridPane staticTerrainLayer;
    private final GridPane dynamicLayer;
    private boolean isTerrainInitialized = false;

    private BiConsumer<Position, MouseEvent> onTileClicked;
    private Consumer<Position> onTileHovered;

    public GameBoardView(Game game, TileRenderer tileRenderer) {
        this.game = game;
        this.tileRenderer = tileRenderer;
        
        this.staticTerrainLayer = new GridPane();
        this.dynamicLayer = new GridPane();
        
        this.getChildren().addAll(staticTerrainLayer, dynamicLayer);
    }

    public void setOnTileClicked(BiConsumer<Position, MouseEvent> handler) {
        this.onTileClicked = handler;
    }

    public void setOnTileHovered(Consumer<Position> handler) {
        this.onTileHovered = handler;
    }

    public void render(BoardViewState state) {
        String[] map = game.getMapDefinition();

        // Rendering of terrain layer happens only once at the start of the game
        if (!isTerrainInitialized) {
            initializeTerrainLayer(map);
            isTerrainInitialized = true;
        }

        // The dynamic layer is wiped and redrawn every time the mouse moves.
        dynamicLayer.getChildren().clear();

        for (int r = 0; r < map.length; r++) {
            String rowString = map[r].replace(" ", "");
            for (int c = 0; c < rowString.length(); c++) {
                Position pos = new Position(c, r);

                StackPane tileDynamicContent = new StackPane();
                tileDynamicContent.setPrefSize(ViewConstants.TILE_SIZE, ViewConstants.TILE_SIZE);

                // Mouse handlers are attached to the dynamic container because it always sits 
                // on top of the Z-index, guaranteeing it catches all hover/click events.
                setupTileMouseHandlers(tileDynamicContent, pos, state);

                addBuildingOverlay(tileDynamicContent, pos);
                addHighlights(tileDynamicContent, pos, state);
                addPathDot(tileDynamicContent, pos, state);
                addUnit(tileDynamicContent, pos, state);

                dynamicLayer.add(tileDynamicContent, c, r);
            }
        }
    }

    private void initializeTerrainLayer(String[] map) {
        for (int r = 0; r < map.length; r++) {
            String rowString = map[r].replace(" ", "");
            for (int c = 0; c < rowString.length(); c++) {
                Position pos = new Position(c, r);
                TerrainType terrainType = TerrainType.fromSymbol(rowString.charAt(c));
                
                Node terrainNode = tileRenderer.createTerrainBackground(terrainType, pos, map);
                staticTerrainLayer.add(terrainNode, c, r);
            }
        }
    }

    private void setupTileMouseHandlers(StackPane tile, Position pos, BoardViewState state) {
        tile.setOnMouseEntered(e -> {
            if (onTileHovered != null) {
                onTileHovered.accept(pos);
            }
        });

        tile.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && onTileClicked != null) {
                onTileClicked.accept(pos, e);
            }
        });
    }

    private void addBuildingOverlay(StackPane tile, Position pos) {
        Building building = game.getBuildingAt(pos);
        if (building != null) {
            // Buildings must be on the dynamic layer rather than the static layer 
            // because their textures change immediately when captured by a player.
            tile.getChildren().add(tileRenderer.createBuildingOverlay(building));
        }
    }

    private void addUnit(StackPane tile, Position pos, BoardViewState state) {
        Unit unit = game.getUnitAt(pos);
        if (unit != null) {
            Building buildingOnTile = game.getBuildingAt(pos);
            List<Node> unitNodes = tileRenderer.createUnitNodes(unit, buildingOnTile);
            tile.getChildren().addAll(unitNodes);
        }
    }

    private void addHighlights(StackPane tile, Position pos, BoardViewState state) {
        if (pos.equals(state.getSelectedPosition()) || pos.equals(state.getPreviewPosition())) {
            tile.getChildren().add(tileRenderer.createSelectedHighlight());
        }

        if (state.isTargeting() && state.getValidTargets().contains(pos)) {
            tile.getChildren().add(tileRenderer.createTargetingHighlight());
        } else if (!state.isTargeting() && state.getReachablePositions().contains(pos)) {
            tile.getChildren().add(tileRenderer.createReachableHighlight());
        }
    }

    private void addPathDot(StackPane tile, Position pos, BoardViewState state) {
        if (state.getCurrentPath() != null && state.getCurrentPath().contains(pos)) {
            tile.getChildren().add(tileRenderer.createPathDot());
        }
    }
}