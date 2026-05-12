package xyuguyn00.view;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import xyuguyn00.common.Position;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GameBoardView extends GridPane {
    private final Game game;
    private final TileRenderer tileRenderer;

    private BiConsumer<Position, MouseEvent> onTileClicked;
    private Consumer<Position> onTileHovered;
    private FactoryMenuRequestHandler onFactoryMenuRequested;

    public GameBoardView(Game game, TileRenderer tileRenderer) {
        this.game = game;
        this.tileRenderer = tileRenderer;
    }

    public void setOnTileClicked(BiConsumer<Position, MouseEvent> onTileClicked) {
        this.onTileClicked = onTileClicked;
    }

    public void setOnTileHovered(Consumer<Position> onTileHovered) {
        this.onTileHovered = onTileHovered;
    }

    public void setOnFactoryMenuRequested(FactoryMenuRequestHandler onFactoryMenuRequested) {
        this.onFactoryMenuRequested = onFactoryMenuRequested;
    }

    public void render(BoardViewState state) {
        getChildren().clear();

        String[] map = game.getMapDefinition();

        for (int row = 0; row < game.getHeight(); row++) {
            String rowStr = map[row].replace(" ", "");

            for (int col = 0; col < game.getWidth(); col++) {
                Position pos = new Position(row, col);
                TerrainType terrainType = TerrainType.fromSymbol(rowStr.charAt(col));

                StackPane tile = createTile(pos, terrainType, state, map); 
                add(tile, col, row);
            }
        }
    }

    private StackPane createTile(Position pos, TerrainType terrainType, BoardViewState state, String[] map) {
        StackPane tile = new StackPane();

        setupTileMouseHandlers(tile, pos, state);

        // Pass the map to the renderer
        tile.getChildren().add(tileRenderer.createTerrainBackground(terrainType, pos, map));
        
        addBuildingOverlay(tile, pos);
        addHighlights(tile, pos, state);
        addPathDot(tile, pos, state);
        addUnit(tile, pos, state);

        return tile;
    }

    private void setupTileMouseHandlers(StackPane tile, Position pos, BoardViewState state) {
        tile.setOnMouseClicked(e -> {
            if (shouldOpenFactoryMenu(pos, state)) {
                if (onFactoryMenuRequested != null) {
                    onFactoryMenuRequested.handle(pos, tile, e.getScreenX(), e.getScreenY());
                }
                return;
            }

            if (onTileClicked != null) {
                onTileClicked.accept(pos, e);
            }
        });

        tile.setOnMouseEntered(e -> {
            if (onTileHovered != null) {
                onTileHovered.accept(pos);
            }
        });
    }

    private boolean shouldOpenFactoryMenu(Position pos, BoardViewState state) {
        if (state.getSelectedPosition() != null) {
            return false;
        }

        Unit clickedUnit = game.getUnitAt(pos);
        if (clickedUnit != null) {
            return false;
        }

        Building building = game.getBuildingAt(pos);

        return building != null
            && building.getType() == BuildingType.FACTORY
            && building.getOwner() == game.getCurrentPlayer();
    }

    private void addBuildingOverlay(StackPane tile, Position pos) {
        Building building = game.getBuildingAt(pos);

        if (building != null) {
            tile.getChildren().add(tileRenderer.createBuildingOverlay(building));
        }
    }

    private void addHighlights(StackPane tile, Position pos, BoardViewState state) {
        if (state.isTargeting() && state.getValidTargets().contains(pos)) {
            tile.getChildren().add(tileRenderer.createTargetingHighlight());
        } 
        else if (pos.equals(state.getSelectedPosition()) && state.getPreviewPosition() == null) {
            tile.getChildren().add(tileRenderer.createSelectedHighlight());
        } 
        else if (state.getReachablePositions().contains(pos) && state.getPreviewPosition() == null) {
            tile.getChildren().add(tileRenderer.createReachableHighlight());
        }
    }

    private void addPathDot(StackPane tile, Position pos, BoardViewState state) {
        if (state.getCurrentPath().contains(pos)) {
            tile.getChildren().add(tileRenderer.createPathDot());
        }
    }

    private void addUnit(StackPane tile, Position pos, BoardViewState state) {
        Unit unit = getPreviewAwareUnit(pos, state);

        if (unit != null) {
            Building buildingOnTile = game.getBuildingAt(pos);
            tile.getChildren().addAll(tileRenderer.createUnitNodes(unit, buildingOnTile));
        }
    }

    private Unit getPreviewAwareUnit(Position pos, BoardViewState state) {
        Unit unit = game.getUnitAt(pos);

        if (state.getPreviewPosition() == null) {
            return unit;
        }

        if (pos.equals(state.getPreviewPosition())) {
            return game.getUnitAt(state.getSelectedPosition());
        }

        if (pos.equals(state.getSelectedPosition())) {
            return null;
        }

        return unit;
    }
}