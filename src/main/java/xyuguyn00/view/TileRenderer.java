package xyuguyn00.view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.Position;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.game.Building;
import xyuguyn00.game.Unit;

public class TileRenderer {
    private final int tileSize;
    private final AssetManager assetManager;

    public TileRenderer(int tileSize, AssetManager assetManager) {
        this.tileSize = tileSize;
        this.assetManager = assetManager;
    }

    public Node createTerrainBackground(TerrainType terrainType, Position pos, String[] map) {
        int x = pos.getX();
        int y = pos.getY();

        if (terrainType == TerrainType.FOREST || terrainType == TerrainType.MOUNTAIN) {
            javafx.scene.layout.StackPane layeredTile = new javafx.scene.layout.StackPane();

            ImageView baseGrass = new ImageView(assetManager.getGrassVariation(y, x));
            baseGrass.setFitWidth(tileSize);
            baseGrass.setFitHeight(tileSize);
            baseGrass.setSmooth(false);

            ImageView topFeature = new ImageView(assetManager.getTerrain(terrainType));
            topFeature.setFitWidth(tileSize);
            topFeature.setFitHeight(tileSize);
            topFeature.setSmooth(false);

            layeredTile.getChildren().addAll(baseGrass, topFeature);
            return layeredTile;
        }

        Image texture;
        if (terrainType == TerrainType.WATER) {
            // Pass the map array to the helper method
            boolean up = isWater(x, y - 1, map);
            boolean down = isWater(x, y + 1, map);
            boolean left = isWater(x - 1, y, map);
            boolean right = isWater(x + 1, y, map);
            texture = assetManager.getContextualWater(up, down, left, right);
        } 
        else if (terrainType == TerrainType.PLAIN || terrainType == TerrainType.CITY || terrainType == TerrainType.FACTORY || terrainType == TerrainType.HQ) {
            texture = assetManager.getGrassVariation(y, x);
        } 
        else {
            texture = assetManager.getTerrain(terrainType);
        }

        ImageView imageView = new ImageView(texture);
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);
        imageView.setSmooth(false); 
        return imageView;
    }

    // Helper method now uses the passed-in map array
    private boolean isWater(int checkRow, int checkCol, String[] map) {
        if (checkRow < 0 || checkRow >= map.length) return true;
        
        String mapRow = map[checkRow].replace(" ", "");
        if (checkCol < 0 || checkCol >= mapRow.length()) return true;

        char symbol = mapRow.charAt(checkCol);
        return TerrainType.fromSymbol(symbol) == TerrainType.WATER;
    }

    public Node createBuildingOverlay(Building building) {
        ImageView imageView = new ImageView(assetManager.getBuilding(building.getType(), building.getOwner()));
        imageView.setFitWidth(tileSize);
        imageView.setFitHeight(tileSize);
        imageView.setSmooth(false);
        return imageView;
    }

    public List<Node> createUnitNodes(Unit unit, Building buildingOnTile) {
        List<Node> nodes = new ArrayList<>();

        // Base Unit Texture
        Image unitImage;
        if (unit.hasMoved()) {
            unitImage = assetManager.getUnitMoved(unit.getUnitType());
        } else {
            unitImage = assetManager.getUnit(unit.getUnitType(), unit.getPlayer());
        }

        ImageView unitView = new ImageView(unitImage);
        unitView.setFitWidth(tileSize - 10); 
        unitView.setFitHeight(tileSize - 10);
        unitView.setSmooth(false);
        nodes.add(unitView);

        // CP Badge (Top Left) - Only shows if capturing is in progress
        if (buildingOnTile != null && buildingOnTile.getCapturePoints() < 20) {
            StackPane cpBadge = new StackPane();
            
            Rectangle cpBg = new Rectangle(24, 14, Color.rgb(0, 100, 200, 0.85)); // Solid blue background
            cpBg.setArcWidth(4); 
            cpBg.setArcHeight(4);
            cpBg.setStroke(Color.WHITE);
            cpBg.setStrokeWidth(1);

            Text cpLabel = new Text("C:" + buildingOnTile.getCapturePoints());
            cpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            cpLabel.setFill(Color.WHITE);

            cpBadge.getChildren().addAll(cpBg, cpLabel);
            
            // Push to the top-left corner of the tile
            cpBadge.setTranslateX(-tileSize / 2.0 + 14);
            cpBadge.setTranslateY(-tileSize / 2.0 + 9);
            
            nodes.add(cpBadge);
        }

        // HP Badge 
        StackPane hpBadge = new StackPane();
        
        Rectangle hpBg = new Rectangle(22, 14, Color.rgb(0, 0, 0, 0.75)); // Dark background
        hpBg.setArcWidth(4);
        hpBg.setArcHeight(4);

        Text hpLabel = new Text(String.valueOf(unit.getHp()));
        hpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        
        // Color coding the text based on health status
        if (unit.getHp() > 50) {
            hpLabel.setFill(Color.LIGHTGREEN);
        } else if (unit.getHp() > 20) {
            hpLabel.setFill(Color.YELLOW);
        } else {
            hpLabel.setFill(Color.RED);
            hpBg.setStroke(Color.RED); 
            hpBg.setStrokeWidth(1);
        }

        if (unit.hasMoved()) {
            hpBadge.setOpacity(0.6); 
        }

        hpBadge.getChildren().addAll(hpBg, hpLabel);
        
        // Push to the bottom-right corner of the tile
        hpBadge.setTranslateX(tileSize / 2.0 - 13);
        hpBadge.setTranslateY(tileSize / 2.0 - 9);

        nodes.add(hpBadge);

        return nodes;
    }

    public Circle createPathDot() {
        Circle dot = new Circle(tileSize / 6.0, Color.WHITE);
        dot.setOpacity(0.8);
        return dot;
    }

    public Node createSelectedHighlight() {
        ImageView highlight = new ImageView(assetManager.getTileSelect());
        highlight.setFitWidth(tileSize);
        highlight.setFitHeight(tileSize);
        highlight.setSmooth(false); 
        
        return highlight;
    }

    public Rectangle createReachableHighlight() {
        Rectangle highlight = new Rectangle(tileSize, tileSize);
        highlight.setFill(Color.WHITE);
        highlight.setOpacity(0.3);
        return highlight;
    }

    public Rectangle createTargetingHighlight() {
        Rectangle highlight = new Rectangle(tileSize, tileSize);
        highlight.setFill(Color.RED);
        highlight.setOpacity(0.4);
        return highlight;
    }
}