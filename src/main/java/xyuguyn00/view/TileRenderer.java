package xyuguyn00.view;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.common.enums.UnitType;
import xyuguyn00.game.Building;
import xyuguyn00.game.Unit;

public class TileRenderer {
    private final int tileSize;

    public TileRenderer(int tileSize) {
        this.tileSize = tileSize;
    }

    public Rectangle createTerrainBackground(TerrainType terrainType) {
        Rectangle background = new Rectangle(tileSize, tileSize);
        background.setFill(getTerrainColor(terrainType));
        background.setStroke(Color.BLACK);
        background.setStrokeWidth(0.5);
        return background;
    }

    public Node createBuildingOverlay(Building building) {
        Rectangle overlay = new Rectangle(tileSize - 12, tileSize - 12);
        overlay.setFill(Color.TRANSPARENT);
        overlay.setStrokeWidth(4);

        if (building.getOwner() == PlayerId.PLAYER_1) {
            overlay.setStroke(Color.DARKBLUE);
        } else if (building.getOwner() == PlayerId.PLAYER_2) {
            overlay.setStroke(Color.DARKRED);
        } else {
            overlay.setStroke(Color.WHITE);
        }

        return overlay;
    }

    public Node createTargetingHighlight() {
        Rectangle crosshair = new Rectangle(tileSize, tileSize);
        crosshair.setFill(Color.rgb(255, 0, 0, 0.4));
        crosshair.setStroke(Color.RED);
        crosshair.setStrokeWidth(3);
        return crosshair;
    }

    public Node createSelectedHighlight() {
        Rectangle highlight = new Rectangle(tileSize, tileSize);
        highlight.setFill(Color.rgb(255, 255, 0, 0.4));
        return highlight;
    }

    public Node createReachableHighlight() {
        Rectangle highlight = new Rectangle(tileSize, tileSize);
        highlight.setFill(Color.rgb(255, 255, 255, 0.5));
        highlight.setStroke(Color.WHITE);
        highlight.setStrokeWidth(2);
        return highlight;
    }

    public Node createPathDot() {
        Circle pathDot = new Circle(tileSize / 8.0);
        pathDot.setFill(Color.WHITE);
        pathDot.setOpacity(0.8);
        return pathDot;
    }

    public List<Node> createUnitNodes(Unit unit, Building buildingOnTile) {
        List<Node> nodes = new ArrayList<>();

        if (buildingOnTile != null && buildingOnTile.getCapturePoints() < 20) {
            Text cpLabel = new Text("CP: " + buildingOnTile.getCapturePoints());
            cpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            cpLabel.setFill(Color.CYAN);
            cpLabel.setTranslateY(-22);
            nodes.add(cpLabel);
        }

        Circle token = new Circle(tileSize / 2.5);
        token.setFill(unit.getPlayer() == PlayerId.PLAYER_1 ? Color.DARKBLUE : Color.DARKRED);
        token.setStroke(Color.WHITE);
        token.setStrokeWidth(2);

        Text label = new Text(getUnitSymbol(unit.getUnitType()));
        label.setFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Text hpLabel = new Text(unit.getHp() + " HP");
        hpLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        hpLabel.setTranslateY(18);

        if (unit.getHp() > 50) {
            hpLabel.setFill(Color.LIGHTGREEN);
        } else if (unit.getHp() > 20) {
            hpLabel.setFill(Color.YELLOW);
        } else {
            hpLabel.setFill(Color.RED);
        }

        if (unit.hasMoved()) {
            token.setOpacity(0.4);
            label.setOpacity(0.4);
            hpLabel.setOpacity(0.4);
        }

        nodes.add(token);
        nodes.add(label);
        nodes.add(hpLabel);

        return nodes;
    }

    private Color getTerrainColor(TerrainType terrainType) {
        return switch (terrainType) {
            case PLAIN -> Color.web("#90EE90");
            case FOREST -> Color.web("#228B22");
            case MOUNTAIN -> Color.web("#808080");
            case WATER -> Color.web("#4169E1");
            case CITY -> Color.web("#D3D3D3");
            case FACTORY -> Color.web("#CD853F");
            case HQ -> Color.web("#FFD700");
        };
    }

    private String getUnitSymbol(UnitType type) {
        return switch (type) {
            case INFANTRY -> "I";
            case TANK -> "T";
            case ARTILLERY -> "A";
        };
    }
}