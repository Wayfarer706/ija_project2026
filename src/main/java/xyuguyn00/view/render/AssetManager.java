/**
 * Project: Advance Wars Clone
 * Authors: Nazar Yuguy
 * Description: Implements the Flyweight design pattern for graphical assets. 
 * It ensures that heavy JavaFX Image objects are loaded from the hard drive 
 * exactly once and cached in memory, preventing severe performance drops and 
 * memory leaks during rapid grid redraws.
 */
package xyuguyn00.view.render;

import javafx.scene.image.Image;
import xyuguyn00.common.enums.BuildingType;
import xyuguyn00.common.enums.PlayerId;
import xyuguyn00.common.enums.TerrainType;
import xyuguyn00.common.enums.UnitType;

import java.util.EnumMap;
import java.util.Map;

public class AssetManager {
    // EnumMaps are highly optimized for enums, making texture lookups incredibly fast
    private final Map<TerrainType, Image> terrainTextures = new EnumMap<>(TerrainType.class);
    private final Map<UnitType, Map<PlayerId, Image>> unitTextures = new EnumMap<>(UnitType.class);
    private final Map<UnitType, Image> unitMovedTextures = new EnumMap<>(UnitType.class); 
    private final Map<BuildingType, Map<PlayerId, Image>> buildingTextures = new EnumMap<>(BuildingType.class);

    private Image grassFlowers;
    private Image waterMiddle;
    private Image waterLeftMiddle;
    private Image waterRightMiddle;
    private Image waterTopLeft;
    private Image waterTopMiddle;
    private Image waterTopRight;
    private Image waterBottomLeft;
    private Image waterBottomMiddle;
    private Image waterBottomRight;

    private Image tileSelect;

    public void loadAssets() {
        String terrainPath = "file:lib/assets/terrains/";
        String unitPath = "file:lib/assets/units/";
        String buildingPath = "file:lib/assets/buildings/";
        String otherPath = "file:lib/assets/other/";

        terrainTextures.put(TerrainType.PLAIN, new Image(terrainPath + "grass.png"));
        terrainTextures.put(TerrainType.FOREST, new Image(terrainPath + "forest.png"));
        terrainTextures.put(TerrainType.MOUNTAIN, new Image(terrainPath + "mountain.png"));
        grassFlowers = new Image(terrainPath + "grass_with_flowers.png");

        waterMiddle = new Image(terrainPath + "water_middle.png");
        waterLeftMiddle = new Image(terrainPath + "water_left_middle.png");
        waterRightMiddle = new Image(terrainPath + "water_right_middle.png");
        waterTopLeft = new Image(terrainPath + "water_top_left.png");
        waterTopMiddle = new Image(terrainPath + "water_top_middle.png");
        waterTopRight = new Image(terrainPath + "water_top_right.png");
        waterBottomLeft = new Image(terrainPath + "water_bottom_left.png");
        waterBottomMiddle = new Image(terrainPath + "water_bottom_middle.png");
        waterBottomRight = new Image(terrainPath + "water_bottom_right.png");
        
        // Buildings, forests, and mountains use transparent PNGs, so they conceptually 
        // sit on top of plains. We default their base tile to grass here.
        terrainTextures.put(TerrainType.CITY, new Image(terrainPath + "grass.png"));
        terrainTextures.put(TerrainType.FACTORY, new Image(terrainPath + "grass.png"));
        terrainTextures.put(TerrainType.HQ, new Image(terrainPath + "grass.png"));

        for (UnitType type : UnitType.values()) {
            Map<PlayerId, Image> playerUnitMap = new EnumMap<>(PlayerId.class);
            String typeName = type.name().toLowerCase(); 
            
            playerUnitMap.put(PlayerId.PLAYER_1, new Image(unitPath + typeName + "_blue.png"));
            playerUnitMap.put(PlayerId.PLAYER_2, new Image(unitPath + typeName + "_red.png"));
            unitTextures.put(type, playerUnitMap);

            // Exhausted units are rendered in grayscale to indicate they cannot be used this turn
            unitMovedTextures.put(type, new Image(unitPath + typeName + "_moved.png"));
        }

        for (BuildingType type : BuildingType.values()) {
            Map<PlayerId, Image> playerBuildingMap = new EnumMap<>(PlayerId.class);
            String typeName = type.name().toLowerCase();
            
            playerBuildingMap.put(PlayerId.NEUTRAL, new Image(buildingPath + typeName + "_neutral.png"));
            playerBuildingMap.put(PlayerId.PLAYER_1, new Image(buildingPath + typeName + "_blue.png"));
            playerBuildingMap.put(PlayerId.PLAYER_2, new Image(buildingPath + typeName + "_red.png"));
            buildingTextures.put(type, playerBuildingMap);
        }

        tileSelect = new Image(otherPath + "tile_select.png");
    }

    public Image getTileSelect() {
        return tileSelect;
    }

    public Image getGrassVariation(int x, int y) {
        // Uses a deterministic prime-number algorithm to generate a visually randomized 
        // but persistent 25% flower spawn rate based solely on grid coordinates.
        if ((x * 31 + y * 17) % 100 < 25) {
            return grassFlowers;
        }
        return terrainTextures.get(TerrainType.PLAIN);
    }

    public Image getContextualWater(boolean waterTop, boolean waterBottom, boolean waterLeft, boolean waterRight) {
        // Evaluates neighboring tiles to return the correct seamless auto-tiling sprite
        if (!waterTop && !waterLeft) return waterTopLeft;
        if (!waterTop && !waterRight) return waterTopRight;
        if (!waterBottom && !waterLeft) return waterBottomLeft;
        if (!waterBottom && !waterRight) return waterBottomRight;
        
        if (!waterTop) return waterTopMiddle;
        if (!waterBottom) return waterBottomMiddle;
        if (!waterLeft) return waterLeftMiddle;
        if (!waterRight) return waterRightMiddle;
        
        return waterMiddle;
    }

    public Image getTerrain(TerrainType type) { 
        return terrainTextures.get(type); 
    }

    public Image getBuilding(BuildingType type, PlayerId owner) { 
        return buildingTextures.get(type).get(owner); 
    }

    public Image getUnit(UnitType type, PlayerId owner) { 
        return unitTextures.get(type).get(owner); 
    }

    public Image getUnitMoved(UnitType type) { 
        return unitMovedTextures.get(type); 
    }
}