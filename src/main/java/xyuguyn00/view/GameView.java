package xyuguyn00.view;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
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
import xyuguyn00.game.Building;
import xyuguyn00.game.Game;
import xyuguyn00.game.Unit;
import xyuguyn00.tool.GameObserver;

import java.util.ArrayList;
import java.util.List;

public class GameView extends GridPane implements GameObserver {
    private final Game game;
    private final int tileSize = 60; 

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
        render();
    }

    private void handleTileClick(Position clickedPos, MouseEvent event) {   
        if (isTargeting) {
            if (validTargets.contains(clickedPos)) {
                Position moveFrom = selectedPosition;
                Position moveTo = previewPosition;
                Position target = clickedPos;

                // Commit the move, then strike using the cached variables
                game.moveUnit(moveFrom, moveTo); 
                game.attack(moveTo, target); 
                
                clearSelection(); 
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
        activeMenu = new ContextMenu();
        activeMenu.setStyle("-fx-base: #3c3c3c; -fx-font-size: 14px; -fx-font-weight: bold;");

        // --- CALCULATE VALID COMBAT TARGETS ---
        validTargets.clear();
        Unit attacker = game.getUnitAt(selectedPosition);
        
        // Artillery cannot move and attack in the same turn.
        boolean canAttack = true;
        if (attacker.getType().equals("Dělostřelectvo") && !selectedPosition.equals(previewPosition)) {
            canAttack = false; 
        }

        if (canAttack) {
            for (int r = 0; r < game.getHeight(); r++) {
                for (int c = 0; c < game.getWidth(); c++) {
                    Position p = new Position(r, c);
                    Unit targetUnit = game.getUnitAt(p);
                    
                    // Check if there is an enemy at this coordinate
                    if (targetUnit != null && !targetUnit.getPlayer().equals(attacker.getPlayer())) {
                        // Calculate Manhattan distance from the PREVIEW position, not the start position!
                        int distance = Math.abs(previewPosition.getX() - p.getX()) + 
                                       Math.abs(previewPosition.getY() - p.getY());
                                       
                        if (distance >= attacker.getMinAttackRange() && distance <= attacker.getMaxAttackRange()) {
                            validTargets.add(p);
                        }
                    }
                }
            }
        }

        // --- DYNAMIC MENU OPTIONS ---

        if (!validTargets.isEmpty()) {
            MenuItem attackItem = new MenuItem("Attack");
            attackItem.setOnAction(e -> {
                if (activeMenu != null) activeMenu.setOnHidden(null);
                activeMenu.hide();
                isTargeting = true; // Enter targeting mode!
                render();
            });
            activeMenu.getItems().add(attackItem);
        }

        Building targetBuilding = game.getBuildingAt(previewPosition);
        if (targetBuilding != null && attacker.getType().equals("Pěchota") && !targetBuilding.getOwner().equals(attacker.getPlayer())) {
            MenuItem captureItem = new MenuItem("Capture (" + targetBuilding.getCapturePoints() + " CP)");
            captureItem.setOnAction(e -> {
                Position moveFrom = selectedPosition;
                Position moveTo = previewPosition;

                if (activeMenu != null) activeMenu.setOnHidden(null);
                activeMenu.hide();
                
                game.moveUnit(moveFrom, moveTo);
                game.captureBuilding(moveTo);
                clearSelection();
            });
            activeMenu.getItems().add(captureItem);
        }

        MenuItem waitItem = new MenuItem("Wait");
        waitItem.setOnAction(e -> {
            Position from = selectedPosition;
            Position to = previewPosition;
            
            if (activeMenu != null) activeMenu.setOnHidden(null); 
            clearSelection(); 
            
            if (from != null && to != null) {
                game.moveUnit(from, to); 
            }
        });

        MenuItem cancelItem = new MenuItem("Cancel");
        cancelItem.setOnAction(e -> clearSelection());

        activeMenu.setOnHidden(e -> clearSelection());
        activeMenu.getItems().addAll(waitItem, cancelItem);
        activeMenu.show(this, screenX, screenY);
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
                    char terrainChar = rowStr.charAt(col);
                    Position pos = new Position(row, col); 

                    StackPane tile = new StackPane();
                    tile.setOnMouseClicked(e -> { 
                        Position clickedPos = pos;
                        Unit clickedUnit = game.getUnitAt(clickedPos);

                        if (selectedPosition == null) {
                            if (clickedUnit != null && clickedUnit.getPlayer().equals(game.getCurrentPlayer()) && !clickedUnit.hasMoved()) {
                                selectedPosition = clickedPos;
                                
                                reachablePositions = game.getReachableTiles(clickedPos); 
                                
                                render(); 
                            } else if (clickedUnit == null) {
                                Building b = game.getBuildingAt(clickedPos);
                                if (b != null && b.getType().equals("Továrna") && b.getOwner().equals(game.getCurrentPlayer())) {
                                    showFactoryMenu(clickedPos, tile, e.getScreenX(), e.getScreenY());
                                } else {
                                    clearSelection();
                                }
                            }
                        } else {
                            handleTileClick(pos, e);
                        }
                    });

                    // Track mouse hover for path drawing
                    tile.setOnMouseEntered(e -> {
                        // Only draw paths if a unit is selected, but hasn't finalized a move yet
                        if (selectedPosition != null && previewPosition == null) {
                            if (reachablePositions.contains(pos)) {
                                if (!pos.equals(hoveredPosition)) {
                                    hoveredPosition = pos;
                                    currentPath = game.getPath(selectedPosition, pos);
                                    render(); // Re-render to show the breadcrumbs
                                }
                            } else if (hoveredPosition != null) {
                                // Mouse left the valid movement area, clear the path
                                hoveredPosition = null;
                                currentPath.clear();
                                render();
                            }
                        }
                    });

                    // Background
                    Rectangle bg = new Rectangle(tileSize, tileSize);
                    bg.setFill(getTerrainColor(terrainChar));
                    bg.setStroke(Color.BLACK); 
                    bg.setStrokeWidth(0.5);
                    tile.getChildren().add(bg);
                
                    Building building = game.getBuildingAt(pos);
                    if (building != null) {
                        Rectangle bldgOverlay = new Rectangle(tileSize - 12, tileSize - 12);
                        bldgOverlay.setFill(Color.TRANSPARENT);
                        bldgOverlay.setStrokeWidth(4);
                        
                        if (building.getOwner().equals("Player 1")) bldgOverlay.setStroke(Color.DARKBLUE);
                        else if (building.getOwner().equals("Player 2")) bldgOverlay.setStroke(Color.DARKRED);
                        else bldgOverlay.setStroke(Color.WHITE); // Neutral
                        
                        tile.getChildren().add(bldgOverlay);
                    }

                    // Highlights
                    if (isTargeting && validTargets.contains(pos)) {
                        // Draw red targeting crosshair overlay
                        Rectangle crosshair = new Rectangle(tileSize, tileSize);
                        crosshair.setFill(Color.rgb(255, 0, 0, 0.4)); 
                        crosshair.setStroke(Color.RED);
                        crosshair.setStrokeWidth(3);
                        tile.getChildren().add(crosshair);
                    } else if (pos.equals(selectedPosition) && previewPosition == null) {
                        Rectangle highlight = new Rectangle(tileSize, tileSize);
                        highlight.setFill(Color.rgb(255, 255, 0, 0.4)); 
                        tile.getChildren().add(highlight);
                    } else if (reachablePositions.contains(pos) && previewPosition == null) {
                        Rectangle pathTarget = new Rectangle(tileSize, tileSize);
                        pathTarget.setFill(Color.rgb(255, 255, 255, 0.5)); 
                        pathTarget.setStroke(Color.WHITE);
                        pathTarget.setStrokeWidth(2);
                        tile.getChildren().add(pathTarget);
                    } else if (reachablePositions.contains(pos) && previewPosition == null) {
                        Rectangle pathTarget = new Rectangle(tileSize, tileSize);
                        pathTarget.setFill(Color.rgb(255, 255, 255, 0.5)); 
                        pathTarget.setStroke(Color.WHITE);
                        pathTarget.setStrokeWidth(2);
                        tile.getChildren().add(pathTarget);
                    }

                    // Draw breadcrumb dots for the movement path
                    if (currentPath.contains(pos)) {
                        Circle pathDot = new Circle(tileSize / 8.0); 
                        pathDot.setFill(Color.WHITE);
                        pathDot.setOpacity(0.8);
                        tile.getChildren().add(pathDot);
                    }

                    // Unit Rendering (with preview logic)
                    Unit unit = game.getUnitAt(pos);
                    if (previewPosition != null) {
                        if (pos.equals(previewPosition)) {
                            unit = game.getUnitAt(selectedPosition); 
                        } else if (pos.equals(selectedPosition)) {
                            unit = null; 
                        }
                    }

                    if (unit != null) {
                        Circle token = new Circle(tileSize / 2.5);
                        token.setFill(unit.getPlayer().equals("Player 1") ? Color.DARKBLUE : Color.DARKRED);
                        token.setStroke(Color.WHITE);
                        token.setStrokeWidth(2);

                        Text label = new Text(unit.getType().substring(0, 1));
                        label.setFill(Color.WHITE);
                        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                        
                        Text hpLabel = new Text(unit.getHp() + " HP");
                        hpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                        hpLabel.setTranslateY(18); 
                        
                        if (unit.getHp() > 50) hpLabel.setFill(Color.LIGHTGREEN);
                        else if (unit.getHp() > 20) hpLabel.setFill(Color.YELLOW);
                        else hpLabel.setFill(Color.RED);

                        Building b = game.getBuildingAt(pos);
                        if (b != null && b.getCapturePoints() < 20) {
                            Text cpLabel = new Text("CP: " + b.getCapturePoints());
                            cpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                            cpLabel.setFill(Color.CYAN);
                            cpLabel.setTranslateY(-22); // Place it above the unit icon
                            tile.getChildren().add(cpLabel);
                        }

                        if (unit.hasMoved()) {
                            token.setOpacity(0.4); 
                            label.setOpacity(0.4);
                            hpLabel.setOpacity(0.4);
                        }
                        
                        tile.getChildren().addAll(token, label, hpLabel);
                    }

                    this.add(tile, col, row); 
                }
            }
        });
    }

    private void showFactoryMenu(Position pos, javafx.scene.Node tile, double screenX, double screenY) {
        javafx.scene.control.ContextMenu shopMenu = new javafx.scene.control.ContextMenu();

        // The units available to build
        String[] buildableUnits = {"Pěchota", "Tank", "Dělostřelectvo"};

        for (String type : buildableUnits) {
            int cost = game.getUnitCost(type);
            javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem(type + " (" + cost + " G)");
            
            // Disable the button if the player doesn't have enough gold
            if (game.getPlayerFunds(game.getCurrentPlayer()) < cost) {
                item.setDisable(true);
            }
            
            item.setOnAction(event -> {
                game.purchaseUnit(type, pos);
                shopMenu.hide();
            });
            shopMenu.getItems().add(item);
        }

        javafx.scene.control.MenuItem cancelItem = new javafx.scene.control.MenuItem("Zrušit");
        cancelItem.setOnAction(event -> shopMenu.hide());
        shopMenu.getItems().add(cancelItem);

        shopMenu.show(tile, screenX, screenY);
    }

    private Color getTerrainColor(char t) {
        return switch (t) {
            case 'P' -> Color.web("#90EE90");
            case 'F' -> Color.web("#228B22");
            case 'M' -> Color.web("#808080");
            case 'W' -> Color.web("#4169E1");
            case 'C' -> Color.web("#D3D3D3");
            case 'T' -> Color.web("#CD853F");
            case 'H' -> Color.web("#FFD700");
            default -> Color.WHITE;
        };
    }

    @Override
    public void update(GameEvent event) {
        clearSelection(); 
    }
}