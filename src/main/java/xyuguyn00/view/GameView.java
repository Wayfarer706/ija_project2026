package xyuguyn00.view;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.GameEvent;
import xyuguyn00.common.Position;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.tool.GameObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * The graphical representation of the game board.
 * Handles rendering, mouse interactivity, and path visualization.
 */
public class GameView extends GridPane implements GameObserver {
    private final Game game;
    private final int tileSize = 60; 

    // --- Interactivity State ---
    private Position selectedPosition = null;
    private List<Position> reachablePositions = new ArrayList<>();

    public GameView(Game game) {
        this.game = game;
        this.game.addObserver(this); 
        this.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-background-color: #2F4F4F;");
        render();
    }

    private void handleTileClick(Position clickedPos) {
        if (selectedPosition == null) {
            Unit unit = game.getUnitAt(clickedPos);
            // Unit must exist, belong to active player, AND not have moved yet
            if (unit != null && unit.getPlayer().equals(game.getCurrentPlayer()) && !unit.hasMoved()) {
                selectedPosition = clickedPos;
                reachablePositions = game.getReachableTiles(clickedPos);
                render(); 
            }
        } else {
            if (reachablePositions.contains(clickedPos)) {
                game.moveUnit(selectedPosition, clickedPos);
            }
            
            selectedPosition = null;
            reachablePositions.clear();
            render();
        }
    }

    private void render() {
        this.getChildren().clear();
        String[] map = game.getMapDefinition();

        for (int row = 0; row < game.getHeight(); row++) {
            String rowStr = map[row].replace(" ", "");
            
            for (int col = 0; col < game.getWidth(); col++) {
                char terrainChar = rowStr.charAt(col);
                Position pos = new Position(row, col); 

                StackPane tile = new StackPane();
                tile.setOnMouseClicked(event -> handleTileClick(pos));

                // 1. Draw the Background Terrain
                Rectangle bg = new Rectangle(tileSize, tileSize);
                bg.setFill(getTerrainColor(terrainChar));
                bg.setStroke(Color.BLACK); 
                bg.setStrokeWidth(0.5);
                tile.getChildren().add(bg);

                // 2. Draw Visual Highlights
                if (pos.equals(selectedPosition)) {
                    Rectangle highlight = new Rectangle(tileSize, tileSize);
                    highlight.setFill(Color.rgb(255, 255, 0, 0.4)); 
                    tile.getChildren().add(highlight);
                } else if (reachablePositions.contains(pos)) {
                    Rectangle pathTarget = new Rectangle(tileSize, tileSize);
                    pathTarget.setFill(Color.rgb(255, 255, 255, 0.5)); 
                    pathTarget.setStroke(Color.WHITE);
                    pathTarget.setStrokeWidth(2);
                    tile.getChildren().add(pathTarget);
                }

                // 3. Draw the Unit
                Unit unit = game.getUnitAt(pos);
                if (unit != null) {
                    Circle token = new Circle(tileSize / 2.5);
                    token.setFill(unit.getPlayer().equals("Player 1") ? Color.DARKBLUE : Color.DARKRED);
                    token.setStroke(Color.WHITE);
                    token.setStrokeWidth(2);

                    // Dim the unit if it has already moved
                    if (unit.hasMoved()) {
                        token.setOpacity(0.4); 
                    }

                    Text label = new Text(unit.getType().substring(0, 1));
                    label.setFill(Color.WHITE);
                    label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    
                    if (unit.hasMoved()) {
                        label.setOpacity(0.4);
                    }
                    
                    tile.getChildren().addAll(token, label);
                }

                this.add(tile, col, row); 
            }
        }
    }

    private Color getTerrainColor(char t) {
        return switch (t) {
            case 'P' -> Color.web("#90EE90"); // Plain
            case 'F' -> Color.web("#228B22"); // Forest
            case 'M' -> Color.web("#808080"); // Mountain
            case 'W' -> Color.web("#4169E1"); // Water
            case 'C' -> Color.web("#D3D3D3"); // City
            case 'T' -> Color.web("#CD853F"); // Factory
            case 'H' -> Color.web("#FFD700"); // HQ
            default -> Color.WHITE;
        };
    }

    @Override
    public void update(GameEvent event) {
        render();
    }
}