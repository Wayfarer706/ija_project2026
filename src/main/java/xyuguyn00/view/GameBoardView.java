package xyuguyn00.view;

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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameBoardView extends StackPane {
    private final Game game;
    private final TileRenderer tileRenderer;

    // Separated layers for massive performance gains
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
        
        // Stack the layers: Terrain on the bottom, Units/UI on top
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

        // Render heavy terrain ONLY on the first frame
        if (!isTerrainInitialized) {
            initializeTerrainLayer(map);
            isTerrainInitialized = true;
        }

        // Clear only the lightweight dynamic elements
        dynamicLayer.getChildren().clear();

        for (int r = 0; r < map.length; r++) {
            String rowString = map[r].replace(" ", "");
            for (int c = 0; c < rowString.length(); c++) {
                Position pos = new Position(c, r);

                // Create a container strictly for the dynamic elements of this specific tile
                StackPane tileDynamicContent = new StackPane();
                tileDynamicContent.setPrefSize(60, 60); 

                // Attach mouse handlers to this top layer so it catches all user input
                setupTileMouseHandlers(tileDynamicContent, pos, state);

                // Add elements that can change state or position
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
        // Buildings are rendered dynamically because their Capture Points and Ownership change
        Building building = game.getBuildingAt(pos);
        if (building != null) {
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